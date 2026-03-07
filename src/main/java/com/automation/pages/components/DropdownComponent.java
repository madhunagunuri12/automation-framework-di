package com.automation.pages.components;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;

public class DropdownComponent extends BaseComponent {

    public DropdownComponent(WebDriver driver) {
        super(driver);
    }

    public void selectOption(By dropdown, String optionText) {
        click(dropdown);
        By option = By.xpath("//div[@role='listbox']//*[text()='" + optionText + "']");
        click(option);
    }
}
