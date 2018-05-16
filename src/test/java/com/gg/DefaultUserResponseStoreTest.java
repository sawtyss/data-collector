package com.gg;

import com.gg.api.UserResponse;
import com.gg.storage.ExpiringStorage;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.List;

import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static org.fest.assertions.Assertions.assertThat;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class DefaultUserResponseStoreTest {
    private static final UserResponse RESPONSE_ONE = new UserResponse("one", 1);
    private static final UserResponse RESPONSE_TWO = new UserResponse("two", 2);
    private static final UserResponse ERROR_RESPONSE_ONE = new UserResponse("errorOne", 401);
    private static final UserResponse ERROR_RESPONSE_TWO = new UserResponse("errorTwo", 402);
    private static final List<UserResponse> ALL_RESPONSES = asList(RESPONSE_ONE, RESPONSE_TWO, ERROR_RESPONSE_ONE, ERROR_RESPONSE_TWO);

    @Mock
    private ExpiringStorage<UserResponse> storage;

    @InjectMocks
    private DefaultUserResponseStore responseStore;

    @Test
    public void shouldGetAllResponses() {
        when(storage.getAll()).thenReturn(ALL_RESPONSES);

        assertThat(responseStore.getAllResponses()).containsOnly(RESPONSE_ONE, RESPONSE_TWO, ERROR_RESPONSE_ONE, ERROR_RESPONSE_TWO);
    }

    @Test
    public void shouldGetErrorResponseCount() {
        when(storage.getAll()).thenReturn(ALL_RESPONSES);

        assertEquals(2, responseStore.getErrorResponseCount());
    }

    @Test
    public void shouldGetErrorResponseCountWhenNoData() {
        when(storage.getAll()).thenReturn(emptyList());

        assertEquals(0, responseStore.getErrorResponseCount());
    }

    @Test
    public void shouldAddResponse() {
        responseStore.addResponse(RESPONSE_ONE);

        verify(storage).add(RESPONSE_ONE);
    }
}