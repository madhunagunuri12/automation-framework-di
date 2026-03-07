package com.automation.retry;

public interface RetryMetrics {
    void recordStart();

    void recordFailure();

    double failureRate();

    int totalTests();

    int failedTests();

    void reset();
}
