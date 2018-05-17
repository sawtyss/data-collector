package com.gg;

import com.gg.api.UserResponse;
import com.gg.api.UserResponseStore;
import com.gg.storage.ExpiringStorage;

import java.util.List;

class DefaultUserResponseStore implements UserResponseStore {
    private ExpiringStorage<UserResponse> dataStore;

    DefaultUserResponseStore(ExpiringStorage<UserResponse> dataStore) {
        this.dataStore = dataStore;
    }

    @Override
    public List<UserResponse> getAllResponses() {
        return dataStore.getAll();
    }

    @Override
    public int getErrorResponseCount() {
        return (int) dataStore.getAll().stream().filter(this::isErrorUserResponseCode).count();
    }

    @Override
    public void addResponse(UserResponse userResponse) {
        dataStore.add(userResponse);
    }

    private boolean isErrorUserResponseCode(UserResponse userResponse) {
        return userResponse.getResponseCode() >= 400 && userResponse.getResponseCode() < 600;
    }
}
