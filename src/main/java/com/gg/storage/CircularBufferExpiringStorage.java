package com.gg.storage;

import java.util.LinkedList;
import java.util.List;

public class CircularBufferExpiringStorage<E> implements ExpiringStorage<E> {
    private final long elementTimeout;
    private final DataWithTimeStamp<E>[] storage;
    private volatile int startIndex;
    private volatile int endIndex;

    public CircularBufferExpiringStorage(long elementTimeout, int maximumSize) {
        this.elementTimeout = elementTimeout;
        storage = new DataWithTimeStamp[maximumSize];
        for (int i = 0; i < maximumSize; i++) {
            storage[i] = new DataWithTimeStamp<>();
        }
    }

    @Override
    public synchronized void add(E element) {
        storage[endIndex].setData(element);
        unsafeIncrementEndIndex();
        incrementStartIndexIfMaximumSizeReached();
    }

    @Override
    public List<E> getAll() {
        removeExpiredElements();
        return copyStorageToNewList();
    }

    private List<E> copyStorageToNewList() {
        List<E> result = new LinkedList<>();
        int startIndex;
        int endIndex;
        synchronized (this) {
            startIndex = this.startIndex;
            endIndex = this.endIndex;
        }
        if (endIndex < startIndex) {
            copySplitStorageToResult(result, startIndex, endIndex);
        } else {
            copyContinuousStorageToResult(result, startIndex, endIndex);
        }
        return result;
    }

    private void copySplitStorageToResult(List<E> result, int startIndex, int endIndex) {
        copyContinuousStorageToResult(result, startIndex, storage.length);
        copyContinuousStorageToResult(result, 0, endIndex + 1);
    }

    private void copyContinuousStorageToResult(List<E> result, int startIndex, int endIndex) {
        for (int i = startIndex; i < endIndex; i++) {
            result.add(storage[i].getData());
        }
    }

    private synchronized void removeExpiredElements() {
        while (storage[startIndex].isOlderThan(elementTimeout) && startIndex != endIndex) {
            unsafeIncrementStartIndex();
        }
    }

    private void incrementStartIndexIfMaximumSizeReached() {
        if (endIndex == startIndex) {
            unsafeIncrementStartIndex();
        }
    }

    private void unsafeIncrementStartIndex() {
        startIndex = (startIndex + 1) % storage.length;
    }

    private void unsafeIncrementEndIndex() {
        endIndex = (endIndex + 1) % storage.length;
    }
}
