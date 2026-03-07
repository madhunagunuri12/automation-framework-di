package com.automation.retry;

import java.util.concurrent.atomic.AtomicInteger;

public class InMemoryRetryMetrics implements RetryMetrics {

    private final AtomicInteger total = new AtomicInteger(0);
    private final AtomicInteger failed = new AtomicInteger(0);

    @Override
    public void recordStart() {
        total.incrementAndGet();
    }

    @Override
    public void recordFailure() {
        failed.incrementAndGet();
    }

    @Override
    public double failureRate() {
        int totalCount = total.get();
        if (totalCount == 0) {
            return 0;
        }
        return (failed.get() * 100.0d) / totalCount;
    }

    @Override
    public int totalTests() {
        return total.get();
    }

    @Override
    public int failedTests() {
        return failed.get();
    }

    @Override
    public void reset() {
        total.set(0);
        failed.set(0);
    }
}
