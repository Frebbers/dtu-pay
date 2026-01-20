package dtu.pay;

import dtu.ws.fastmoney.BankService;
import dtu.ws.fastmoney.BankServiceException_Exception;
import dtu.ws.fastmoney.BankService_Service;
import io.cucumber.java.After;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import jakarta.ws.rs.WebApplicationException;

import static org.junit.Assert.*;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class CommonUserSteps {

    private String firstName;
    private String lastName;
    private String cpr;
    private final ScenarioContext context;
    private BankService bank = new BankService_Service().getBankServicePort();
    private String bankApiKey = "amber2460";
    private DtuPayClient dtupay = new DtuPayClient();
    private String createdUserBankAccNumber;
    private List<String> bankAccounts = new ArrayList<>();
    private dtu.pay.User customer;
    private String DTUPayAccountId;
    private List<String> tokens;
    private Throwable latestError;

    @Given("User with CPR {string} is not registered in the bank")
    public void userWithCPRIsCleanedUpInTheBank(String cpr) {

        try {
            var acc = bank.getAccountByCprNumber(cpr);
            bank.retireAccount(bankApiKey, acc.getId());

        } catch (BankServiceException_Exception ignored) {
        }
    }

    @Given("User with CPR {string} is not registered in DTU Pay")
    public void userWithCPRIsNotRegisteredInDtuPay(String cpr) {
        try {
            dtupay.unregisterCustomer(cpr);
        } catch (Exception ignored) {
        }
        try {
            dtupay.unregisterMerchant(cpr);
        } catch (Exception ignored) {
        }
    }

    public CommonUserSteps(ScenarioContext context) {
        this.context = context;
    }

    @Given("a user with name {string}, last name {string}, and CPR {string}")
    public void aUserWithNameLastNameAndCpr(String first, String last, String cpr) {
        context.user = new dtu.pay.User(first, last, cpr, null);
    }

    @Given("the user is registered with the bank with an initial balance of {int} kr")
    public void registerInBank(int balance) throws BankServiceException_Exception {
        var user = new dtu.ws.fastmoney.User();
        user.setFirstName(context.user.firstName());
        user.setLastName(context.user.lastName());
        user.setCprNumber(context.user.cprNumber());

        context.bankAccountId = bank.createAccountWithBalance(bankApiKey, user, new BigDecimal(balance));
        if (context.bankAccountId == null || context.bankAccountId.isBlank()) {
            throw new AssertionError("Bank account id is missing after bank registration");
        }
    }

    @Given("the customer is registered with Simple DTU Pay using their bank account")
    public void registerInDtuPay() {
        var request = new dtu.pay.User(
                context.user.firstName(),
                context.user.lastName(),
                context.user.cprNumber(),
                context.bankAccountId);
        context.customerId = dtupay.registerDTUPayCustomer(request);
        if (context.customerId == null || context.customerId.isBlank()) {
            throw new AssertionError("DTU Pay customer id is missing afterr egistration");
        }
        context.DTUPayAccountId = context.customerId;
        context.customer = context.user;
        // var request = new dtu.pay.User(
        // context.user.firstName(),
        // context.user.lastName(),
        // context.user.cprNumber(),
        // context.bankAccountId);

        // try {
        // context.customerId = dtupay.registerDTUPayCustomer(request);
        // context.DTUPayAccountId = context.customerId;
        // context.latestError = null;
        // } catch (RuntimeException e) {
        // context.latestError = e;
        // }
    }

    @Given("the merchant is registered with Simple DTU Pay using their bank account")
    public void theMerchantIsRegisteredWithSimpleDTUPayUsingTheirBankAccount() {
        var request = new dtu.pay.User(
                context.user.firstName(),
                context.user.lastName(),
                context.user.cprNumber(),
                context.bankAccountId);
        context.merchantId = dtupay.registerDTUPayMerchant(request);
        if (context.merchantId == null || context.merchantId.isBlank()) {
            throw new AssertionError("DTU Pay merchant id is missing after registration");
        }
        context.DTUPayAccountId = context.merchantId;
        context.merchant = context.user;
    }

    @When("the same customer is registered again with Simple DTU Pay using their bank account")
    public void theSameCustomerIsRegisteredAgainWithSimpleDTUPayUsingTheirBankAccount() {
        var request = new dtu.pay.User(
                context.user.firstName(),
                context.user.lastName(),
                context.user.cprNumber(),
                context.bankAccountId
        );

        try {
            dtupay.registerDTUPayCustomer(request);
            context.latestError = null; // means: unexpectedly succeeded
        } catch (Throwable t) {
            context.latestError = t;    // expected path
        }
    }

    @Then("the DTU Pay registration is rejected with status {int}")
    public void theDTUPayRegistrationIsRejectedWithStatus(Integer expectedStatus) {
        assertNotNull("Expected registration to be rejected, but it succeeded", context.latestError);
        assertTrue(
            "Expected a WebApplicationException but got: " + context.latestError.getClass(),
            context.latestError instanceof WebApplicationException
        );

        WebApplicationException ex = (WebApplicationException) context.latestError;
        assertEquals(expectedStatus.intValue(), ex.getResponse().getStatus());
    }
    

    @Then("the error message contains {string}")
    public void theErrorMessageContains(String expected) {
        assertNotNull("Expected an error, but no error was captured", context.latestError);
        String msg = context.latestError.getMessage();
        assertNotNull("Error message was null", msg);
        assertTrue(
                "Expected error message to contain '" + expected + "' but was: " + msg,
                msg.toLowerCase().contains(expected.toLowerCase())
        );
    }
  
}

    // @After
    // public void cleanupDtuPayUsers() {
    // try {
    // if (context.customerId != null && !context.customerId.isBlank()) {
    // dtupay.unregisterCustomer(context.customerId);
    // }
    // } catch (Exception ignored) {
    // }

    // try {
    // if (context.merchantId != null && !context.merchantId.isBlank()) {
    // dtupay.unregisterMerchant(context.merchantId);
    // }
    // } catch (Exception ignored) {
    // }

    // // reset IDs so next scenario doesn't reuse them
    // context.customerId = null;
    // context.merchantId = null;
    // context.DTUPayAccountId = null;
    // }


