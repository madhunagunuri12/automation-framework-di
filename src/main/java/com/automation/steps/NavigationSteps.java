package com.automation.steps;

import com.automation.core.logging.LoggerUtil;
import com.automation.pages.AdminUserManagement;
import com.automation.pages.Navigation;
import io.cucumber.java.en.When;
import org.testng.Assert;

public class NavigationSteps {

    private final Navigation navigationPage;
    private final AdminUserManagement adminUserManagementPage;

    public NavigationSteps(Navigation navigationPage, AdminUserManagement adminUserManagementPage) {
        this.navigationPage = navigationPage;
        this.adminUserManagementPage = adminUserManagementPage;
    }

    @When("User navigates to Admin tab")
    public void userNavigatesToAdminTab() {
        LoggerUtil.info("Step: User navigates to Admin tab");
        navigationPage.clickAdminTab();
        Assert.assertTrue(adminUserManagementPage.isUserManagementTabVisible(),
                "Admin page / User Management tab is not visible after navigation.");
    }
}
