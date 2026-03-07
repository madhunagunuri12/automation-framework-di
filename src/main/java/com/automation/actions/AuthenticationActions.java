package com.automation.actions;

import com.automation.pages.Login;

public class AuthenticationActions {

    private final Login loginPage;

    public AuthenticationActions(Login loginPage) {
        this.loginPage = loginPage;
    }

    public void login(String username, String password) {
        loginPage.enterUsername(username);
        loginPage.enterPassword(password);
        loginPage.clickLoginButton();
    }

    public boolean isLoginSuccessful() {
        return loginPage.isLoginSuccessful();
    }

    public boolean isLoginPageLoaded() {
        return loginPage.isUserNameFieldLoaded();
    }

    public boolean isLoginErrorDisplayed() {
        return loginPage.isLoginErrorDisplayed();
    }

    public void enterUsername(String username) {
        loginPage.enterUsername(username);
    }

    public void enterPassword(String password) {
        loginPage.enterPassword(password);
    }

    public void clickLoginButton() {
        loginPage.clickLoginButton();
    }
}
