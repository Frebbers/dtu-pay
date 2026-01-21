Feature: Registration

  Scenario: User registration with valid details
    Given a user with firstname "Alice" and lastname "Doe" and CPR "0000-0000" and account number "acc123"
    When the user registers with DTU Pay
    Then the user should be registered successfully and receive a CPR
    And a "UserRegistered" event should be published with cpr

  Scenario: Successful deregistration publishes UserDeregistered with same correlation id
    Given a registered user with firstname "James" and lastname "Bond" and CPR "007-000" and bank account "UK007"
    And a deregistration request is sent
    When the deregistration request is handled
    Then a "UserDeregistered" event is published
    And the account is removed from the account list

  Scenario: User registration fails when CPR already exists
    Given a registered user with firstname "Alice" and lastname "Doe" and CPR "000000-0000" and bank account "acc123"
    And a user with firstname "Alice" and lastname "Doe" and CPR "000000-0000" and account number "acc123"
    When the user registers with DTU Pay
    Then the registration failure message is "Account with CPR number 000000-0000 already exists"


  Scenario: Deregistration fails for a non-existing user
    Given a deregistration request is sent for CPR "999-999"
    When the deregistration request is handled
    Then the deregistration failure message is "Account with CPR number 999-999 does not exist"
