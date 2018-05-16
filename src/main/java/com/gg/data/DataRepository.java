package com.gg.data;

import com.gg.dao.UserResponse;

import java.util.Collection;
import java.util.Deque;
import java.util.concurrent.ConcurrentLinkedDeque;

import static java.util.stream.Collectors.toList;

public class DataRepository {
    private static final long TIMEOUT_FIVE_MINUTES = 5 * 60 * 1000;
    private static final int DEFAULT_MAXIMUM_SIZE = 100;
    private final long elementTimeout;
    private final int maximumSize;
    private final Deque<UserResponse> repository = new ConcurrentLinkedDeque<>();

    public DataRepository() {
        this(TIMEOUT_FIVE_MINUTES, DEFAULT_MAXIMUM_SIZE);
    }

    public DataRepository(long elementTimeout, int maximumSize) {
        this.elementTimeout = elementTimeout;
        this.maximumSize = maximumSize;
    }

    public void add(UserResponse userResponse) {
        removeElementsExceedingTimeoutOrMaximumSize();
        repository.add(new UserResponseWithTimeout(userResponse));
    }

    public Collection<UserResponse> getLastErrorCodes() {
        return getLast().stream().filter(this::isErrorResponseCode).collect(toList());
    }

    public Collection<UserResponse> getLast() {
        removeElementsExceedingTimeoutOrMaximumSize();
        return repository;
    }

    private synchronized void removeElementsExceedingTimeoutOrMaximumSize() {
        removeElementsExceedingTimeout();
        removeElementsOverMaximumSize();
    }

    private void removeElementsOverMaximumSize() {
        while (repository.size() > maximumSize) {
            repository.pop();
        }
    }

    private void removeElementsExceedingTimeout() {
        while (isHeadTimedOut()) {
            repository.pop();
        }
    }

    private boolean isHeadTimedOut() {
        UserResponseWithTimeout head = (UserResponseWithTimeout) repository.peek();
        return head != null && head.isTimedOut(elementTimeout);
    }

    private boolean isErrorResponseCode(UserResponse userResponse) {
        return userResponse.getResponseCode() >= 400;
    }
}
