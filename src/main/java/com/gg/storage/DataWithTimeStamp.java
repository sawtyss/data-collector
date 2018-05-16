package com.gg.storage;

import static java.lang.System.currentTimeMillis;

class DataWithTimeStamp<E> {
    private volatile E data;
    private long timeStamp = currentTimeMillis();

    DataWithTimeStamp() {
    }

    DataWithTimeStamp(E data) {
        this.data = data;
    }

    synchronized void setData(E data) {
        timeStamp = currentTimeMillis();
        this.data = data;
    }

    E getData() {
        return data;
    }

    boolean isOlderThan(long periodInMillis) {
        return currentTimeMillis() - timeStamp > periodInMillis;
    }
}
