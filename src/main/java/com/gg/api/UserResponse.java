package com.gg.api;

import java.util.Objects;

public class UserResponse {
    private final String userAgent;
    private final int responseCode;

    public UserResponse(String userAgent, int responseCode) {
        this.userAgent = userAgent;
        this.responseCode = responseCode;
    }

    public String getUserAgent() {
        return userAgent;
    }

    public int getResponseCode() {
        return responseCode;
    }

    @Override
    public boolean equals(Object o) {
        return this == o || o instanceof UserResponse && equals((UserResponse) o);
    }

    private boolean equals(UserResponse o) {
        return responseCode == o.responseCode &&
                Objects.equals(userAgent, o.userAgent);
    }

    @Override
    public int hashCode() {
        return Objects.hash(userAgent, responseCode);
    }

    @Override
    public String toString() {
        return userAgent + ':' + responseCode;
    }
}
