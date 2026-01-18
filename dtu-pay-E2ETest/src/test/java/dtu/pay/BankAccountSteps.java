package dtu.pay;

import static org.junit.Assert.*;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import dtu.ws.fastmoney.BankService;
import dtu.ws.fastmoney.BankService_Service;
import dtu.ws.fastmoney.BankServiceException_Exception;

import io.cucumber.java.After;
import io.cucumber.java.PendingException;
import io.cucumber.java.en.And;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;

public class BankAccountSteps {

  private final ScenarioContext context;
  private BankService bank = new BankService_Service().getBankServicePort();
  private String bankApiKey = "amber2460";
  private DtuPayClient dtupay = new DtuPayClient();
  private String createdUserBankAccNumber;
  private List<String> bankAccounts = new ArrayList<>();
  private dtu.pay.User customer;
  private String DTUPayAccountId;
  private Throwable latestError;

  public BankAccountSteps(ScenarioContext context) {
    this.context = context;
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

    @Given("the user is registered with the bank with an initial balance of {int} kr")
    public void registerInBank(int balance) throws BankServiceException_Exception {
        var user = new dtu.ws.fastmoney.User();
        user.setFirstName(context.user.firstName());
        user.setLastName(context.user.lastName());
        user.setCprNumber(context.user.cprNumber());

        context.bankAccountId = bank.createAccountWithBalance
                (bankApiKey, user, new BigDecimal(balance));
    }




}
