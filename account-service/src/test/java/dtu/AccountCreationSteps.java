package dtu;

import dtu.Exceptions.AccountAlreadyExistsException;
import dtu.repositories.AccountServiceTopics;
import dtu.repositories.User;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import messaging.Event;

import static org.junit.Assert.*;
import static org.mockito.Mockito.verify;

public class AccountCreationSteps {

  private final SharedContext context;

  // keep correlationId local to this step class (fine)
  private CorrelationId correlationId;

  public AccountCreationSteps(SharedContext context) {
    this.context = context;
  }

  @Given("a user with firstname {string} and lastname {string} and CPR {string} and account number {string}")
  public void aUserWithFirstnameAndLastnameAndCprAndAccountNumber(
      String firstName, String lastName, String cpr, String accountNumber) {
    context.account = new User(firstName, lastName, cpr, accountNumber);
  }

  @When("the user registers with DTU Pay")
  public void theUserRegistersWithDTUPay() {
    correlationId = CorrelationId.randomId();

    context.accountService.handleUserRegistration(
        new Event(AccountServiceTopics.USER_REGISTRATION_REQUESTED,
            new Object[]{ context.account, correlationId })
    );

    context.createdCpr = context.readRepo.getCprByBankAccount(context.account.bankAccountNum());
  }

  @Then("the user should be registered successfully and receive a CPR")
  public void theUserShouldBeRegisteredSuccessfullyAndReceiveACPR() {
    assertNotNull(context.createdCpr);
    assertTrue(context.readRepo.existsByBankAccountNumber(context.account.bankAccountNum()));
  }

  @Then("a {string} event should be published with cpr")
  public void aEventShouldBePublishedWithCpr(String eventName) {
    String cpr = context.readRepo.getCprByBankAccount(context.account.bankAccountNum());

    verify(context.queueExternal).publish(
        new Event(eventName, new Object[]{ cpr, correlationId })
    );
  }

  // Second scenario
  @Given("a registered user with firstname {string} and lastname {string} and CPR {string} and bank account {string}")
  public void aRegisteredUserFirstnameLastnameAndCprAndBankAccount(
      String firstName, String lastName, String cpr, String bankAccountNum) throws AccountAlreadyExistsException {

    context.createdCpr = context.accountService.createAccount(firstName, lastName, cpr, bankAccountNum);
    context.bankAccount = bankAccountNum;
  }

  @Given("a deregistration request is sent")
  public void aDeregistrationRequestIsSent() {
    correlationId = CorrelationId.randomId();

    context.deregistrationEvent = new Event(
        AccountServiceTopics.USER_DEREGISTERED_REQUESTED,
        new Object[]{ context.createdCpr, correlationId }
    );
  }

  @When("the deregistration request is handled")
  public void theDeregistrationRequestIsHandled() {
    context.accountService.handleUserDeregistration(context.deregistrationEvent);
  }

  @Then("a {string} event is published")
  public void aEventIsPublished(String eventName) {
    verify(context.queueExternal).publish(
        new Event(eventName, new Object[]{ context.createdCpr, correlationId })
    );
  }

  @Then("the account is removed from the account list")
  public void theAccountIsRemovedFromTheAccountList() {
    assertFalse(context.readRepo.existsByBankAccountNumber(context.bankAccount));
  }
}
