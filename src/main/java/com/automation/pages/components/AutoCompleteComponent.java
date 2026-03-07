package com.automation.pages.components;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

public class AutoCompleteComponent extends BaseComponent {

    public AutoCompleteComponent(WebDriver driver) {
        super(driver);
    }

    public void typeAndSelectFirstOption(By inputField, By loadingIndicator, By optionLocator, String value) {
        enterText(inputField, value);

        waitForCondition(Boolean.TRUE, flag -> waitForElementToBeInvisible(loadingIndicator));

        waitForCondition(Boolean.TRUE, flag -> selectVisibleOption(optionLocator), DEFAULT_TIMEOUT, 1);
    }

    private Boolean selectVisibleOption(By optionLocator) {
        try {
            WebElement option = waitForElementToBeVisible(optionLocator, DEFAULT_TIMEOUT);
            option.click();
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}
