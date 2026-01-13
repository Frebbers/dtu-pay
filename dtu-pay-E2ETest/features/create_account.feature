Feature: create account
    Scenario: Successful Account Creation
        Given a user with name "Alice", last name "Johnson", and CPR "150390-1234"
        When the user registers for a DTU Pay account
        Then the account creation is successful
        And the user has a DTU Pay account associated with their bank account