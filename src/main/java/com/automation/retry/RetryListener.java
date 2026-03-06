package com.automation.retry;

import org.testng.ITestListener;
import org.testng.ITestResult;

public class RetryListener implements ITestListener {

    @Override
    public void onTestStart(ITestResult result) {
        RetryTracker.recordStart();
    }

    @Override
    public void onTestFailure(ITestResult result) {
        RetryTracker.recordFailure();
    }
}
