package com.gg.api;

import java.util.List;

public interface UserResponseStore {
    List<UserResponse> getAllResponses();

    int getErrorResponseCount();

    void addResponse(UserResponse userResponse);
}
