package com.gg.storage;

public class UnsynchronizedCircularBufferExpiringStorageTest extends ExpiringStorageTest {
    @Override
    ExpiringStorage<Integer> createExpiringStorage(long expirationTimeout, int maximumSize) {
        return new UnsynchronizedCircularBufferExpiringStorage<>(expirationTimeout, maximumSize);
    }
}