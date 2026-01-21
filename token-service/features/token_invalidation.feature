Feature: Token Invalidation
    Scenario: Invalidate all tokens for a customer
        Given a customer id "cust-1"
        And the customer already has 2 unused tokens
        When a user deregistration request is received for customer "cust-1" with issued tokens
        Then all tokens are invalidated for customer "cust-1"