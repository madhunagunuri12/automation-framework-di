package com.automation.retry;

public final class RetryComponents {

    private static final RetrySettings SETTINGS = RetrySettings.fromSystemProperties();
    private static final RetryMetrics METRICS = new InMemoryRetryMetrics();
    private static final RetryDecisionPolicy POLICY = new ThresholdRetryDecisionPolicy(SETTINGS, METRICS);

    private RetryComponents() {
    }

    public static RetrySettings settings() {
        return SETTINGS;
    }

    public static RetryMetrics metrics() {
        return METRICS;
    }

    public static RetryDecisionPolicy policy() {
        return POLICY;
    }
}
