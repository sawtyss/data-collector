package com.gg.storage;

import java.util.List;

public interface ExpiringStorage<E> {
    void add(E element);

    List<E> getAll();
}
