package com.gg.storage;

import org.junit.Before;
import org.junit.Test;

import static java.lang.Thread.sleep;
import static org.fest.assertions.Assertions.assertThat;

public abstract class ExpiringStorageTest {
    private static final long EXPIRATION_TIMEOUT = 50;
    private static final int MAXIMUM_SIZE = 10;
    private static final Integer EXPIRED_OBJECT = 0;
    private static final Integer ANOTHER_EXPIRED_OBJECT = 1;
    private static final Integer NON_EXPIRED_OBJECT = 2;
    private static final Integer ANOTHER_NON_EXPIRED_OBJECT = 3;

    private ExpiringStorage<Integer> storage;

    abstract ExpiringStorage<Integer> createExpiringStorage(long expirationTimeout, int maximumSize);

    @Before
    public void setUp() {
        storage = createExpiringStorage(EXPIRATION_TIMEOUT, MAXIMUM_SIZE);
    }

    @Test
    public void shouldRemoveExpiredEntries() throws InterruptedException {
        storage.add(EXPIRED_OBJECT);
        sleepThroughExpiration();
        storage.add(NON_EXPIRED_OBJECT);
        storage.add(ANOTHER_NON_EXPIRED_OBJECT);

        assertThat(storage.getAll()).containsOnly(NON_EXPIRED_OBJECT, ANOTHER_NON_EXPIRED_OBJECT);
    }

    @Test
    public void shouldRemoveOldestEntriesWhenExceedingMaximumSize() {
        storage.add(EXPIRED_OBJECT);
        for (int i = 0; i < MAXIMUM_SIZE - 2; i++) {
            storage.add(i + 4);
        }
        storage.add(NON_EXPIRED_OBJECT);
        storage.add(ANOTHER_NON_EXPIRED_OBJECT);

        assertThat(storage.getAll()).hasSize(MAXIMUM_SIZE).excludes(EXPIRED_OBJECT);
    }

    @Test
    public void shouldReturnEmptyListForNoData() {
        assertThat(storage.getAll()).isEmpty();
    }

    @Test
    public void shouldReturnEmptyListWhenAllElementsExpired() throws InterruptedException {
        storage.add(EXPIRED_OBJECT);
        storage.add(ANOTHER_EXPIRED_OBJECT);
        sleepThroughExpiration();

        assertThat(storage.getAll()).isEmpty();
    }

    private void sleepThroughExpiration() throws InterruptedException {
        sleep(EXPIRATION_TIMEOUT * 2);
    }
}
