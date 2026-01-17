Feature: Account creation of a user in DTU pay

  Scenario: Successful user account creation
    Given a user with name "Alice", last name "Ali", and CPR "150390-221"
    And the user is registered with the bank with an initial balance of 1000 kr
    When the user is registered with Simple DTU Pay using their bank account
    Then the DTU Pay registration is successful
    And a non-empty string user id is returned
