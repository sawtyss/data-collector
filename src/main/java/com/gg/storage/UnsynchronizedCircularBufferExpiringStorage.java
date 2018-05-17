package com.gg.storage;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

public class UnsynchronizedCircularBufferExpiringStorage<E> implements ExpiringStorage<E> {
    private final long expirationTimeout;
    private final AtomicReference<DataWithTimeStamp<E>>[] storage;
    private final AtomicInteger currentIndex = new AtomicInteger();

    public UnsynchronizedCircularBufferExpiringStorage(long expirationTimeout, int maximumSize) {
        this.expirationTimeout = expirationTimeout;
        storage = new AtomicReference[maximumSize];
        for (int i = 0; i < maximumSize; i++) {
            storage[i] = new AtomicReference<>();
        }
    }

    @Override
    public void add(E element) {
        int index = currentIndex.getAndUpdate(value -> (value + 1) % storage.length);
        storage[index].set(new DataWithTimeStamp<>(element));
    }

    @Override
    public List<E> getAll() {
        List<E> data = new LinkedList<>();
        for (AtomicReference<DataWithTimeStamp<E>> aStorage : storage) {
            DataWithTimeStamp<E> dataWithTimeStamp = aStorage.get();
            if (dataWithTimeStamp != null && !dataWithTimeStamp.isOlderThan(expirationTimeout)) {
                data.add(dataWithTimeStamp.getData());
            }
        }
        return data;
    }
}
