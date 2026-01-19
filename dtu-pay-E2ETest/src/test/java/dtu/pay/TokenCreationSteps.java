package dtu.pay;

import dtu.ws.fastmoney.BankService;
import dtu.ws.fastmoney.BankServiceException_Exception;
import dtu.ws.fastmoney.BankService_Service;
import io.cucumber.java.After;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.When;
import io.cucumber.java.en.Then;

import dtu.ws.fastmoney.User;
import jakarta.ws.rs.core.Response;
import org.junit.jupiter.api.Assertions;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class TokenCreationSteps {

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

    public TokenCreationSteps(ScenarioContext context) {
        this.context = context;
    }

    @Given("the customer has {int} unused tokens")
    public void theCustomerHasUnusedTokens(int amount) {
        context.tokens = dtupay.requestTokens(context.customerId, amount);
        if (context.tokens == null) {
            context.latestError = new RuntimeException("Token request failed: " + dtupay.getLatestError());
        }
    }

    @When("the customer requests {int} tokens")
    public void theCustomerRequestsTokens(int amount) {
        context.tokens = dtupay.requestTokens(context.customerId, amount);
        if (context.tokens == null) {
            context.latestError = new RuntimeException("Token request failed: " + dtupay.getLatestError());
        }
    }

    @Then("the customer receives exactly {int} unique tokens")
    public void theCustomerReceivesExactlyUniqueTokens(int expectedAmount) {
        assertNotNull(context.tokens);
        assertEquals(expectedAmount, context.tokens.size());
        assertEquals(expectedAmount, context.tokens.stream().distinct().count());
    }

    @Then("the customer doesn't get tokens")
    public void theCustomerDoesnTGetTokens() {
        Response response = dtupay.getLastResponse();

        assertNotNull(response, "No HTTP response received");
        assertTrue(
                response.getStatus() == 400 || response.getStatus() == 409,
                "Expected token request to be rejected, but got status " + response.getStatus()
        );
    }

    @After
    public void breakDown(){
        for (String account : bankAccounts) {
            try {
                bank.retireAccount(bankApiKey, account);
            } catch (BankServiceException_Exception e) {
                System.out.println("Could not retire account: " + account);
            }
        }
    }
}
