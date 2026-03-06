package com.automation.core.base;

import com.automation.core.logging.LoggerUtil;
import java.util.List;
import java.util.Optional;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

public class Base extends SeleniumUtils {

    protected WebDriver driver;

    public Base(WebDriver driver, List<By> defaultLocators) {
        super(driver);
        this.driver = driver;
        verifyDefaultLocators(defaultLocators);
    }

    public void navigateTo(String url) {
        driver.get(url);
    }

    public String getPageTitle() {
        return driver.getTitle();
    }

    public void closeCurrentWindow() {
        driver.close();
    }

    public void quitDriver() {
        driver.quit();
    }

    private void verifyDefaultLocators(List<By> locators) {
        LoggerUtil.info("🔍 Verifying default locators...");

        for (By locator : locators) {
            Optional<WebElement> element = waitForElement(locator, DEFAULT_TIMEOUT);
            if (element.isEmpty()) {
                throw new IllegalStateException("Default locator not visible: " + locator);
            }
            LoggerUtil.info("✅ " + locator + " is displayed");
        }
    }

    public Optional<WebElement> waitForElement(By by, long timeout) {
        try {
            WebElement element = waitForElementToBeVisible(by, timeout);
            return Optional.of(element);
        } catch (Exception e) {
            LoggerUtil.warn("Element not found or not visible: " + by);
            return Optional.empty();
        }
    }
}
