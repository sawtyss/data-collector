package com.gg;

import com.gg.api.UserResponse;
import com.gg.api.UserResponseStore;
import org.junit.Test;

import java.security.SecureRandom;
import java.util.Collection;
import java.util.LinkedList;
import java.util.UUID;
import java.util.concurrent.*;

import static com.gg.UserResponseStores.*;
import static java.util.stream.Collectors.averagingLong;

public class UserResponseStoreLoadTest {
    private static final SecureRandom RANDOM = new SecureRandom();
    private static final ExecutorService THREAD_POOL = Executors.newFixedThreadPool(8);

    private Collection<Long> timesTakenToWrite = new LinkedList<>();
    private Collection<Long> timesTakenToRead = new LinkedList<>();

    private CountDownLatch startLatch = new CountDownLatch(1);

    private Collection<Future> taskFutures = new LinkedList<>();

    @Test
    public void shouldCircularBufferStorageHandleHeavyWriters() {
        shouldStorageHandleHeavyLoad(createUserResponseStoreOnCircularBuffer(DEFAULT_TIMEOUT, 100), "Circular buffer-based store");
    }

    @Test
    public void shouldDequeStorageHandleHeavyWriters() {
        shouldStorageHandleHeavyLoad(createUserResponseStoreOnDeque(DEFAULT_TIMEOUT, 100), "Deque-based store");
    }

    @Test
    public void shouldUnsynchronizedCircularBufferStorageHandleHeavyWriters()
    {
        shouldStorageHandleHeavyLoad(createUserResponseStoreOnUnsynchronizedCircularBuffer(DEFAULT_TIMEOUT, 100), "Unsynchronized circular buffer-based store");
    }

    private void shouldStorageHandleHeavyLoad(UserResponseStore responseStore, String storeType) {
        for (int i = 0; i < 4; i++) {
            taskFutures.add(THREAD_POOL.submit(new HeavyWriterWithRandomData(100000, responseStore)));
        }
        for (int i = 0; i < 4; i++) {
            taskFutures.add(THREAD_POOL.submit(new HeavyReader(100000, responseStore)));
        }
        startLatch.countDown();

        taskFutures.forEach(this::awaitForFuture);
        System.out.println(storeType + " average write time (in nanos): " + timesTakenToWrite.stream().collect(averagingLong((element) -> element)));
        System.out.println(storeType + " average read time (in nanos): " + timesTakenToRead.stream().collect(averagingLong((element) -> element)));
    }

    private void awaitForFuture(Future future) {
        try {
            future.get();
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }
    }

    private class HeavyWriterWithRandomData implements Runnable {
        private final int amountOfDataToWrite;
        private final UserResponseStore userResponseStore;

        private HeavyWriterWithRandomData(int amountOfDataToWrite, UserResponseStore userResponseStore) {
            this.amountOfDataToWrite = amountOfDataToWrite;
            this.userResponseStore = userResponseStore;
        }

        @Override
        public void run() {
            waitForStart();
            for (int i = 0; i < amountOfDataToWrite; i++) {
                recordTimeTaken(timesTakenToWrite, () -> userResponseStore.addResponse(new UserResponse(UUID.randomUUID().toString(), RANDOM.nextInt() % 600)));
            }
        }
    }

    private class HeavyReader implements Runnable {

        private final int timesToRead;
        private final UserResponseStore userResponseStore;

        private HeavyReader(int timesToRead, UserResponseStore userResponseStore) {
            this.timesToRead = timesToRead;
            this.userResponseStore = userResponseStore;
        }

        @Override
        public void run() {
            waitForStart();
            for (int i = 0; i < timesToRead; i++) {
                recordTimeTaken(timesTakenToRead, userResponseStore::getAllResponses);
            }
        }
    }

    private void waitForStart() {
        try {
            startLatch.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private void recordTimeTaken(Collection<Long> targetCollection, Runnable operation) {
        long start = System.nanoTime();
        operation.run();
        targetCollection.add(System.nanoTime() - start);
    }
}
