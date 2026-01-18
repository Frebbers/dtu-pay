Feature: Registration

  Scenario: User registration with valid details
    Given a user with firstname "Alice" and lastname "Doe" and account number "acc123"
    When the user registers with DTU Pay
    Then the user should be registered successfully and receive a UUID
    And a "UserRegistered" event should be published with uuid

  # Scenario: Duplicate registration publishes UserRegistrationFailed with same correlation id
  #   Given an existing account with firstname "Akhi" lastname "Louis" account "DK123"
  #   And a registration request for firstname "Akhi" lastname "Louis" account "DK123" with correlation id "c-2"
  #   When the registration request is handled
  #   Then an event "UserRegistrationFailed" is published
  #   And the published event has message containing "Account already exists"
  #   And the published event has correlation id "c-2"

  # Scenario: Successful deregistration publishes UserDeregistered with same correlation id
  #   Given an existing account with firstname "Sara" lastname "Larson" account "DK999"
  #   And a deregistration request for that account id with correlation id "c-3"
  #   When the deregistration request is handled
  #   Then an event "UserDeregistered" is published
  #   And the published event has correlation id "c-3"

  # Scenario: Deregistering a non-existing account publishes UserDeregistrationFailed with same correlation id
  #   Given a deregistration request for non-existing account id "00000000-0000-0000-0000-000000000000" with correlation id "c-4"
  #   When the deregistration request is handled
  #   Then an event "UserDeregistrationFailed" is published
  #   And the published event has message containing "Account does not exist"
  #   And the published event has correlation id "c-4"
