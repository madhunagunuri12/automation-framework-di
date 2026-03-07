package com.automation.steps;

import com.automation.pages.AddUser;
import com.automation.pages.AdminUserManagement;
import com.automation.transformers.TestDataStore;
import com.automation.core.logging.LoggerUtil;
import io.cucumber.java.en.And;
import io.cucumber.java.en.Then;
import org.testng.Assert;

public class AddNewUserSteps {

    private final AddUser addUserPage;
    private final AdminUserManagement adminUserManagementPage;

    public AddNewUserSteps(AddUser addUserPage, AdminUserManagement adminUserManagementPage) {
        this.addUserPage = addUserPage;
        this.adminUserManagementPage = adminUserManagementPage;
    }

    @Then("Add User page is displayed")
    public void addUserPageIsDisplayed() {
        LoggerUtil.info("Step: Verify Add User page is displayed");
        Assert.assertTrue(addUserPage.isPageLoaded(),
                "Add User page header or mandatory fields are not visible.");
    }

    @And("User selects User Role {string}")
    public void userSelectsUserRole(String role) {
        LoggerUtil.info("Step: User selects User Role: " + role);
        addUserPage.selectUserRole(role);
    }

    @And("User enters Employee Name {string}")
    public void userEntersEmployeeName(String employeeName) {
        LoggerUtil.info("Step: User enters Employee Name: " + employeeName);
        addUserPage.enterEmployeeName(employeeName);
    }

    @And("User selects Status {string}")
    public void userSelectsStatus(String status) {
        LoggerUtil.info("Step: User selects Status: " + status);
        addUserPage.selectStatus(status);
    }

    @And("User enters Username {string}")
    public void userEntersUsername(String username) {
        LoggerUtil.info("Step: User enters Username: " + username);
        addUserPage.enterUsername(username);
        TestDataStore.put("UserName", username);
    }

    @And("User enters Password {string}")
    public void userEntersPassword(String password) {
        LoggerUtil.info("Step: User enters Password");
        addUserPage.enterPassword(password);
    }

    @And("User enters Confirm Password {string}")
    public void userEntersConfirmPassword(String password) {
        LoggerUtil.info("Step: User enters Confirm Password");
        addUserPage.enterConfirmPassword(password);
    }

    @And("User clicks Save button")
    public void userClicksSaveButton() {
        LoggerUtil.info("Step: User clicks Save button");
        addUserPage.clickSave();
    }

    @Then("Success message is displayed")
    @Then("New user created successfully")
    public void successMessageIsDisplayed() {
        LoggerUtil.info("Step: Verify success message is displayed");
        Assert.assertTrue(addUserPage.isSuccessMessageDisplayed(),
                "Success toast message was not displayed.");
    }

    @And("Redirected to user management page")
    public void redirectedToUserManagementPage() {
        LoggerUtil.info("Step: Verify redirected to user management page");
        Assert.assertTrue(adminUserManagementPage.isAddUserVisible(),
                "User Management tab is not visible after adding a new user.");
    }
}
