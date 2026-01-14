Feature: Registration

  Scenario: User registration with valid details
    Given a user with firstname "Alice" and lastname "Doe" and account number "acc123"
    When the user registers with DTU Pay
    Then the user should be registered successfully and receive a UUID
    And a "UserRegistered" event should be published with uuid

  # Scenario: Customer registration with duplicate account
  #   Given a customer with firstname "Alice" and lastname "Doe" and account number "acc123"
  #   And the customer is already registered
  #   When the customer registers with DTU Pay
  #   Then the registration should fail with error "Customer already registered: Alice Doe, acc123"

  

  # Scenario: Merchant registration with duplicate account
  #   Given a merchant with firstname "Bob" and lastname "Store" and CPR number "250580-9856" and account number "acc456"
  #   And the merchant is already registered
  #   When the merchant registers with DTU Pay
  #   Then the registration should fail with error "Merchant already registered: Bob Store, acc456"
