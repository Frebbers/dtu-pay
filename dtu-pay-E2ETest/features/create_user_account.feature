Feature: Account creation of a user in DTU pay

    Scenario: Successfull user account creation
        Given a user with name "Alice", last name "Johnson", and CPR "150390-1234"
        And the user is registered with the bank with an initial balance of 1000 kr
        And the user is registered with Simple DTU Pay using their bank account
        Then the account is created successfully