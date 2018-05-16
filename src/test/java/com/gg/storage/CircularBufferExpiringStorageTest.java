package com.gg.storage;

public class CircularBufferExpiringStorageTest extends ExpiringStorageTest {
    @Override
    ExpiringStorage<Integer> createExpiringStorage(long expirationTimeout, int maximumSize) {
        return new CircularBufferExpiringStorage<>(expirationTimeout, maximumSize);
    }
}