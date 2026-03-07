package com.automation.steps;

import com.automation.actions.AdminNavigationActions;
import com.automation.core.logging.LoggerUtil;
import io.cucumber.java.en.When;
import org.testng.Assert;

public class NavigationSteps {

    private final AdminNavigationActions adminNavigationActions;

    public NavigationSteps(AdminNavigationActions adminNavigationActions) {
        this.adminNavigationActions = adminNavigationActions;
    }

    @When("User navigates to Admin tab")
    public void userNavigatesToAdminTab() {
        LoggerUtil.info("Step: User navigates to Admin tab");
        adminNavigationActions.navigateToAdminSection();
        Assert.assertTrue(adminNavigationActions.isAdminSectionVisible(),
                "Admin page / User Management tab is not visible after navigation.");
    }
}
