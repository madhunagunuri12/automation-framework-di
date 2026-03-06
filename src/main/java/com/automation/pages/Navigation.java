package com.automation.pages;

import com.automation.core.base.Base;
import com.automation.core.driver.TestContext;
import java.util.List;
import org.openqa.selenium.By;

public class Navigation extends Base {
    private static final By HOME_ICON = By.cssSelector("img[alt='client brand banner']");
    private static final By ADMIN_TAB = By.xpath("//*[text()='Admin']/parent::a");

    private static final List<By> DEFAULT_LOCATORS = List.of(HOME_ICON);

    public Navigation(TestContext context) {
        super(context.getDriver(), DEFAULT_LOCATORS);
    }

    public void clickAdminTab() {
        waitForElementToBeClickable(ADMIN_TAB).click();
    }

}
