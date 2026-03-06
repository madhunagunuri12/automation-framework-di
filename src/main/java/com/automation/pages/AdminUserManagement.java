package com.automation.pages;

import com.automation.core.base.Base;
import com.automation.core.driver.TestContext;
import org.openqa.selenium.By;

public class AdminUserManagement extends Base {
    private static final By USER_MANAGEMENT_TAB = By.xpath("//*[text()='User Management ']/parent::li");
    private static final By ADD_USER_BUTTON = By.xpath("//button[text()=' Add ']");
    private static final By USERNAME_INPUT = By.xpath(
            "//label[text()='Username']/parent::div/following-sibling::div//input");
    private static final By SEARCH_BUTTON = By.xpath("//button[text()=' Search ']");
    private static final By RESET_BUTTON = By.xpath("//button[text()=' Reset ']");
    private static final String USER_ROW = "//div[@role='table']//div[text()='{{username}}']";
    private static final By[] DEFAULT_LOCATORS = {USER_MANAGEMENT_TAB};

    public AdminUserManagement(TestContext context) {
        super(context.getDriver());
    }

    public void clickAddUserButton() {
        waitForElementToBeClickable(ADD_USER_BUTTON).click();
    }

    public boolean isUserManagementTabVisible() {
        return isElementDisplayed(USER_MANAGEMENT_TAB);
    }
    
    public boolean isAddUserVisible() {
        return waitForCondition(true, ele -> {
            try {
                return isElementDisplayed(ADD_USER_BUTTON);
            } catch (Exception e) {
                return false;
            }
        }, 30, 2000);
    }

    public void searchByUsername(String username) {
        enterText(USERNAME_INPUT, username);
    }

    public void clickSearch() {
        click(SEARCH_BUTTON);
    }

    public void clickReset() {
        click(RESET_BUTTON);
    }

    public boolean isUserPresentInResults(String username) {
        By userRow = By.xpath(USER_ROW.replace("{{username}}", username));
        return isElementDisplayed(userRow);
    }

    @Override
    protected By[] getDefaultLocators() {
        return DEFAULT_LOCATORS;
    }
}
