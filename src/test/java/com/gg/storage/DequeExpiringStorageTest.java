package com.gg.storage;

public class DequeExpiringStorageTest extends ExpiringStorageTest {
    @Override
    ExpiringStorage<Integer> createExpiringStorage(long expirationTimeout, int maximumSize) {
        return new DequeExpiringStorage<>(expirationTimeout, maximumSize);
    }
}
