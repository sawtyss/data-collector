package com.gg.storage;

import java.util.List;

public interface ExpiringStorage<E> {
    long DEFAULT_TIMEOUT = 5 * 60 * 1000;
    int DEFAULT_MAXIMUM_SIZE = 100;

    void add(E element);

    List<E> getAll();
}
