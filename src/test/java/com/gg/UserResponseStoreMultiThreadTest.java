package com.gg;

import com.gg.api.UserResponse;
import com.gg.api.UserResponseStore;
import org.junit.Test;

import java.util.Collection;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import static com.gg.UserResponseStores.createUserResponseStoreOnCircularBuffer;
import static com.gg.UserResponseStores.createUserResponseStoreOnDeque;
import static com.gg.UserResponseStores.createUserResponseStoreOnUnsynchronizedCircularBuffer;
import static java.util.Comparator.comparing;
import static org.fest.assertions.Assertions.assertThat;

public class UserResponseStoreMultiThreadTest {
    private static final ExecutorService THREAD_POOL = Executors.newFixedThreadPool(10);

    private final CountDownLatch startLatch = new CountDownLatch(1);
    private final List<UserResponse> allThreadsData = new LinkedList<>();
    private final List<Future<?>> tasks = new LinkedList<>();

    private int idCounter;
    private int statusCodeCounter;

    private UserResponseStore userResponseStore;

    @Test
    public void shouldDequeBaseHandleMultipleWritersAndNotLooseData() {
        userResponseStore = createUserResponseStoreOnDeque(1000, 100);

        shouldUserResponseStoreHandleMultipleWriters();
    }

    @Test
    public void shouldCircularBufferBaseHandleMultipleWritersAndNotLooseData() {
        userResponseStore = createUserResponseStoreOnCircularBuffer(1000, 100);

        shouldUserResponseStoreHandleMultipleWriters();
    }

    @Test
    public void shouldUnsynchronizedCircularBufferBaseHandleMultipleWritersAndNotLooseData() {
        userResponseStore = createUserResponseStoreOnUnsynchronizedCircularBuffer(1000, 100);

        shouldUserResponseStoreHandleMultipleWriters();
    }

    private void shouldUserResponseStoreHandleMultipleWriters() {
        for (int i = 0; i < 10; i++) {
            submitNewWriterTask();
        }
        startLatch.countDown();

        tasks.forEach(this::waitForFutureToFinish);

        List<UserResponse> allResponses = userResponseStore.getAllResponses();
        allResponses.sort(comparing(UserResponse::getResponseCode));

        assertThat(allResponses).isEqualTo(allThreadsData);
    }

    private void submitNewWriterTask() {
        List<UserResponse> dataForThread = generateDataForThread(10);
        allThreadsData.addAll(dataForThread);
        tasks.add(THREAD_POOL.submit(new WritingTask(dataForThread, userResponseStore)));
    }

    private List<UserResponse> generateDataForThread(int dataCount) {
        List<UserResponse> dataForThread = new LinkedList<>();
        for (int i = 0; i < dataCount; i++) {
            dataForThread.add(new UserResponse("Agent" + idCounter++, statusCodeCounter++));
        }
        return dataForThread;
    }

    private class WritingTask implements Runnable {

        private List<UserResponse> data;
        private UserResponseStore userResponseStore;

        private WritingTask(List<UserResponse> data, UserResponseStore userResponseStore) {
            this.data = data;
            this.userResponseStore = userResponseStore;
        }

        @Override
        public void run() {
            waitForStart();
            data.forEach(userResponseStore::addResponse);
        }

    }

    private void waitForStart() {
        try {
            startLatch.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private void waitForFutureToFinish(Future<?> future) {
        try {
            future.get();
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }
}
