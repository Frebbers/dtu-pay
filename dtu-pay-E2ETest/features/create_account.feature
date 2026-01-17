Feature: create account

    @ignore
    Scenario: Successful customer Account Creation
        Given a customer with name "Susan", last name "Baldwin", and CPR "240396-8174"
        And the customer is registered with the bank with an initial balance of 1000 kr
        And the customer is registered with DTU Pay using their bank account
        Then the account is created successfully

    @ignore
    Scenario: Successful merchant Account Creation
        Given a merchant with name "Daniel", last name "Oliver", and CPR "120394-4827"
        And the merchant is registered with the bank with an initial balance of 1000 kr
        And the merchant is registered with DTU Pay using their bank account
        Then the account is created successfullyay.Use