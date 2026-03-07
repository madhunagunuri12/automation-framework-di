package com.automation.core.driver;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.edge.EdgeDriver;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;

public class LocalWebDriverFactory implements WebDriverFactory {

    @Override
    public WebDriver create(BrowserType browserType, DriverConfig config) {
        switch (browserType) {
            case FIREFOX:
                return new FirefoxDriver();
            case HEADLESS_FIREFOX:
                return new FirefoxDriver(firefoxOptions(true));
            case EDGE:
                return new EdgeDriver();
            case HEADLESS_CHROME:
                return new ChromeDriver(chromeOptions(true));
            case CHROME:
            default:
                return new ChromeDriver(chromeOptions(false));
        }
    }

    private ChromeOptions chromeOptions(boolean headless) {
        ChromeOptions options = new ChromeOptions();
        options.addArguments("--ignore-certificate-errors");
        options.addArguments("--incognito");

        if (headless) {
            options.addArguments("--headless=new");
            options.addArguments("--no-sandbox");
            options.addArguments("--disable-dev-shm-usage");
        }

        return options;
    }

    private FirefoxOptions firefoxOptions(boolean headless) {
        FirefoxOptions options = new FirefoxOptions();
        if (headless) {
            options.addArguments("--headless");
        }
        return options;
    }
}
