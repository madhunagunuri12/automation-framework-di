package com.automation.steps;

import com.automation.core.logging.LoggerUtil;
import com.automation.pages.AdminUserManagement;
import io.cucumber.datatable.DataTable;
import io.cucumber.java.en.And;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.testng.Assert;

public class UserManagementSteps {

    private final AdminUserManagement adminUserManagementPage;

    public UserManagementSteps(AdminUserManagement adminUserManagementPage) {
        this.adminUserManagementPage = adminUserManagementPage;
    }

    @And("User clicks on Add button")
    public void userClicksOnAddButton() {
        LoggerUtil.info("Step: User clicks on Add button");
        adminUserManagementPage.clickAddUserButton();
    }

    @When("Search user {string} in user management")
    public void searchUserInUserManagement(String username) {
        LoggerUtil.info("Step: Search user in user management: " + username);
        adminUserManagementPage.clickReset();
        adminUserManagementPage.searchByUsername(username);
        adminUserManagementPage.clickSearch();
    }

    @Then("User {string} is in the user management list")
    public void userShouldBeVisibleInTheUserManagementList(String username) {
        LoggerUtil.info("Step: Verify user is visible in results: " + username);
        Assert.assertTrue(adminUserManagementPage.isUserPresentInResults(username),
                "User " + username + " was not found in the search results.");
    }

    @And("Search users in user management list")
    public void searchUsersInUserManagementList(DataTable dataTable) {
        LoggerUtil.info("Step: Search users in user management list");

        dataTable.asMaps(String.class, String.class)
                .stream()
                .map(row -> row.get("UserName"))
                .forEach(username -> {
                    searchUserInUserManagement(username);
                    userShouldBeVisibleInTheUserManagementList(username);
                });
    }
}
