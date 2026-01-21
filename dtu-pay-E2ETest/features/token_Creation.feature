Feature: Token management (E2E)
  Background: Bank account cleanup
    # Given User with CPR "472910-4728" is not registered in the bank
    # And User with CPR "591928-1235" is not registered in the bank
    Given User with CPR "472910-4728" is not registered in the bank
    And User with CPR "472910-4728" is not registered in DTU Pay
    And User with CPR "591928-1235" is not registered in the bank
    And User with CPR "591928-1235" is not registered in DTU Pay

  Scenario: Registered customer can request tokens
    Given a user with name "Poh", last name "Hob", and CPR "472910-4728"
    And the user is registered with the bank with an initial balance of 1000 kr
    And the customer is registered with Simple DTU Pay using their bank account
    When the customer requests 5 tokens
    Then the customer receives exactly 5 unique tokens

  Scenario: Registered customer requests tokens when already in possession of 2
    Given a user with name "Atlas", last name "Map", and CPR "591928-1235"
    And the user is registered with the bank with an initial balance of 1000 kr
    And the customer is registered with Simple DTU Pay using their bank account
    And the customer has 2 unused tokens
    When the customer requests 5 tokens
    Then the customer doesn't get tokens