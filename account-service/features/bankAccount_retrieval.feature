Feature: Bank account retrieval

  Scenario: Retrieve bank account number for an existing user
    Given a registered user with firstname "Akhi" and lastname "Louis" and CPR "111111-1111" and bank account "DK111"
    And a bank account number request is sent for that user
    When the bank account request is handled
    Then a "accounts.events.BankAccountRetrieved" event is published with bank account "DK111"
    And the stored bank account for that user is "DK111"

  Scenario: Bank account retrieval fails for a non-existing user
    Given a bank account number request is sent for a non-existing user with CPR "999999-9999"
    When the bank account request is handled
    Then the retrieval failure message is "Account with CPR999999-9999 does not exist"
