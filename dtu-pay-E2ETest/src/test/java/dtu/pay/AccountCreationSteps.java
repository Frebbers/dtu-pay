package dtu.pay;

import static org.junit.Assert.*;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import dtu.ws.fastmoney.BankService;
import dtu.ws.fastmoney.BankService_Service;
import dtu.ws.fastmoney.Account;
import dtu.ws.fastmoney.BankServiceException_Exception;

import dtu.ws.fastmoney.User;
import io.cucumber.java.After;
import io.cucumber.java.PendingException;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import jakarta.ws.rs.core.Response;

public class AccountCreationSteps {

  private final ScenarioContext context;
  private BankService bank = new BankService_Service().getBankServicePort();
  private String bankApiKey = "amber2460";
  private DtuPayClient dtupay = new DtuPayClient();
  private String createdUserBankAccNumber;
  private List<String> bankAccounts = new ArrayList<>();
  private dtu.pay.User customer;
  private String DTUPayAccountId;
  private Throwable latestError;

  public  AccountCreationSteps(ScenarioContext context) {
    this.context = context;
  }

  @When("the customer attempts to register again with Simple DTU Pay using the same bank account")
  public void theUserAttemptsToRegisterAgainWithSimpleDTUPayUsingTheSameBankAccount() {
    var request = new dtu.pay.User(
            context.customer.firstName(),
            context.customer.lastName(),
            context.bankAccountId,
            null
    );

    context.DTUPayAccountId = dtupay.registerDTUPayAccount(request, "customers");
  }

  @Then("the DTU Pay registration is successful")
  public void theDTUPayRegistrationIsSuccessful() {
    if (latestError != null) {
      fail("DTU Pay registration failed with error: " + latestError.getMessage());
    }
  }

  @Then("a non-empty string user id is returned")
  public void aNonEmptyStringUserIdIsReturned() {
    assertNotNull("Returned user id should not be null", context.DTUPayAccountId);
    assertFalse("Returned user id should not be empty", context.DTUPayAccountId.trim().isEmpty());
  }

  @Then("the DTU Pay registration fails with error {string}")
  public void theDTUPayRegistrationFailsWithError(String expectedMessage) {
    Response response = dtupay.getLastResponse();

    assertNotNull(response, "No HTTP response received");
    assertTrue(
            response.getStatus() == 400 || response.getStatus() == 409,
            "Unexpected status: " + response.getStatus()
    );

    String body = response.readEntity(String.class);
    assertTrue(
            body.contains(expectedMessage),
            "Expected error message not found. Body was: " + body
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
