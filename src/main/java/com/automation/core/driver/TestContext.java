package com.automation.core.driver;

import org.openqa.selenium.WebDriver;

public class TestContext {

    private final DriverManager driverManager;
    private final WebDriver driver;

    public TestContext() {
        this.driverManager = new DriverManager();
        this.driver = driverManager.getDriver();
    }

    public WebDriver getDriver() {
        return driver;
    }
    
    public DriverManager getDriverManager() {
        return driverManager;
    }
}
