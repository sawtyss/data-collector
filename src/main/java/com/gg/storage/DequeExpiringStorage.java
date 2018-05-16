package com.gg.storage;

import java.util.Deque;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.stream.Collectors;

public class DequeExpiringStorage<E> implements ExpiringStorage<E> {
    private final long elementTimeout;
    private final int maximumSize;
    private final Deque<DataWithTimeStamp<E>> repository = new ConcurrentLinkedDeque<>();

    public DequeExpiringStorage(long elementTimeout, int maximumSize) {
        this.elementTimeout = elementTimeout;
        this.maximumSize = maximumSize;
    }

    public void add(E userResponse) {
        removeElementsOverMaximumSizeOrExpired();
        repository.add(new DataWithTimeStamp<>(userResponse));
    }

    public List<E> getAll() {
        removeElementsOverMaximumSizeOrExpired();
        return repository.stream().map(DataWithTimeStamp::getData).collect(Collectors.toList());
    }

    private synchronized void removeElementsOverMaximumSizeOrExpired() {
        removeExpiredElements();
        removeElementsOverMaximumSize();
    }

    private void removeElementsOverMaximumSize() {
        while (repository.size() > maximumSize) {
            repository.pop();
        }
    }

    private void removeExpiredElements() {
        while (isHeadExpired()) {
            repository.pop();
        }
    }

    private boolean isHeadExpired() {
        DataWithTimeStamp<E> head = repository.peek();
        return head != null && head.isOlderThan(elementTimeout);
    }
}
