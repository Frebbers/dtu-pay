Feature: Payment Service Feature
  Scenario: Payment request processed successfully scenario
    Given a "PaymentRequested" event is received
    And the customerId is fetched via TokenService
    And the customerBankAccNum is fetched via AccountService
    And the merchantBankAccNum is fetched via AccountService
    When the bank processes the payment successfully
    Then the payment request is processed successfully
