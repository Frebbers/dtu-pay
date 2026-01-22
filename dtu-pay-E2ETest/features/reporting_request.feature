Feature: Reporting request
  Background: Bank account cleanup
    Given the global payment history is empty
    And User with CPR "123456-7890" is not registered in the bank
    And User with CPR "123456-7890" is not registered in DTU Pay
    And User with CPR "112233-4455" is not registered in the bank
    And User with CPR "112233-4455" is not registered in DTU Pay
    And User with CPR "098765-4321" is not registered in the bank
    And User with CPR "098765-4321" is not registered in DTU Pay
    And User with CPR "010101-2323" is not registered in the bank
    And User with CPR "010101-2323" is not registered in DTU Pay
    Given a user with name "John", last name "Snow", and CPR "123456-7890"
    And the user is registered with the bank with an initial balance of 1000 kr
    And the customer is registered with Simple DTU Pay using their bank account
    And the customer with id "123456-7890" has 3 unused tokens
    Given a user with name "Second", last name "Customer", and CPR "112233-4455"
    And the user is registered with the bank with an initial balance of 1000 kr
    And the customer is registered with Simple DTU Pay using their bank account
    And the customer with id "112233-4455" has 3 unused tokens
    Given a user with name "Shop", last name "Owner", and CPR "098765-4321"
    And the user is registered with the bank with an initial balance of 1000 kr
    And the merchant is registered with Simple DTU Pay using their bank account
    Given a user with name "Second", last name "Merchant", and CPR "010101-2323"
    And the user is registered with the bank with an initial balance of 1000 kr
    And the merchant is registered with Simple DTU Pay using their bank account
    When the merchant with id "098765-4321" initiates a payment of 100 kr by the customer with id "123456-7890" using the token
    And the payment is successful
    When the merchant with id "098765-4321" initiates a payment of 200 kr by the customer with id "123456-7890" using the token
    And the payment is successful
    When the merchant with id "010101-2323" initiates a payment of 500 kr by the customer with id "123456-7890" using the token
    And the payment is successful
    When the merchant with id "098765-4321" initiates a payment of 300 kr by the customer with id "112233-4455" using the token
    And the payment is successful

  Scenario: Verify all reports (Customer, Merchant, Manager)
    Then the customer with id "123456-7890" requests the report
    And the customer report contains 3 payments
    And the report contains a payment of 100 kr to the merchant with id "098765-4321"
    And the report contains a payment of 200 kr to the merchant with id "098765-4321"
    And the report contains a payment of 500 kr to the merchant with id "010101-2323"
    Then the merchant with id "098765-4321" requests the report
    And the merchant report contains 3 payments
    And the report contains a payment of 100 kr
    And the report contains a payment of 200 kr
    And the report contains a payment of 300 kr
    And the report does not contain the customer details
    Then the manager requests the report
    And the manager report contains 4 payments
    And the report contains a summary of total transfers amount equal to 1100 kr