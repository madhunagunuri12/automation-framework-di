package com.automation.retry;

import java.util.concurrent.atomic.AtomicInteger;

public final class RetryTracker {

    private static final AtomicInteger total =
            new AtomicInteger(0);

    private static final AtomicInteger failed =
            new AtomicInteger(0);

    private RetryTracker() {
        throw new UnsupportedOperationException("Utility class");
    }

    public static void recordStart() {
        total.incrementAndGet();
    }

    public static void recordFailure() {
        failed.incrementAndGet();
    }

    public static double failureRate() {

        int t = total.get();

        if (t == 0) {
            return 0;
        }

        return (failed.get() * 100.0) / t;
    }
}
