package com.automation.retry;

public final class RetryConfig {

    private RetryConfig() {
        throw new UnsupportedOperationException("Utility class");
    }

    public static final int MAX_RETRY =
            Integer.parseInt(
                    System.getProperty("retry.max", "1"));

    public static final double FAILURE_THRESHOLD =
            Double.parseDouble(
                    System.getProperty("retry.threshold", "20"));
}
