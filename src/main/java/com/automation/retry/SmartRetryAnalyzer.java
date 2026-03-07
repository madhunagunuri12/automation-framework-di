package com.automation.retry;

import org.testng.IRetryAnalyzer;
import org.testng.ITestResult;

public class SmartRetryAnalyzer implements IRetryAnalyzer {

    @Override
    public boolean retry(ITestResult result) {
        int currentAttempt = result.getMethod().getCurrentInvocationCount();
        return RetryComponents.policy().canRetry(currentAttempt);
    }
}
