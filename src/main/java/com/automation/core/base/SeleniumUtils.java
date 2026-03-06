package com.automation.core.base;

import com.automation.core.logging.LoggerUtil;
import java.time.Duration;
import java.util.function.Function;
import org.openqa.selenium.By;
import org.openqa.selenium.ElementClickInterceptedException;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.StaleElementReferenceException;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.FluentWait;
import org.openqa.selenium.support.ui.Wait;
import org.openqa.selenium.support.ui.WebDriverWait;

public class SeleniumUtils {

    protected static final long DEFAULT_TIMEOUT = 10; // Seconds
    protected static final long MIN_ELEMENT_LOAD_TIMEOUT = 5; // Seconds
    private static final long DEFAULT_POLLING_INTERVAL = 500; // Milliseconds
    private final WebDriver driver;

    public SeleniumUtils(WebDriver driver) {
        this.driver = driver;
    }

    /**
     * Waits for the page to be fully loaded (document.readyState = complete).
     *
     * @param timeout The maximum time to wait in seconds.
     */
    protected void waitForPageLoad(long timeout) {
        LoggerUtil.info("Waiting for page to load (document.readyState=complete)...");
        try {
            Wait<WebDriver> wait = new WebDriverWait(driver, Duration.ofSeconds(timeout));
            wait.until(webDriver -> ((JavascriptExecutor) webDriver)
                    .executeScript("return document.readyState").equals("complete"));
            LoggerUtil.info("Page loaded successfully.");
        } catch (Exception e) {
            LoggerUtil.warn("Page load timeout or error: " + e.getMessage());
        }
    }

    /**
     * Waits for the page to be fully loaded with default timeout.
     */
    protected void waitForPageLoad() {
        waitForPageLoad(DEFAULT_TIMEOUT);
    }

