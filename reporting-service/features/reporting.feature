Feature: Reporting Service
  Scenario: Reporting service readiness
    Given the reporting service ability check passes
    Then the check result is healthy

