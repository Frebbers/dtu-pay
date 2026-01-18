Feature: Reporting Service
  Scenario: Customer report request
    Given the BankTransferCompletedSuccessfully event is received
    When the "CustomerReportRequested" event is received
    Then the "CustomerReportReturned" event is published

  Scenario: Merchant report request
    Given the BankTransferCompletedSuccessfully event is received
    When the "MerchantReportRequested" event is received
    Then the "MerchantReportReturned" event is published

  Scenario: Manager report request
    Given the BankTransferCompletedSuccessfully event is received
    When the "ManagerReportRequested" event is received
    Then the "ManagerReportReturned" event is published
