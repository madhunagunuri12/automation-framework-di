package com.automation.pages;

import com.automation.core.base.Base;
import com.automation.core.driver.TestContext;
import org.openqa.selenium.By;

public class Navigation extends Base {
    private static final By HOME_ICON = By.cssSelector("img[alt='client brand banner']");
    private static final By ADMIN_TAB = By.xpath("//*[text()='Admin']/parent::a");

    private static final By[] DEFAULT_LOCATORS = {HOME_ICON};

    public Navigation(TestContext context) {
        super(context.getDriver());
    }

    public void clickAdminTab() {
        waitForElementToBeClickable(ADMIN_TAB).click();
    }

    public boolean isHomePageVisible() {
        return isElementDisplayed(HOME_ICON);
    }

    @Override
    protected By[] getDefaultLocators() {
        return DEFAULT_LOCATORS;
    }
}
