package com.gg.storage;

import java.util.LinkedList;
import java.util.List;

public class CircularBufferExpiringStorage<E> implements ExpiringStorage<E> {
    private final long elementTimeout;
    private final DataWithTimeStamp<E>[] storage;
    private final Object incrementLock = new Object();
    private volatile int startIndex;
    private volatile int endIndex;

    public CircularBufferExpiringStorage() {
        this(DEFAULT_TIMEOUT, DEFAULT_MAXIMUM_SIZE);
    }

    public CircularBufferExpiringStorage(long elementTimeout, int maximumSize) {
        this.elementTimeout = elementTimeout;
        storage = new DataWithTimeStamp[maximumSize];
        for (int i = 0; i < maximumSize; i++) {
            storage[i] = new DataWithTimeStamp<>();
        }
    }

    @Override
    public void add(E element) {
        storage[endIndex].setData(element);
        synchronized (incrementLock) {
            endIndex = (endIndex + 1) % storage.length;
            if (endIndex == startIndex) {
                startIndex = (startIndex + 1) % storage.length;
            }
        }
    }

    @Override
    public List<E> getAll() {
        removeExpiredElements();
        return copyStorageToNewList();
    }

    private List<E> copyStorageToNewList() {
        List<E> result = new LinkedList<>();
        int startIndex = this.startIndex;
        int endIndex = this.endIndex;
        if (endIndex < startIndex) {
            copySplitStorage(result, startIndex, endIndex);
        } else {
            copyContinuousStorage(result, startIndex, endIndex);
        }
        return result;
    }

    private void copyContinuousStorage(List<E> result, int startIndex, int endIndex) {
        for (int i = startIndex; i < endIndex; i++) {
            result.add(storage[i].getData());
        }
    }

    private void copySplitStorage(List<E> result, int startIndex, int endIndex) {
        copyContinuousStorage(result, startIndex, storage.length);
        copyContinuousStorage(result, 0, endIndex+1);
    }

    private void removeExpiredElements() {
        synchronized (incrementLock) {
            while (storage[startIndex].isOlderThan(elementTimeout) && startIndex != endIndex) {
                startIndex = (startIndex + 1) % storage.length;
            }
        }
    }
}
