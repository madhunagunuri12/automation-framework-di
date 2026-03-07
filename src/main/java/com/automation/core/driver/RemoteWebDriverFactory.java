package com.automation.core.driver;

import java.net.MalformedURLException;
import java.net.URI;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.remote.RemoteWebDriver;

public class RemoteWebDriverFactory implements WebDriverFactory {

    @Override
    public WebDriver create(BrowserType browserType, DriverConfig config) {
        try {
            switch (browserType) {
                case FIREFOX:
                    return new RemoteWebDriver(URI.create(config.getGridUrl()).toURL(), firefoxOptions(false));
                case HEADLESS_FIREFOX:
                    return new RemoteWebDriver(URI.create(config.getGridUrl()).toURL(), firefoxOptions(true));
                case EDGE:
                    throw new UnsupportedOperationException("Edge is not configured in Grid");
                case HEADLESS_CHROME:
                    return new RemoteWebDriver(URI.create(config.getGridUrl()).toURL(), chromeOptions(true));
                case CHROME:
                default:
                    return new RemoteWebDriver(URI.create(config.getGridUrl()).toURL(), chromeOptions(false));
            }
        } catch (MalformedURLException e) {
            throw new IllegalStateException("Invalid Selenium Grid URL: " + config.getGridUrl(), e);
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
            options.addArguments("--disable-gpu");
            options.addArguments("--remote-allow-origins=*");
            options.addArguments("--window-size=1920,1080");
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