    /**
     * Waits for an element to be visible on the page.
     *
     * @param by      The locator for the element.
     * @param timeout The maximum time to wait for the element to be visible (in seconds).
     * @return The WebElement if it becomes visible, otherwise throws a TimeoutException.
     * @throws TimeoutException if the element is not visible within the specified timeout.
     */
    protected WebElement waitForElementToBeVisible(By by, long timeout) {
        LoggerUtil.info("Waiting for element to be visible: " + by + " (Timeout: " + timeout + "s)");
        try {
            WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(timeout));
            return wait.until(ExpectedConditions.visibilityOfElementLocated(by));
        } catch (TimeoutException e) {
            LoggerUtil.error("Element not visible within timeout: " + by);
            throw e;
        }
    }

    /**
     * Waits for an element to be visible on the page with default timeout.
     *
     * @param by The locator for the element.
     * @return The WebElement if it becomes visible, otherwise throws a TimeoutException.
     * @throws TimeoutException if the element is not visible within the default timeout.
     */
    protected WebElement waitForElementToBeVisible(By by) {
        return waitForElementToBeVisible(by, DEFAULT_TIMEOUT);
    }

    /**
     * Waits for an element to be invisible on the page.
     *
     * @param by      The locator for the element.
     * @param timeout The maximum time to wait for the element to be invisible (in seconds).
     * @return True if the element becomes invisible, otherwise throws a TimeoutException.
     * @throws TimeoutException if the element is still visible within the specified timeout.
     */
    protected boolean waitForElementToBeInvisible(By by, long timeout) {
        LoggerUtil.info("Waiting for element to be invisible: " + by + " (Timeout: " + timeout + "s)");
        try {
            WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(timeout));
            return wait.until(ExpectedConditions.invisibilityOfElementLocated(by));
        } catch (TimeoutException e) {
            LoggerUtil.error("Element still visible within timeout: " + by);
            throw e;
        }
    }

    /**
     * Waits for an element to be invisible on the page with default timeout.
     *
     * @param by The locator for the element.
     * @return True if the element becomes invisible, otherwise throws a TimeoutException.
     * @throws TimeoutException if the element is still visible within the default timeout.
     */
    protected boolean waitForElementToBeInvisible(By by) {
        return waitForElementToBeInvisible(by, DEFAULT_TIMEOUT);
    }

    /**
     * Waits for an element to be clickable on the page.
     *
     * @param by      The locator for the element.
     * @param timeout The maximum time to wait for the element to be clickable (in seconds).
     * @return The WebElement if it becomes clickable, otherwise throws a TimeoutException.
     * @throws TimeoutException if the element is not clickable within the specified timeout.
     */
    protected WebElement waitForElementToBeClickable(By by, long timeout) {
        LoggerUtil.info("Waiting for element to be clickable: " + by + " (Timeout: " + timeout + "s)");
        try {
            WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(timeout));
            return wait.until(ExpectedConditions.elementToBeClickable(by));
        } catch (TimeoutException e) {
            LoggerUtil.error("Element not clickable within timeout: " + by);
            throw e;
        }
    }

    /**
     * Waits for an element to be clickable on the page with default timeout.
     *
     * @param by The locator for the element.
     * @return The WebElement if it becomes clickable, otherwise throws a TimeoutException.
     * @throws TimeoutException if the element is not clickable within the default timeout.
     */
    protected WebElement waitForElementToBeClickable(By by) {
        return waitForElementToBeClickable(by, DEFAULT_TIMEOUT);
    }

    /**
     * Enters text into a text field.
     *
     * @param by      The locator for the text field.
     * @param text    The text to enter.
     * @param timeout The maximum time to wait for the element to be visible (in seconds).
     * @throws TimeoutException if the element is not visible within the specified timeout.
     */
    protected void enterText(By by, String text, long timeout) {
        LoggerUtil.info("Entering text '" + text + "' into element: " + by);
        WebElement element = waitForElementToBeVisible(by, timeout);
        element.clear();
        element.sendKeys(text);
    }

    /**
     * Enters text into a text field with default timeout.
     *
     * @param by   The locator for the text field.
     * @param text The text to enter.
     * @throws TimeoutException if the element is not visible within the default timeout.
     */
    protected void enterText(By by, String text) {
        enterText(by, text, DEFAULT_TIMEOUT);
    }

    /**
     * Clears the text from a text field.
     *
     * @param by      The locator for the text field.
     * @param timeout The maximum time to wait for the element to be visible (in seconds).
     * @throws TimeoutException if the element is not visible within the specified timeout.
     */
    protected void clearText(By by, long timeout) {
        LoggerUtil.info("Clearing text from element: " + by);
        WebElement element = waitForElementToBeVisible(by, timeout);
        element.clear();
    }

    /**
     * Clears the text from a text field with default timeout.
     *
     * @param by The locator for the text field.
     * @throws TimeoutException if the element is not visible within the default timeout.
     */
    protected void clearText(By by) {
        clearText(by, DEFAULT_TIMEOUT);
    }

    /**
     * Gets the text of an element.
     *
     * @param by      The locator for the element.
     * @param timeout The maximum time to wait for the element to be visible (in seconds).
     * @return The text of the element.
     * @throws TimeoutException if the element is not visible within the specified timeout.
     */
    protected String getText(By by, long timeout) {
        LoggerUtil.info("Getting text from element: " + by);
        WebElement element = waitForElementToBeVisible(by, timeout);
        String text = element.getText();
        LoggerUtil.info("Retrieved text: '" + text + "'");
        return text;
    }

    /**
     * Gets the text of an element with default timeout.
     *
     * @param by The locator for the element.
     * @return The text of the element.
     * @throws TimeoutException if the element is not visible within the default timeout.
     */
    protected String getText(By by) {
        return getText(by, DEFAULT_TIMEOUT);
    }

    /**
     * Clicks on an element.
     *
     * @param by      The locator for the element.
     * @param timeout The maximum time to wait for the element to be clickable (in seconds).
     * @throws TimeoutException if the element is not clickable within the specified timeout.
     */
    protected void click(By by, long timeout) {
        LoggerUtil.info("Clicking on element: " + by);
        try {
            WebElement element = waitForElementToBeClickable(by, timeout);
            element.click();
        } catch (TimeoutException | ElementClickInterceptedException e) {
            LoggerUtil.warn("Standard click failed. Falling back to JavaScript click for: " + by);
            WebElement element = waitForElementToBeVisible(by, timeout);
            ((JavascriptExecutor) driver).executeScript("arguments[0].click();", element);
        }
    }

    /**
     * Clicks on an element with default timeout.
     *
     * @param by The locator for the element.
     * @throws TimeoutException if the element is not clickable within the default timeout.
     */
    protected void click(By by) {
        click(by, DEFAULT_TIMEOUT);
    }

    /**
     * Checks if an element is displayed.
     *
     * @param by      The locator for the element.
     * @param timeout The maximum time to wait for the element to be present (in seconds).
     * @return True if the element is displayed, false otherwise.
     */
    protected boolean isElementDisplayed(By by, long timeout) {
        LoggerUtil.info("Checking if element is displayed: " + by);
        try {
            return waitForCondition(by, locator -> {
                try {
                    WebElement element = driver.findElement(locator);
                    return element.isDisplayed();
                } catch (NoSuchElementException | StaleElementReferenceException e) {
                    return false;
                }
            }, timeout, DEFAULT_POLLING_INTERVAL);
        } catch (TimeoutException e) {
            LoggerUtil.warn("Element not displayed (Timeout): " + by);
            return false;
        }
    }

    /**
     * Checks if an element is displayed with default timeout.
     *
     * @param by The locator for the element.
     * @return True if the element is displayed, false otherwise.
     */
    protected boolean isElementDisplayed(By by) {
        return isElementDisplayed(by, DEFAULT_TIMEOUT);
    }

    /**
     * Fluent wait for element to be visible.
     *
     * @param by      The locator for the element.
     * @param timeout The maximum time to wait for the element to be visible (in seconds).
     * @return The WebElement if it becomes visible.
     * @throws TimeoutException if the element is not visible within the specified timeout.
     */
    protected WebElement fluentWaitForElementToBeVisible(By by, long timeout) {
        LoggerUtil.info("Fluent wait for element: " + by);
        Wait<WebDriver> wait = new FluentWait<>(driver)
                .withTimeout(Duration.ofSeconds(timeout))
                .pollingEvery(Duration.ofMillis(DEFAULT_POLLING_INTERVAL))
                .ignoring(NoSuchElementException.class)
                .ignoring(StaleElementReferenceException.class);

        return wait.until(driver -> driver.findElement(by));
    }

    /**
     * Fluent wait for element to be visible with default timeout.
     *
     * @param by The locator for the element.
     * @return The WebElement if it becomes visible.
     * @throws TimeoutException if the element is not visible within the default timeout.
     */
    protected WebElement fluentWaitForElementToBeVisible(By by) {
        return fluentWaitForElementToBeVisible(by, DEFAULT_TIMEOUT);
    }

    /**
     * Waits for a specific condition to be met using a custom function (lambda).
     *
     * @param object          The object to be used in the condition (e.g., WebElement, By, etc.).
     * @param condition       The function (lambda) that defines the condition to wait for.
     *                        It takes the object as input and returns a Boolean indicating
     *                        whether the condition is met (true) or not (false).
     * @param timeout         The maximum time to wait for the condition to be met (in seconds).
     * @param pollingInterval The frequency at which to check the condition (in milliseconds).
     * @param <T>             The type of the object.
     * @param <R>             The type of the result returned by the condition function.
     * @return The result of the condition function when it returns true.
     * @throws TimeoutException if the condition is not met within the specified timeout.
     */
    protected <T, R> R waitForCondition(T object, Function<T, R> condition, long timeout, long pollingInterval) {
        LoggerUtil.info("Waiting for custom condition on object: " + object);
        Wait<WebDriver> wait = new FluentWait<>(driver)
                .withTimeout(Duration.ofSeconds(timeout))
                .pollingEvery(Duration.ofMillis(pollingInterval))
                .ignoring(NoSuchElementException.class)
                .ignoring(StaleElementReferenceException.class);

        return wait.until(driver -> condition.apply(object));
    }

    /**
     * Waits for a specific condition to be met using a custom function (lambda) with default timeout.
     *
     * @param object    The object to be used in the condition (e.g., WebElement, By, etc.).
     * @param condition The function (lambda) that defines the condition to wait for.
     *                  It takes the object as input and returns a Boolean indicating
     *                  whether the condition is met (true) or not (false).
     * @param <T>       The type of the object.
     * @param <R>       The type of the result returned by the condition function.
     * @return The result of the condition function when it returns true.
     * @throws TimeoutException if the condition is not met within the default timeout.
     */
    protected <T, R> R waitForCondition(T object, Function<T, R> condition) {
        return waitForCondition(object, condition, DEFAULT_TIMEOUT, DEFAULT_POLLING_INTERVAL);
    }

    /**
     * Checks if an element is enabled.
     *
     * @param by      The locator for the element.
     * @param timeout The maximum time to wait for the element to be present (in seconds).
     * @return True if the element is enabled, false otherwise.
     */
    protected boolean isElementEnabled(By by, long timeout) {
        LoggerUtil.info("Checking if element is enabled: " + by);
        try {
            WebElement element = waitForElementToBeVisible(by, timeout);
            boolean isEnabled = element.isEnabled();
            LoggerUtil.info("Element enabled: " + isEnabled);
            return isEnabled;
        } catch (TimeoutException e) {
            LoggerUtil.warn("Element not enabled (Timeout): " + by);
            return false;
        }
    }

    /**
     * Checks if an element is enabled with default timeout.
     *
     * @param by The locator for the element.
     * @return True if the element is enabled, false otherwise.
     */
    protected boolean isElementEnabled(By by) {
        return isElementEnabled(by, DEFAULT_TIMEOUT);
    }

    /**
     * Performs click using Actions class.
     *
     * @param element The locator for the element.
     */
    protected void clickAction(By element) {
        LoggerUtil.info("Performing Action Click on: " + element);
        Actions action = new Actions(driver);
        WebElement webElement = driver.findElement(element);
        action.moveToElement(webElement).click(webElement).build().perform();
    }
}
