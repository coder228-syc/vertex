package com.vertex.client.util;

public class TimerUtil {

    private long time = System.currentTimeMillis();

    public void reset() {
        time = System.currentTimeMillis();
    }

    public long getTime() {
        return System.currentTimeMillis() - time;
    }

    public void setTime(long time) {
        this.time = time;
    }

    public boolean hasTimeElapsed(long ms) {
        return getTime() >= ms;
    }
}
