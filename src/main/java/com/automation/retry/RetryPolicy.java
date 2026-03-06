package com.automation.retry;

public final class RetryPolicy {

    private RetryPolicy() {
        throw new UnsupportedOperationException("Utility class");
    }

    public static boolean canRetry(int currentAttempt) {

        int maxRetry = RetryConfig.MAX_RETRY;

        if (currentAttempt >= maxRetry) {
            return false;
        }

        double threshold = RetryConfig.FAILURE_THRESHOLD;

        double failureRate =
                RetryTracker.failureRate();

        return failureRate <= threshold;
    }
}
