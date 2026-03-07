package com.automation.retry;

import java.util.Objects;

public class ThresholdRetryDecisionPolicy implements RetryDecisionPolicy {

    private final RetrySettings settings;
    private final RetryMetrics metrics;

    public ThresholdRetryDecisionPolicy(RetrySettings settings, RetryMetrics metrics) {
        this.settings = Objects.requireNonNull(settings, "settings must not be null");
        this.metrics = Objects.requireNonNull(metrics, "metrics must not be null");
    }

    @Override
    public boolean canRetry(int currentAttempt) {
        if (!settings.isImmediateRetryEnabled()) {
            return false;
        }

        if (currentAttempt >= settings.getMaxRetry()) {
            return false;
        }

        return metrics.failureRate() <= settings.getFailureThreshold();
    }
}
