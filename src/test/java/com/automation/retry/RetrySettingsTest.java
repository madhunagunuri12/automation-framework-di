package com.automation.retry;

import org.testng.Assert;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.Test;

public class RetrySettingsTest {

    @AfterMethod
    public void clearProps() {
        System.clearProperty("retry.max");
        System.clearProperty("retry.threshold");
        System.clearProperty("retry.immediate.enabled");
    }

    @Test
    public void shouldLoadDefaultSettingsFromSystemProperties() {
        RetrySettings settings = RetrySettings.fromSystemProperties();

        Assert.assertEquals(settings.getMaxRetry(), 1);
        Assert.assertEquals(settings.getFailureThreshold(), 20.0d, 0.0001d);
        Assert.assertFalse(settings.isImmediateRetryEnabled());
    }

    @Test
    public void shouldLoadCustomSettingsFromSystemProperties() {
        System.setProperty("retry.max", "3");
        System.setProperty("retry.threshold", "12.5");
        System.setProperty("retry.immediate.enabled", "true");

        RetrySettings settings = RetrySettings.fromSystemProperties();

        Assert.assertEquals(settings.getMaxRetry(), 3);
        Assert.assertEquals(settings.getFailureThreshold(), 12.5d, 0.0001d);
        Assert.assertTrue(settings.isImmediateRetryEnabled());
    }
}
