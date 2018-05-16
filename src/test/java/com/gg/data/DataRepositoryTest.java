package com.gg.data;

import com.gg.dao.UserResponse;
import org.junit.Test;

import java.util.LinkedList;
import java.util.List;
import java.util.UUID;

import static org.fest.assertions.Assertions.assertThat;

public class DataRepositoryTest {
    private final DataRepository dataRepository = new DataRepository();

    @Test
    public void shouldFilterErrorCodes() {
        List<UserResponse> errorResponses = new LinkedList<>();
        List<UserResponse> nonErrorResponses = new LinkedList<>();

        for (int i = 0; i < 30; i++) {
            errorResponses.add(createErrorResponse());
        }

        for (int i = 0; i < 30; i++) {
            nonErrorResponses.add(createNonErrorResponse());
        }

        recordResponses(errorResponses);
        recordResponses(nonErrorResponses);

        assertThat(dataRepository.getLastErrorCodes()).hasSize(30).containsOnly(errorResponses.toArray());
    }


    private void recordResponses(List<UserResponse> responses) {
        responses.forEach(dataRepository::add);
    }

    private UserResponse createNonErrorResponse() {
        return new UserResponse(UUID.randomUUID().toString(), 200);
    }

    private UserResponse createErrorResponse() {
        return new UserResponse(UUID.randomUUID().toString(), 450);
    }
}
