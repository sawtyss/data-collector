package com.gg.storage;

import com.gg.api.UserResponse;
import org.junit.Test;

import java.security.SecureRandom;
import java.util.Collection;
import java.util.LinkedList;
import java.util.UUID;
import java.util.concurrent.*;

import static java.util.stream.Collectors.averagingLong;

public class ExpiringStorageLoadTest {
    private static final SecureRandom RANDOM = new SecureRandom();
    private static final ExecutorService THREAD_POOL = Executors.newFixedThreadPool(8);

    private Collection<Long> timesTakenToWrite = new LinkedList<>();
    private Collection<Long> timesTakenToRead = new LinkedList<>();

    private CountDownLatch startLatch = new CountDownLatch(1);

    private Collection<Future> taskFutures = new LinkedList<>();

    @Test
    public void shouldCircularBufferStorageHandleHeavyWriters() {
        shouldStorageHandleHeavyLoad(new CircularBufferExpiringStorage<>());
    }

    @Test
    public void shouldDequeStorageHandleHeavyWriters() {
        shouldStorageHandleHeavyLoad(new DequeExpiringStorage<>());
    }

    private void shouldStorageHandleHeavyLoad(ExpiringStorage<UserResponse> expiringStorage) {
        for (int i = 0; i < 4; i++) {
            taskFutures.add(THREAD_POOL.submit(new HeavyWriterWithRandomData(100000, expiringStorage)));
        }
        for (int i = 0; i < 4; i++) {
            taskFutures.add(THREAD_POOL.submit(new HeavyReader(100000, expiringStorage)));
        }
        startLatch.countDown();

        taskFutures.forEach(this::awaitForFuture);
        System.out.println(expiringStorage.getClass().getSimpleName() + " average write time (in nanos): " + timesTakenToWrite.stream().collect(averagingLong((element) -> element)));
        System.out.println(expiringStorage.getClass().getSimpleName() + " average read time (in nanos): " + timesTakenToRead.stream().collect(averagingLong((element) -> element)));
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
        private final ExpiringStorage<UserResponse> expiringStorage;

        private HeavyWriterWithRandomData(int amountOfDataToWrite, ExpiringStorage<UserResponse> expiringStorage) {
            this.amountOfDataToWrite = amountOfDataToWrite;
            this.expiringStorage = expiringStorage;
        }

        @Override
        public void run() {
            waitForStart();
            for (int i = 0; i < amountOfDataToWrite; i++) {
                recordTimeTaken(timesTakenToWrite, () -> expiringStorage.add(new UserResponse(UUID.randomUUID().toString(), RANDOM.nextInt() % 600)));
            }
        }
    }

    private class HeavyReader implements Runnable {

        private final int timesToRead;
        private final ExpiringStorage<UserResponse> expiringStorage;

        private HeavyReader(int timesToRead, ExpiringStorage<UserResponse> expiringStorage) {
            this.timesToRead = timesToRead;
            this.expiringStorage = expiringStorage;
        }

        @Override
        public void run() {
            waitForStart();
            for (int i = 0; i < timesToRead; i++) {
                recordTimeTaken(timesTakenToRead, expiringStorage::getAll);
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
