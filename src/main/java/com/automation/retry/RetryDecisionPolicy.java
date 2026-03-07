package com.automation.retry;

public interface RetryDecisionPolicy {
    boolean canRetry(int currentAttempt);
}
