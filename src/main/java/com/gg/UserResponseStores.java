package com.gg;

import com.gg.api.UserResponseStore;
import com.gg.storage.UnsynchronizedCircularBufferExpiringStorage;

public class UserResponseStores {
    public static final long DEFAULT_TIMEOUT = 5 * 60 * 1000;
    public static final int DEFAULT_MAXIMUM_SIZE = 100;

    public static UserResponseStore createUserResponseStoreOnUnsynchronizedCircularBuffer(long elementTimeout, int maximumSize) {
        return new DefaultUserResponseStore(new UnsynchronizedCircularBufferExpiringStorage<>(elementTimeout, maximumSize));
    }

    public static UserResponseStore createUserResponseStore() {
        return createUserResponseStore(DEFAULT_TIMEOUT, DEFAULT_MAXIMUM_SIZE);
    }

    public static UserResponseStore createUserResponseStore(long elementTimeout, int maximumSize) {
        return new DefaultUserResponseStore(new UnsynchronizedCircularBufferExpiringStorage<>(elementTimeout, maximumSize));
    }
}
