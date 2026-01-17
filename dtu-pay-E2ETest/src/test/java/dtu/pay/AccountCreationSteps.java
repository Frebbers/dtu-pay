package dtu.pay;

import static org.junit.Assert.*;

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

public class AccountCreationSteps {

  private String firstName;
  private String lastName;
  private String cpr;
  private BankService bank = new BankService_Service().getBankServicePort();
  private String bankApiKey = "amber2460";
  private DtuPayClient dtupay = new DtuPayClient();
  private String createdUserBankAccNumber;
  private List<String> bankAccounts = new ArrayList<>();
  private dtu.pay.User customer;
  private String DTUPayAccountId;
  private Throwable latestError;

  @Given("a user with name {string}, last name {string}, and CPR {string}")
  public void aUserWithNameLastNameAndCPR(String firstName, String lastName, String cpr) {
    customer = new dtu.pay.User(firstName, lastName, null, cpr);
  }

  @Given("the user is registered with the bank with an initial balance of {int} kr")
  public void theUserIsRegisteredWithTheBankWithAnInitialBalanceOfKr(Integer initialBalance) throws BankServiceException_Exception {
    User user = new User();
    user.setCprNumber(customer.cprNumber());
    user.setFirstName(customer.firstName());
    user.setLastName(customer.lastName());

    createdUserBankAccNumber = bank.createAccountWithBalance(bankApiKey, user, new BigDecimal(initialBalance));
    bankAccounts.add(createdUserBankAccNumber);
  }

  @When("the customer is registered with Simple DTU Pay using their bank account")
  public void theUserIsRegisteredWithSimpleDTUPayUsingTheirBankAccount() {
    latestError = null;
    DTUPayAccountId = null;

    dtu.pay.User request = new dtu.pay.User(customer.firstName(), customer.lastName(), createdUserBankAccNumber, null); 

    try {
      DTUPayAccountId = dtupay.registerDTUPayCustomer(request);
    } catch (Throwable t) {
      latestError = t;
    }
  }

  @Then("the DTU Pay registration is successful")
  public void theDTUPayRegistrationIsSuccessful() {
    if (latestError != null) {
      fail("DTU Pay registration failed with error: " + latestError.getMessage());
    }
  }

  @Then("a non-empty string user id is returned")
  public void aNonEmptyStringUserIdIsReturned() {
    assertNotNull("Returned user id should not be null", DTUPayAccountId);
    assertFalse("Returned user id should not be empty", DTUPayAccountId.trim().isEmpty());
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
  @Given("User with CPR {string} is not registered in the bank")
  public void userWithCPRIsCleanedUp(String cpr) {

    try {
      var acc = bank.getAccountByCprNumber(cpr);
      bank.retireAccount(bankApiKey, acc.getId());
    }
    catch (BankServiceException_Exception ignored) {}
  }

  @When("the merchant is registered with Simple DTU Pay using their bank account")
  public void theMerchantIsRegisteredWithSimpleDTUPayUsingTheirBankAccount() {
    latestError = null;
    DTUPayAccountId = null;

    dtu.pay.User request = new dtu.pay.User(customer.firstName(), customer.lastName(), createdUserBankAccNumber, null);

    try {
      DTUPayAccountId = dtupay.registerDTUPayMerchant(request);
    } catch (Throwable t) {
    latestError = t;
   }
  }

  @When("the user attempts to register again with Simple DTU Pay using the same bank account")
  public void theUserAttemptsToRegisterAgainWithSimpleDTUPayUsingTheSameBankAccount() {
    // Write code here that turns the phrase above into concrete actions
    assert latestError == null;
    theUserIsRegisteredWithSimpleDTUPayUsingTheirBankAccount();
  }

  @Then("the DTU Pay registration fails with error {string}")
  public void theDTUPayRegistrationFailsWithError(String expectedErrorMessage) {
    // Write code here that turns the phrase above into concrete actions
    assert latestError.getMessage().contains(expectedErrorMessage);
  }
}
