package com.automation.core.base;

import com.automation.core.logging.LoggerUtil;

import java.util.Arrays;
import java.util.Optional;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

public abstract class Base extends SeleniumUtils {

    protected WebDriver driver;

    public Base(WebDriver driver) {
        super(driver);
        this.driver = driver;
        waitForPageToLoad();
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

    public Optional<WebElement> waitForElement(By by, long timeout) {
        try {
            WebElement element = waitForElementToBeVisible(by, timeout);
            return Optional.of(element);
        } catch (Exception e) {
            LoggerUtil.warn("Element not found or not visible: " + by);
            return Optional.empty();
        }
    }

    /**
     * Each page MUST provide default locators.
     */
    protected abstract By[] getDefaultLocators();

    /**
     * Common wait logic for all pages
     */
    private void waitForPageToLoad() {
        By[] locators = getDefaultLocators();

        Arrays.stream(locators).forEach(this::isElementDisplayed);
    }
}
