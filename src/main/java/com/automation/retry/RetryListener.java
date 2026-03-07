package com.automation.retry;

import org.testng.ITestListener;
import org.testng.ITestResult;

public class RetryListener implements ITestListener {

    @Override
    public void onTestStart(ITestResult result) {
        RetryComponents.metrics().recordStart();
    }

    @Override
    public void onTestFailure(ITestResult result) {
        RetryComponents.metrics().recordFailure();
    }
}
