package com.automation.retry;

import org.testng.Assert;
import org.testng.annotations.Test;

public class ThresholdRetryDecisionPolicyTest {

    @Test
    public void shouldNotRetryWhenImmediateRetryIsDisabled() {
        RetrySettings settings = RetrySettings.builder()
                .maxRetry(2)
                .failureThreshold(50.0d)
                .immediateRetryEnabled(false)
                .build();

        RetryMetrics metrics = new InMemoryRetryMetrics();
        metrics.recordStart();
        metrics.recordFailure();

        RetryDecisionPolicy policy = new ThresholdRetryDecisionPolicy(settings, metrics);
        Assert.assertFalse(policy.canRetry(0));
    }

    @Test
    public void shouldRetryWhenEnabledAndFailureRateWithinThreshold() {
        RetrySettings settings = RetrySettings.builder()
                .maxRetry(3)
                .failureThreshold(50.0d)
                .immediateRetryEnabled(true)
                .build();

        RetryMetrics metrics = new InMemoryRetryMetrics();
        metrics.recordStart();
        metrics.recordFailure();
        metrics.recordStart();

        RetryDecisionPolicy policy = new ThresholdRetryDecisionPolicy(settings, metrics);
        Assert.assertTrue(policy.canRetry(1));
    }

    @Test
    public void shouldNotRetryWhenAttemptReachedMaxRetry() {
        RetrySettings settings = RetrySettings.builder()
                .maxRetry(1)
                .failureThreshold(100.0d)
                .immediateRetryEnabled(true)
                .build();

        RetryDecisionPolicy policy = new ThresholdRetryDecisionPolicy(settings, new InMemoryRetryMetrics());
        Assert.assertFalse(policy.canRetry(1));
    }
}
