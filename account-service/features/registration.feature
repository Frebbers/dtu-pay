Feature: Registration

  Scenario: Customer registration
    Given the user is on the registration page
    When the user enters valid registration details
    And submits the registration form
    Then the user should see a confirmation message

  Scenario: Merchant registration
    