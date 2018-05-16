package com.gg.api;

public class UserResponse {
    private final String userAgent;
    private final int responseCode;

    public UserResponse(String userAgent, int responseCode) {
        this.userAgent = userAgent;
        this.responseCode = responseCode;
    }

    public UserResponse(UserResponse userResponse) {
        this(userResponse.getUserAgent(), userResponse.getResponseCode());
    }

    public String getUserAgent() {
        return userAgent;
    }

    public int getResponseCode() {
        return responseCode;
    }
}
