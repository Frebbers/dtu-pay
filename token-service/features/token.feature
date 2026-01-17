Feature: Token service
  Scenario: Customer can request tokens when none exist
    Given a customer id "cust-1"
    When the customer requests 3 tokens
    Then tokens are issued with count 3 for customer "cust-1"

  Scenario: Token request rejected when more than one unused token exists
    Given a customer id "cust-1"
    And the customer already has 2 unused tokens
    When the customer requests 1 tokens
    Then the token request is rejected

  Scenario: Token can be consumed once
    Given a customer id "cust-1"
    And the customer already has 1 unused tokens
    When the customer consumes a token
    Then the token is consumed for customer "cust-1"

  Scenario: Token request rejected when count exceeds max
    Given a customer id "cust-1"
    When the customer requests 6 tokens
    Then the token request is rejected

  Scenario: Token cannot be consumed twice
    Given a customer id "cust-1"
    And the customer already has 1 unused tokens
    When the customer consumes a token
    And the same token is consumed again
    Then the token consumption is rejected
