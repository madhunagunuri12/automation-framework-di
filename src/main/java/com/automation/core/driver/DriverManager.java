package com.automation.core.driver;

import java.util.EnumMap;
import java.util.Map;
import java.util.Objects;
import org.openqa.selenium.WebDriver;

public class DriverManager {

    private final DriverConfig driverConfig;
    private final Map<ExecutionMode, WebDriverFactory> factories;
    private WebDriver driver;

    public DriverManager() {
        this(DriverConfig.fromSystemProperties());
    }

    public DriverManager(DriverConfig driverConfig) {
        this.driverConfig = Objects.requireNonNull(driverConfig, "driverConfig must not be null");
        this.factories = new EnumMap<ExecutionMode, WebDriverFactory>(ExecutionMode.class);
        this.factories.put(ExecutionMode.LOCAL, new LocalWebDriverFactory());
        this.factories.put(ExecutionMode.REMOTE, new RemoteWebDriverFactory());
    }

    public synchronized WebDriver getDriver() {
        if (driver == null) {
            driver = initDriver();
        }
        return driver;
    }

    private WebDriver initDriver() {
        WebDriverFactory factory = factories.get(driverConfig.getExecutionMode());
        if (factory == null) {
            throw new IllegalStateException("No WebDriverFactory found for execution mode: "
                    + driverConfig.getExecutionMode());
        }

        WebDriver webDriver = factory.create(driverConfig.getBrowserType(), driverConfig);
        if (driverConfig.isMaximizeWindow()) {
            webDriver.manage().window().maximize();
        }
        return webDriver;
    }

    public synchronized void quitDriver() {
        if (driver != null) {
            driver.quit();
            driver = null;
        }
    }
}
