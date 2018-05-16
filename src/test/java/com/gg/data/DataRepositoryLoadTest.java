package com.gg.data;

import com.gg.dao.UserResponse;
import org.junit.Test;

import java.security.SecureRandom;
import java.time.Instant;
import java.util.Collection;
import java.util.LinkedList;
import java.util.UUID;
import java.util.concurrent.*;

import static java.time.Duration.between;
import static java.time.Instant.now;
import static java.util.stream.Collectors.averagingLong;

public class DataRepositoryLoadTest {
    private static final SecureRandom RANDOM = new SecureRandom();

    private static final ExecutorService THREAD_POOL = Executors.newFixedThreadPool(25);

    private final DataRepository dataRepository = new DataRepository();

    private Collection<Long> timesTakenToWrite = new LinkedList<>();
    private Collection<Long> timesTakenToRead = new LinkedList<>();
    private Collection<Long> timesTakenToReadFiltered = new LinkedList<>();

    private CountDownLatch startLatch = new CountDownLatch(1);

    private Collection<Future> taskFutures = new LinkedList<>();

    @Test
    public void shouldHandleHeavyWriters() {
        for (int i = 0; i < 15; i++) {
            taskFutures.add(THREAD_POOL.submit(new HeavyWriterWithRandomData(100000, dataRepository)));
        }
        for (int i = 0; i < 5; i++) {
            taskFutures.add(THREAD_POOL.submit(new HeavyReader(100000, dataRepository)));
        }
        for (int i = 0; i < 5; i++) {
            taskFutures.add(THREAD_POOL.submit(new HeavyReaderWithFiltering(100000, dataRepository)));
        }
        startLatch.countDown();

        taskFutures.forEach(this::awaitForFuture);

        System.out.println("Average write time (in nanos): " + timesTakenToWrite.stream().collect(averagingLong((element) -> element)));
        System.out.println("Average read time (in nanos): " + timesTakenToRead.stream().collect(averagingLong((element) -> element)));
        System.out.println("Average read time with filtering (in nanos): " + timesTakenToReadFiltered.stream().collect(averagingLong((element) -> element)));
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
        private final DataRepository dataRepository;

        private HeavyWriterWithRandomData(int amountOfDataToWrite, DataRepository dataRepository) {
            this.amountOfDataToWrite = amountOfDataToWrite;
            this.dataRepository = dataRepository;
        }

        @Override
        public void run() {
            waitForStart();
            for (int i = 0; i < amountOfDataToWrite; i++) {
                recordTimeTaken(timesTakenToWrite, () -> dataRepository.add(new UserResponse(UUID.randomUUID().toString(), RANDOM.nextInt() % 600)));
            }
        }
    }

    private class HeavyReader implements Runnable {

        private final int timesToRead;
        private final DataRepository dataRepository;

        private HeavyReader(int timesToRead, DataRepository dataRepository) {
            this.timesToRead = timesToRead;
            this.dataRepository = dataRepository;
        }

        @Override
        public void run() {
            waitForStart();
            for (int i = 0; i < timesToRead; i++) {
                recordTimeTaken(timesTakenToRead, dataRepository::getLast);
            }
        }
    }

    private class HeavyReaderWithFiltering implements Runnable {

        private final int timesToRead;
        private final DataRepository dataRepository;

        private HeavyReaderWithFiltering(int timesToRead, DataRepository dataRepository) {
            this.timesToRead = timesToRead;
            this.dataRepository = dataRepository;
        }

        @Override
        public void run() {
            waitForStart();
            for (int i = 0; i < timesToRead; i++) {
                recordTimeTaken(timesTakenToReadFiltered, dataRepository::getLastErrorCodes);
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
        Instant start = now();
        operation.run();
        targetCollection.add(between(start, now()).toNanos());
    }
}
