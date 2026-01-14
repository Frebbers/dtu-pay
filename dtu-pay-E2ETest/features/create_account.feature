Feature: create account
    Scenario: Successful Account Creation
        Given a customer with name "Susan", last name "Baldwin", and CPR "240396-8174"
        And the customer is registered with the bank with an initial balance of 1000 kr
        And the customer is registered with Simple DTU Pay using their bank account
        Then the account is created successfully