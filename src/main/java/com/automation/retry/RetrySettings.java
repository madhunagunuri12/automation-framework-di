package com.automation.retry;

public final class RetrySettings {

    private final int maxRetry;
    private final double failureThreshold;
    private final boolean immediateRetryEnabled;

    private RetrySettings(Builder builder) {
        this.maxRetry = builder.maxRetry;
        this.failureThreshold = builder.failureThreshold;
        this.immediateRetryEnabled = builder.immediateRetryEnabled;
    }

    public static RetrySettings fromSystemProperties() {
        return builder()
                .maxRetry(parseInt("retry.max", 1))
                .failureThreshold(parseDouble("retry.threshold", 20.0d))
                .immediateRetryEnabled(Boolean.parseBoolean(System.getProperty("retry.immediate.enabled", "false")))
                .build();
    }

    private static int parseInt(String key, int defaultValue) {
        String value = System.getProperty(key);
        if (value == null || value.trim().isEmpty()) {
            return defaultValue;
        }
        return Integer.parseInt(value.trim());
    }

    private static double parseDouble(String key, double defaultValue) {
        String value = System.getProperty(key);
        if (value == null || value.trim().isEmpty()) {
            return defaultValue;
        }
        return Double.parseDouble(value.trim());
    }

    public static Builder builder() {
        return new Builder();
    }

    public int getMaxRetry() {
        return maxRetry;
    }

    public double getFailureThreshold() {
        return failureThreshold;
    }

    public boolean isImmediateRetryEnabled() {
        return immediateRetryEnabled;
    }

    public static final class Builder {
        private int maxRetry = 1;
        private double failureThreshold = 20.0d;
        private boolean immediateRetryEnabled;

        public Builder maxRetry(int maxRetry) {
            this.maxRetry = maxRetry;
            return this;
        }

        public Builder failureThreshold(double failureThreshold) {
            this.failureThreshold = failureThreshold;
            return this;
        }

        public Builder immediateRetryEnabled(boolean immediateRetryEnabled) {
            this.immediateRetryEnabled = immediateRetryEnabled;
            return this;
        }

        public RetrySettings build() {
            return new RetrySettings(this);
        }
    }
}
