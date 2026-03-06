package com.automation.retry;

import org.testng.IRetryAnalyzer;
import org.testng.ITestResult;

public class SmartRetryAnalyzer implements IRetryAnalyzer {

    @Override
    public boolean retry(ITestResult result) {
        // We do NOT want immediate retries.
        // The framework handles reruns via a separate execution phase (ReportGenerator -> cucumberRerun task).
        // Returning false ensures the test fails immediately and moves to the next one.
        return false;
    }
}
