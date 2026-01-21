Feature: Payment via DTU Pay
  Background: Bank account cleanup
    Given User with CPR "150390-2210" is not registered in the bank
    And User with CPR "150390-2210" is not registered in DTU Pay
    And User with CPR "200581-1234" is not registered in the bank
    And User with CPR "200581-1234" is not registered in DTU Pay


  Scenario: Successful Payment
    Given a user with name "Alice", last name "Ali", and CPR "150390-2210"
    And the user is registered with the bank with an initial balance of 1000 kr
    And the customer is registered with Simple DTU Pay using their bank account
    And a user with name "Bob", last name "Dylan", and CPR "200581-1234"
    And the user is registered with the bank with an initial balance of 0 kr
    And the merchant is registered with Simple DTU Pay using their bank account
    And the customer requests 5 tokens
    When the merchant initiates a payment for 10 kr by the customer
    Then the payment is successful
    And the balance of the customer at the bank is 990 kr
    And the balance of the merchant at the bank is 10 kr

  Scenario: Unsuccessful Payment Due to Invalid Token
    Given a user with name "Alice", last name "Ali", and CPR "150390-2210"
    And the user is registered with the bank with an initial balance of 1000 kr
    And the customer is registered with Simple DTU Pay using their bank account
    And a user with name "Bob", last name "Dylan", and CPR "200581-1234"
    And the user is registered with the bank with an initial balance of 0 kr
    And the merchant is registered with Simple DTU Pay using their bank account
    And the customer requests 5 tokens
    When the merchant initiates a payment for 10 kr by the customer with an invalid token
    Then the payment is unsuccessful
    And the balance of the customer at the bank is 1000 kr
    And the balance of the merchant at the bank is 0 kr

  Scenario: Unsuccessful Payment Due to Invalid MerchantId
    Given a user with name "Alice", last name "Ali", and CPR "150390-2210"
    And the user is registered with the bank with an initial balance of 1000 kr
    And the customer is registered with Simple DTU Pay using their bank account
    And a user with name "Bob", last name "Dylan", and CPR "200581-1234"
    And the user is registered with the bank with an initial balance of 0 kr
    And the merchant is registered with Simple DTU Pay using their bank account
    And the customer requests 5 tokens
    When the merchant initiates a payment for 10 kr by the customer with an invalid merchantId
    Then the payment is unsuccessful
    And the balance of the customer at the bank is 1000 kr
    And the balance of the merchant at the bank is 0 kr

    Scenario: Unsuccessful Payment Due to Insufficient balance
      Given a user with name "Alice", last name "Ali", and CPR "150390-2210"
      And the user is registered with the bank with an initial balance of 50 kr
      And the customer is registered with Simple DTU Pay using their bank account
      And a user with name "Bob", last name "Dylan", and CPR "200581-1234"
      And the user is registered with the bank with an initial balance of 0 kr
      And the merchant is registered with Simple DTU Pay using their bank account
      And the customer requests 5 tokens
      When the merchant initiates a payment for 100 kr by the customer
      Then the payment is unsuccessful
      And the balance of the customer at the bank is 50 kr
      And the balance of the merchant at the bank is 0 kr