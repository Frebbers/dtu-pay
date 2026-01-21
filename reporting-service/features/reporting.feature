Feature: Report generation and delivery
  Scenario: Customer request the report
    Given the BankTransferCompletedSuccessfully event is received
    When the "CustomerReportRequested" event is received
    Then the "CustomerReportReturned" event is published and the report is returned to the customer

  Scenario: Merchant request the report
    Given the BankTransferCompletedSuccessfully event is received
    When the "MerchantReportRequested" event is received
    Then the "MerchantReportReturned" event is published and the report is returned to the merchant

  Scenario: Manager request the report
    Given the BankTransferCompletedSuccessfully event is received
    When the "ManagerReportRequested" event is received
    Then the "ManagerReportReturned" event is published and the report is returned to the manager
