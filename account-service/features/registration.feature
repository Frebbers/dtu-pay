Feature: Registration

  Scenario: Customer registration with valid details
    Given a customer with firstname "Alice" and lastname "Doe" and CPR number "101010-6464" and account number "acc123"
    When the customer registers with DTU Pay
    Then the customer should be registered successfully and receive a UUID
    And a "customer.registered" event should be published with uuid, name "Alice Doe" and account number "acc123"

  Scenario: Customer registration with duplicate account
    Given a customer with firstname "Alice" and lastname "Doe" and CPR number "101010-6464" and account number "acc123"
    And the customer is already registered
    When the customer registers with DTU Pay
    Then the registration should fail with error "Customer already registered: Alice Doe, acc123"

  Scenario: Merchant registration with valid details
    Given a merchant with firstname "Bob" and lastname "Store" and CPR number "250580-9856" and account number "acc456"
    When the merchant registers with DTU Pay
    Then the merchant should be registered successfully and receive a UUID
    And a "merchant.registered" event should be published with uuid, name "Bob Store" and account number "acc456"

  Scenario: Merchant registration with duplicate account
    Given a merchant with firstname "Bob" and lastname "Store" and CPR number "250580-9856" and account number "acc456"
    And the merchant is already registered
    When the merchant registers with DTU Pay
    Then the registration should fail with error "Merchant already registered: Bob Store, acc456"

  Scenario: Customer registration
    Given there is a customer with empty id
    When the customer is being registered
    Then the "CustomerRegistrationRequested" event is sent
    When the "CustomerIdAssigned" event is sent with non-empty id
    Then the customer is registered and his id is set

  Scenario: Merchant registration
    Given there is a merchant with empty id
    When the merchant is being registered
    Then the "MerchantRegistrationRequested" event is sent
    When the "MerchantIdAssigned" event is sent with non-empty id
    Then the merchant is registered and his id is set