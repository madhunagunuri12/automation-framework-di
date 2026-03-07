package com.automation.actions;

import com.automation.pages.AdminUserManagement;
import com.automation.pages.Navigation;

public class AdminNavigationActions {

    private final Navigation navigationPage;
    private final AdminUserManagement adminUserManagementPage;

    public AdminNavigationActions(Navigation navigationPage, AdminUserManagement adminUserManagementPage) {
        this.navigationPage = navigationPage;
        this.adminUserManagementPage = adminUserManagementPage;
    }

    public void navigateToAdminSection() {
        navigationPage.clickAdminTab();
    }

    public boolean isAdminSectionVisible() {
        return adminUserManagementPage.isUserManagementTabVisible();
    }
}
