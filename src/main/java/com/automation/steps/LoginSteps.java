package com.automation.steps;

import com.automation.core.base.Constants;
import com.automation.core.config.ConfigReader;
import com.automation.core.logging.LoggerUtil;
import com.automation.pages.Login;
import io.cucumber.java.ParameterType;
import io.cucumber.java.en.And;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.testng.Assert;

public class LoginSteps {

    private final Login loginPage;

    public LoginSteps(Login loginPage) {
        this.loginPage = loginPage;
    }

    @ParameterType("username|password")
    public String getCredential(String key) {
        if (Constants.login.USERNAME.equals(key) || Constants.login.PASSWORD.equals(key)) {
            return ConfigReader.getProperty(key);
        }
        return ConfigReader.getProperty(key);
    }

    @When("User enters user name {getCredential}")
    @When("User enters user name {string}")
    public void userEntersUserName(String username) {
        LoggerUtil.info("Step: User enters username");
        loginPage.enterUsername(username);
    }

    @When("User enters password {getCredential}")
    @When("User enters password {string}")
    public void enterPassword(String password) {
        LoggerUtil.info("Step: User enters password");
        loginPage.enterPassword(password);
    }

    @And("User clicks login button")
    public void userClicksLoginButton() {
        LoggerUtil.info("Step: User clicks login button");
        loginPage.clickLoginButton();
    }

    @Then("Login successful")
    public void validateLogin() {
        LoggerUtil.info("Step: Validate login successful");
        Assert.assertTrue(loginPage.isLoginSuccessful(),
                "Assert failed as the login is not successful");
    }

    @Given("Login page loaded successfully")
    public void loginPageLoadedSuccessfully() {
        LoggerUtil.info("Step: Verify login page loaded");
        Assert.assertTrue(loginPage.isUserNameFieldLoaded(),
                "Assert failed as the login input fields are not loaded");
    }

    @Then("Login error displayed for invalid credentials")
    public void loginPageDisplaysError() {
        LoggerUtil.info("Step: Validate login error displayed");
        Assert.assertTrue(loginPage.isLoginErrorDisplayed(),
                "Assert failed as the login error not displayed");
    }

    @When("Login to application with {getCredential} and {getCredential}")
    public void login(String username, String password) {
        LoggerUtil.info("Helper: Performing login for user: " + username);
        loginPage.enterUsername(username);
        loginPage.enterPassword(password);
        loginPage.clickLoginButton();
        Assert.assertTrue(loginPage.isLoginSuccessful(), "Login failed for user: " + username);
    }
}
