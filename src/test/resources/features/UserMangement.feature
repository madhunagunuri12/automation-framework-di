@FullRun
Feature: User Management > Add user > Search user > Edit user > Delete user

  @user-management
  Scenario: Create a new user
    Given Login to application with username and password
    When User navigates to Admin tab
    And User clicks on Add button
    And User enters Employee Name "f"
    And User enters Username "{%cucumber-get-random-num:Adam%}"
    And User selects User Role "Admin"
    And User selects Status "Enabled"
    And User enters Password "admin12345"
    And User enters Confirm Password "admin12345"
    And User clicks Save button
    Then New user created successfully
    And Redirected to user management page
    When Search user "{*UserName*}" in user management
    Then User "{*UserName*}" is in the user management list
    And Search users in user management list
      | UserName     |
      | {*UserName*} |
      | Admin        |
