Feature: Registration

  
  Scenario: User registration with valid details
    Given a user with firstname "Alice" and lastname "Doe" and CPR "0000-0000" and account number "acc123"
    When the user registers with DTU Pay
    Then the user should be registered successfully and receive a CPR
    And a "UserRegistered" event should be published with cpr
  # Scenario: Duplicate registration publishes UserRegistrationFailed with same correlation id
  #   Given an existing account with firstname "Akhi" lastname "Louis" account "DK123"
  #   And a registration request for firstname "Akhi" lastname "Louis" account "DK123" with correlation id "c-2"
  #   When the registration request is handled
  #   Then an event "UserRegistrationFailed" is published
  #   And the published event has message containing "Account already exists"
  #   And the published event has correlation id "c-2"
 
  Scenario: Successful deregistration publishes UserDeregistered with same correlation id
    Given a registered user with firstname "James" and lastname "Bond" and CPR "007-000" and bank account "UK007"
    And a deregistration request is sent
    When the deregistration request is handled
    Then a "UserDeregistered" event is published
    And the account is removed from the account list
  # Scenario: Deregistering a non-existing account publishes UserDeregistrationFailed with same correlation id
  #   Given a deregistration request for non-existing account id "00000000-0000-0000-0000-000000000000" with correlation id "c-4"
  #   When the deregistration request is handled
  #   Then an event "UserDeregistrationFailed" is published
  #   And the published event has message containing "Account does not exist"
  #   And the published event has correlation id "c-4"
