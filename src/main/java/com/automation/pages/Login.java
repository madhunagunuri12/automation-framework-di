package com.automation.pages;

import com.automation.core.base.Base;
import com.automation.core.driver.TestContext;
import org.openqa.selenium.By;

public class Login extends Base {

    private static final By LOGIN_PAGE = By.xpath("//*[text()='Login']");
    private static final By USERNAME = By.cssSelector("input[name='username']");
    private static final By PASSWORD = By.cssSelector("input[name='password']");
    private static final By LOGIN_BUTTON = By.cssSelector("button[type='submit']");
    private static final By DASHBOARD = By.xpath("//*[contains(@class,'header') and text()='Dashboard']");
    private static final By LOGIN_ERROR = By.xpath("//div[contains(@class,'error')]//*[text()='Invalid credentials']");

    private static final By[] DEFAULT_LOCATORS = {LOGIN_PAGE};

    public Login(TestContext context) {
        super(context.getDriver());
    }

    public void enterUsername(String username) {
        enterText(USERNAME, username, DEFAULT_TIMEOUT);
    }

    public void enterPassword(String password) {
        enterText(PASSWORD, password, DEFAULT_TIMEOUT);
    }

    public void clickLoginButton() {
        waitForElementToBeClickable(LOGIN_BUTTON).click();
    }

    public boolean isUserNameFieldLoaded() {
        return isElementDisplayed(USERNAME);
    }

    public boolean isLoginSuccessful() {
        return isElementDisplayed(DASHBOARD);
    }

    public boolean isLoginErrorDisplayed() {
        return isElementDisplayed(LOGIN_ERROR, DEFAULT_TIMEOUT);
    }

    @Override
    protected By[] getDefaultLocators() {
        return DEFAULT_LOCATORS;
    }
}
