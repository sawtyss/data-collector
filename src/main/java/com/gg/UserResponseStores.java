package com.gg;

import com.gg.api.UserResponseStore;
import com.gg.storage.CircularBufferExpiringStorage;
import com.gg.storage.DequeExpiringStorage;

public class UserResponseStores {
    public static UserResponseStore createUserResponseStoreOnDeque()
    {
        return new DefaultUserResponseStore(new DequeExpiringStorage<>());
    }

    public static UserResponseStore createUserResponseStoreOnCircularBuffer()
    {
        return new DefaultUserResponseStore(new CircularBufferExpiringStorage<>());
    }

    public static UserResponseStore createUserResponseStore()
    {
        return createUserResponseStoreOnCircularBuffer();
    }
}
