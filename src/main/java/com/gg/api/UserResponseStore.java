package com.gg.api;

import java.util.Collection;

public interface UserResponseStore {
    Collection<UserResponse> getAllResponses();

    int getErrorResponseCount();

    void addResponse(UserResponse userResponse);
}
