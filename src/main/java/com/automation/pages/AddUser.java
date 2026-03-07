package com.automation.pages;

import com.automation.core.base.Base;
import com.automation.core.driver.TestContext;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

public class AddUser extends Base {
    private static final By ADD_USER_HEADER = By.xpath("//*[text()='Add User']");
    private static final By USER_ROLE_DROPDOWN = By.xpath(
            "//label[text()='User Role']/parent::div/following-sibling::div//div[@class='oxd-select-wrapper']");
    private static final By EMPLOYEE_NAME_INPUT = By.xpath(
            "//label[text()='Employee Name']/parent::div/following-sibling::div//input");
    private static final By STATUS_DROPDOWN = By.xpath(
            "//label[text()='Status']/parent::div/following-sibling::div//div[@class='oxd-select-wrapper']");
    private static final By USERNAME_INPUT = By.xpath(
            "//label[text()='Username']/parent::div/following-sibling::div//input");
    private static final By PASSWORD_INPUT = By.xpath(
            "//label[text()='Password']/parent::div/following-sibling::div//input");
    private static final By CONFIRM_PASSWORD_INPUT = By.xpath(
            "//label[text()='Confirm Password']/parent::div/following-sibling::div//input");
    private static final By SAVE_BUTTON = By.xpath("//button[text()=' Save ']");
    private static final By CANCEL_BUTTON = By.xpath("//button[text()=' Cancel ']");
    private static final By EMPLOYEE_OPTION = By.cssSelector("div[role='option']");
    private static final By SEARCHING_LOADER = By.xpath("//*[text()='Searching....']");
    private static final By SUCCESS_TOAST_MESSAGE = By.cssSelector(
            ".oxd-toast-container--bottom div[class='oxd-toast oxd-toast--success oxd-toast-container--toast']");

    private static final By[] DEFAULT_LOCATORS = {ADD_USER_HEADER, USER_ROLE_DROPDOWN,
            USERNAME_INPUT, SAVE_BUTTON};

    public AddUser(TestContext context) {
        super(context.getDriver());
    }

    public boolean isPageLoaded() {
        return isElementDisplayed(ADD_USER_HEADER) && isElementDisplayed(SAVE_BUTTON);
    }

    public void selectUserRole(String role) {
        click(USER_ROLE_DROPDOWN);
        By roleOption = By.xpath("//div[@role='listbox']//*[text()='" + role + "']");
        click(roleOption);
    }

    public void enterEmployeeName(String employeeName) {
        enterText(EMPLOYEE_NAME_INPUT, employeeName);

        waitForCondition(Boolean.TRUE, value -> waitForElementToBeInvisible(SEARCHING_LOADER));

        waitForCondition(Boolean.TRUE, value -> waitForElement(EMPLOYEE_OPTION, DEFAULT_TIMEOUT)
                .map(new java.util.function.Function<WebElement, Boolean>() {
                    @Override
                    public Boolean apply(WebElement element) {
                        element.click();
                        return true;
                    }
                })
                .orElse(false), DEFAULT_TIMEOUT, 1);
    }

    public void selectStatus(String status) {
        click(STATUS_DROPDOWN);
        By statusOption = By.xpath("//div[@role='listbox']//*[text()='" + status + "']");
        click(statusOption);
    }

    public void enterUsername(String username) {
        enterText(USERNAME_INPUT, username);
    }

    public void enterPassword(String password) {
        enterText(PASSWORD_INPUT, password);
    }

    public void enterConfirmPassword(String password) {
        enterText(CONFIRM_PASSWORD_INPUT, password);
    }

    public void clickSave() {
        click(SAVE_BUTTON);
    }

    public void clickCancel() {
        click(CANCEL_BUTTON);
    }

    public boolean isSuccessMessageDisplayed() {
        return isElementDisplayed(SUCCESS_TOAST_MESSAGE, DEFAULT_TIMEOUT);
    }

    @Override
    protected By[] getDefaultLocators() {
        return DEFAULT_LOCATORS;
    }
}
