Feature: Token management (E2E)
  Background: Bank account cleanup
    Given User with CPR "472910-4728" is not registered in the bank

  Scenario: Registered customer can request tokens
    Given a user with name "Poh", last name "Hob", and CPR "472910-4728"
    And the user is registered with the bank with an initial balance of 1000 kr
    And the customer is registered with Simple DTU Pay using their bank account
    When the customer requests 5 tokens
    Then the customer receives exactly 5 unique tokens
