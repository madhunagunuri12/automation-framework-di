package com.automation.utilities.js;

import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

public final class JSUtil {

    private JSUtil() {
        throw new UnsupportedOperationException("Utility class");
    }

    private static JavascriptExecutor getExecutor(WebDriver driver) {
        return (JavascriptExecutor) driver;
    }


    /* =========================
       CLICK
       ========================= */

    public static void click(WebDriver driver,
                             WebElement element) {

        getExecutor(driver)
                .executeScript("arguments[0].click();",
                        element);
    }


    /* =========================
       SCROLL
       ========================= */

    public static void scrollToElement(WebDriver driver,
                                       WebElement element) {

        getExecutor(driver)
                .executeScript(
                        "arguments[0].scrollIntoView(true);",
                        element);
    }

    public static void scrollBy(WebDriver driver,
                                int x,
                                int y) {

        getExecutor(driver)
                .executeScript(
                        "window.scrollBy(arguments[0], arguments[1]);",
                        x, y);
    }


    /* =========================
       SET VALUE
       ========================= */

    public static void setValue(WebDriver driver,
                                WebElement element,
                                String value) {

        getExecutor(driver)
                .executeScript(
                        "arguments[0].value=arguments[1];",
                        element, value);
    }


    /* =========================
       HIGHLIGHT
       ========================= */

    public static void highlight(WebDriver driver,
                                 WebElement element) {

        getExecutor(driver)
                .executeScript(
                        "arguments[0].style.border='3px solid red'",
                        element);
    }


    /* =========================
       GET VALUE
       ========================= */

    public static String getValue(WebDriver driver,
                                  WebElement element) {

        return (String) getExecutor(driver)
                .executeScript(
                        "return arguments[0].value;",
                        element);
    }
}
