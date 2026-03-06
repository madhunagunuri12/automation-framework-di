package com.automation.core.driver;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.edge.EdgeDriver;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.remote.RemoteWebDriver;

import java.net.MalformedURLException;
import java.net.URI;

public class DriverManager {

    private WebDriver driver;

    public WebDriver getDriver() {
        if (driver == null) {
            driver = initDriver();
        }
        return driver;
    }

    private WebDriver initDriver() {

        String execution = System.getProperty("execution", "local");
        String browser = System.getProperty("browser", "chrome");

        WebDriver webDriver;

        if ("remote".equalsIgnoreCase(execution)) {
            webDriver = createRemoteDriver(browser);
        } else {
            webDriver = createLocalDriver(browser);
        }

        webDriver.manage().window().maximize();
        return webDriver;
    }

    // =========================
    // Local Driver Creation
    // =========================

    private WebDriver createLocalDriver(String browser) {

        return switch (browser.toLowerCase()) {

            case "firefox" -> new FirefoxDriver();

            case "headless-firefox" -> {
                FirefoxOptions options = new FirefoxOptions();
                options.addArguments("--headless");
                yield new FirefoxDriver(options);
            }

            case "edge" -> new EdgeDriver();

            case "headless-chrome" -> {
                ChromeOptions options = getChromeOptions();
                options.addArguments("--headless=new");
                options.addArguments("--no-sandbox");
                options.addArguments("--disable-dev-shm-usage");
                yield new ChromeDriver(options);
            }

            default -> {
                ChromeOptions options = getChromeOptions();
                yield new ChromeDriver(options);
            }
        };
    }

    // =========================
    // Remote Driver Creation
    // =========================

    private WebDriver createRemoteDriver(String browser) {

        try {

            String gridUrl = System.getProperty(
                    "grid.url",
                    "http://host.docker.internal:4444/wd/hub"
            );

            return switch (browser.toLowerCase()) {

                case "firefox" -> new RemoteWebDriver(
                        URI.create(gridUrl).toURL(),
                        new FirefoxOptions()
                );

                case "headless-firefox" -> {
                    FirefoxOptions options = new FirefoxOptions();
                    options.addArguments("--headless");
                    yield new RemoteWebDriver(URI.create(gridUrl).toURL(), options);
                }

                case "edge" -> throw new UnsupportedOperationException("Edge not configured in Grid");

                case "headless-chrome" -> {
                    ChromeOptions options = new ChromeOptions();

                    options.addArguments("--headless=new");
                    options.addArguments("--no-sandbox");
                    options.addArguments("--disable-dev-shm-usage");
                    options.addArguments("--disable-gpu");
                    options.addArguments("--remote-allow-origins=*");
                    options.addArguments("--window-size=1920,1080");
                    yield new RemoteWebDriver(URI.create(gridUrl).toURL(), options);
                }

                default -> new RemoteWebDriver(
                        URI.create(gridUrl).toURL(),
                        getChromeOptions()
                );
            };

        } catch (MalformedURLException e) {
            throw new RuntimeException("Failed to create RemoteWebDriver", e);
        }
    }

    private ChromeOptions getChromeOptions() {
        ChromeOptions options = new ChromeOptions();
        options.addArguments("--ignore-certificate-errors");
        options.addArguments("--incognito");
        return options;
    }

    public void quitDriver() {
        if (driver != null) {
            driver.quit();
            driver = null;
        }
    }
}
