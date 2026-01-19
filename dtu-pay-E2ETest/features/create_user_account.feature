Feature: Account creation of a user in DTU pay
  Background: Bank account cleanup
    Given User with CPR "150390-2210" is not registered in the bank
    And User with CPR "200581-1234" is not registered in the bank
    And User with CPR "101070-5678" is not registered in the bank

  Scenario: Successful customer account creation
    Given a customer with name "Alice", last name "Ali", and CPR "150390-2210"
    And the customer is registered with the bank with an initial balance of 1000 kr
    And the customer is registered with Simple DTU Pay using their bank account
    Then the DTU Pay registration is successful
    And a non-empty string user id is returned

  Scenario: Successful Merchant account creation
    Given a merchant with name "Bob", last name "Dylan", and CPR "200581-1234"
    And the merchant is registered with the bank with an initial balance of 0 kr
    And the merchant is registered with Simple DTU Pay using their bank account
    Then the DTU Pay registration is successful
    And a non-empty string user id is returned

  Scenario: Customer account already exists
    Given a customer with name "Charlie", last name "Chaplin", and CPR "101070-5678"
    And the customer is registered with the bank with an initial balance of 500 kr
    And the customer is registered with Simple DTU Pay using their bank account
    When the customer attempts to register again with Simple DTU Pay using the same bank account
    Then the DTU Pay registration fails with error "User already exists"

