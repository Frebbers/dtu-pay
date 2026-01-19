Feature: Bank account retireval

  Scenario: Retrieve bank account number for an existing user
    Given a registered user with firstname "Akhi" and lastname "Louis" and CPR "111111-1111" and bank account "DK111"
    And a bank account number request is sent for that user
    When the bank account request is handled
    Then a "BankAccountRetrieved" event is published with bank account "DK111"
    And the stored bank account for that user is "DK111"

