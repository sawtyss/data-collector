package com.gg.data;

import com.gg.dao.UserResponse;

import static java.lang.System.currentTimeMillis;

public class UserResponseWithTimeout extends UserResponse {
    private final long addTime;

    UserResponseWithTimeout(UserResponse userResponse) {
        super(userResponse);
        addTime = currentTimeMillis();
    }

    boolean isTimedOut(long timeout) {
        return currentTimeMillis() - addTime > timeout;
    }
}
