package com.automation.core.driver;

import org.openqa.selenium.WebDriver;

public interface WebDriverFactory {
    WebDriver create(BrowserType browserType, DriverConfig config);
}
