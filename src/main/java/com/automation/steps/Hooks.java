package com.automation.steps;

import com.automation.core.base.Constants;
import com.automation.core.config.ConfigReader;
import com.automation.core.driver.TestContext;
import com.automation.core.logging.LogContext;
import com.automation.core.logging.LoggerUtil;
import com.automation.core.logging.StepLogBuffer;
import com.automation.transformers.CucumberInterceptor;
import com.automation.transformers.TestDataStore;
import io.cucumber.java.After;
import io.cucumber.java.AfterStep;
import io.cucumber.java.Before;
import io.cucumber.java.BeforeStep;
import io.cucumber.java.Scenario;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;

public class Hooks {

    private final TestContext testContext;

    public Hooks(TestContext testContext) {
        this.testContext = testContext;
    }

    @Before(order = 0)
    public void setUp(Scenario scenario) {
        LogContext.clear();
        LogContext.put("scenario", scenario.getName());
        LoggerUtil.info("Starting Scenario: " + scenario.getName());
        TestDataStore.clear(); // Clear data store for new scenario
        testContext.getDriver().get(ConfigReader.getProperty(Constants.login.URL));
    }

    @BeforeStep
    public void beforeStep(Scenario scenario) {
        StepLogBuffer.clear();
        // Intercept and transform data before the step executes
        CucumberInterceptor.beforeStep(scenario);
    }

    @AfterStep
    @SuppressWarnings("unused")
    public void afterStep(Scenario scenario) {
        // Attach Screenshot
        try {
            byte[] screenshot = ((TakesScreenshot) testContext.getDriver()).getScreenshotAs(OutputType.BYTES);
            if (screenshot != null && screenshot.length > 0) {
                scenario.attach(screenshot, "image/png", "Screenshot");
            }
        } catch (Exception e) {
            // Ignore
        }

        // Attach Logs
        String logs = StepLogBuffer.getLogs();
        if (logs != null && !logs.isEmpty()) {
            scenario.attach(logs, "text/plain", "Output");
        }
    }

    @After(order = 1)
    public void tearDown(Scenario scenario) {
        try {
            LoggerUtil.info("Finished Scenario: " + scenario.getName() + " [Status: " + scenario.getStatus() + "]");
            TestDataStore.clear(); // Clean up
            testContext.getDriverManager().quitDriver();
        } finally {
            LogContext.clear();
        }
    }
}
