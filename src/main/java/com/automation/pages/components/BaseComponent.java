package com.automation.pages.components;

import com.automation.core.base.SeleniumUtils;
import org.openqa.selenium.WebDriver;

public abstract class BaseComponent extends SeleniumUtils {

    protected BaseComponent(WebDriver driver) {
        super(driver);
    }
}
