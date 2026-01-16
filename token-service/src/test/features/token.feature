Feature: Token Service
  Scenario: Token service readiness
    Given the token service ability check passes
    Then the check result is healthy

