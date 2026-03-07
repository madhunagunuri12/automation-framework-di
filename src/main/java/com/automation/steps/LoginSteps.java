package com.automation.steps;

import com.automation.actions.AuthenticationActions;
import com.automation.core.base.Constants;
import com.automation.core.config.ConfigReader;
import com.automation.core.logging.LoggerUtil;
import io.cucumber.java.ParameterType;
import io.cucumber.java.en.And;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import java.util.Arrays;
import org.testng.Assert;

public class LoginSteps {

    private final AuthenticationActions authenticationActions;

    public LoginSteps(AuthenticationActions authenticationActions) {
        this.authenticationActions = authenticationActions;
    }

    @ParameterType("username|password")
    public String getCredential(String key) {
        boolean allowedCredential = Arrays.asList(Constants.login.USERNAME, Constants.login.PASSWORD)
                .contains(key);

        if (!allowedCredential) {
            throw new IllegalArgumentException("Unsupported credential key: " + key);
        }

        return ConfigReader.getRequiredProperty(key);
    }

    @When("User enters user name {getCredential}")
    @When("User enters user name {string}")
    public void userEntersUserName(String username) {
        LoggerUtil.info("Step: User enters username");
        authenticationActions.enterUsername(username);
    }

    @When("User enters password {getCredential}")
    @When("User enters password {string}")
    public void enterPassword(String password) {
        LoggerUtil.info("Step: User enters password");
        authenticationActions.enterPassword(password);
    }

    @And("User clicks login button")
    public void userClicksLoginButton() {
        LoggerUtil.info("Step: User clicks login button");
        authenticationActions.clickLoginButton();
    }

    @Then("Login successful")
    public void validateLogin() {
        LoggerUtil.info("Step: Validate login successful");
        Assert.assertTrue(authenticationActions.isLoginSuccessful(),
                "Assert failed as the login is not successful");
    }

    @Given("Login page loaded successfully")
    public void loginPageLoadedSuccessfully() {
        LoggerUtil.info("Step: Verify login page loaded");
        Assert.assertTrue(authenticationActions.isLoginPageLoaded(),
                "Assert failed as the login input fields are not loaded");
    }

    @Then("Login error displayed for invalid credentials")
    public void loginPageDisplaysError() {
        LoggerUtil.info("Step: Validate login error displayed");
        Assert.assertTrue(authenticationActions.isLoginErrorDisplayed(),
                "Assert failed as the login error not displayed");
    }

    @When("Login to application with {getCredential} and {getCredential}")
    public void login(String username, String password) {
        LoggerUtil.info("Helper: Performing login for user: " + username);
        authenticationActions.login(username, password);
        Assert.assertTrue(authenticationActions.isLoginSuccessful(), "Login failed for user: " + username);
    }
}
