@login @FullRun
Feature: Selenium Automation login feature

  Scenario:Login to the my website test
    Given Login page loaded successfully
    When User enters user name username
    And User enters password password
    And User clicks login button
    Then Login successful

  Scenario: Login to the system with valid username and invalid password
    Given Login page loaded successfully
    When User enters user name "Admin"
    And User enters password "password"
    And User clicks login button
    Then Login error displayed for invalid credentials

  Scenario: Login to the system with invalid username and valid password
    Given Login page loaded successfully
    When User enters user name "Adminsys"
    And User enters password "admin123"
    And User clicks login button
    Then Login error displayed for invalid credentials

  Scenario: Login to the system with invalid username and invalid password
    Given Login page loaded successfully
    When User enters user name "Adminsys"
    And User enters password "admin@$145"
    And User clicks login button
    Then Login error displayed for invalid credentials


