package dtu.pay;

import dtu.pay.models.report.*;
import io.cucumber.java.en.And;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;

import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class ReportRequestSteps {
    private final ScenarioContext context;
    private final DtuPayClient dtuPayClient = new DtuPayClient();
    private CustomerReport customerReport;
    private MerchantReport merchantReport;
    private ManagerReport managerReport;

    public ReportRequestSteps(ScenarioContext context) {
        this.context = context;
    }


    @Given("the global payment history is empty")
    public void theGlobalPaymentHistoryIsEmpty() {
        dtuPayClient.cleanAllPayments();
    }

    @When("the merchant with id {string} initiates a payment of {int} kr by the customer with id {string} using the token")
    public void theMerchantWithIdInitiatesAPaymentOfKrByTheCustomerWithIdUsingTheToken(String merchantId, int cost, String customerId) {
        try {
            List<String> tokens = context.tokensMap.get(customerId);
            if (tokens == null) throw new RuntimeException(customerId + " doesn't have tokens (map: " + context.tokensMap + ")");
            String tokenToUse = tokens.getFirst();
            tokens.removeFirst();
            dtuPayClient.pay(tokenToUse, merchantId, new BigDecimal(cost));
        } catch (Exception e) {
            context.latestError = e;
        }
    }

    @And("the customer with id {string} has {int} unused tokens")
    public void theCustomerWithIdHasUnusedTokens(String customerId, int tokens) {
        context.tokensMap.put(customerId, dtuPayClient.requestTokens(customerId, tokens));
        if (context.tokensMap == null) {
            context.latestError = new RuntimeException("Token request failed: " + dtuPayClient.getLatestError());
        }
    }


    // Customer

    @Then("the customer with id {string} requests the report")
    public void theCustomerWithIdRequestsTheReport(String customerId) {
        customerReport = dtuPayClient.getCustomerReport(customerId);
        assertNotNull(customerReport);
    }

    @Then("the customer report contains {int} payments")
    public void theCustomerReportContainsPayments(int expectedCount) {
        List<CustomerReportEntry> payments = customerReport.payments();
        assertNotNull(payments);
        assertFalse(payments.isEmpty());
        assertEquals(expectedCount, payments.size());
    }

    @Then("the report contains a payment of {int} kr to the merchant with id {string}")
    public void theReportContainsAPaymentOfKrToTheMerchant(int amount, String expectedMerchantId) {
        List<CustomerReportEntry> payments = customerReport.payments();

        boolean matchFound = payments.stream().anyMatch(payment ->
                payment.amount() == amount && payment.merchantId().equals(expectedMerchantId)
        );
        assertTrue(matchFound);
    }


    // Merchant

    @Then("the merchant with id {string} requests the report")
    public void theMerchantWithIdRequestsTheReport(String merchantId) {
        merchantReport = dtuPayClient.getMerchantReport(merchantId);
        assertNotNull(merchantReport);
    }

    @Then("the merchant report contains {int} payments")
    public void theMerchantReportContainsPayments(int expectedCount) {
        List<MerchantReportEntry> payments = merchantReport.payments();
        assertNotNull(payments);
        assertFalse(payments.isEmpty());
        assertEquals(expectedCount, payments.size());
    }

    @And("the report contains a payment of {int} kr")
    public void theReportContainsAPaymentOfKr(int amount) {
        List<MerchantReportEntry> payments = merchantReport.payments();

        boolean matchFound = payments.stream().anyMatch(payment ->
                payment.amount() == amount
        );
        assertTrue(matchFound);
    }

    @And("the report does not contain the customer details")
    public void theReportDoesNotContainTheCustomerDetails() {
        Class<?> reportClass = MerchantReportEntry.class;

        for (Field field : reportClass.getDeclaredFields()) {
            String fieldName = field.getName().toLowerCase();
            assertFalse(fieldName.contains("customer"));
            assertFalse(fieldName.contains("cpr"));
        }
    }


    // Manager

    @Then("the manager requests the report")
    public void theManagerRequestsTheReport() {
        managerReport = dtuPayClient.getManagerReport();
        assertNotNull(managerReport);
    }

    @And("the manager report contains {int} payments")
    public void theManagerReportContainsPayments(int expectedCount) {
        List<ManagerReportEntry> payments = managerReport.payments();
        assertNotNull(payments);
        assertFalse(payments.isEmpty());
        assertEquals(expectedCount, payments.size());
    }

    @And("the report contains a summary of total transfers amount equal to {int} kr")
    public void theReportContainsASummaryOfTotalTransfersAmountEqualToKr(int expectedAmount) {
        assertEquals(expectedAmount, managerReport.totalMoneyTransferred());
    }
}
