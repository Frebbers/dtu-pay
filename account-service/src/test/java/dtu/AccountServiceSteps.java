package dtu;

import dtu.repositories.WriteAccountRepository;
import dtu.repositories.User;
import dtu.repositories.ReadAccountRepository;
import dtu.services.AccountService;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import messaging.Event;
import messaging.MessageQueue;
import messaging.implementations.MessageQueueSync;

import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import java.util.UUID;

public class AccountServiceSteps {

  MessageQueue queue = new MessageQueueSync();
  MessageQueue queueExternal = mock(MessageQueue.class);
  WriteAccountRepository writeRepo = new WriteAccountRepository(queue);
  ReadAccountRepository readRepo = new ReadAccountRepository(queue);
  CorrelationId correlationId = CorrelationId.randomId();

  AccountService accountService = new AccountService(queueExternal, readRepo, writeRepo);
  private User account;
  private UUID createdId;

  // First scenario steps
  @Given("a user with firstname {string} and lastname {string} and account number {string}")
  public void aUserWithFirstnameAndLastnameAndAccountNumber(String string, String string2, String string3) {
    account = new User(string, string2, string3);

  }

  @When("the user registers with DTU Pay")
  public void theUserRegistersWithDTUPay() {
    accountService.handleUserRegistration(new Event("UserRegistrationRequested", new Object[] { account, correlationId }));
    createdId = readRepo.getAccountIdByBankAccountNumber(account.bankAccountNum());
  }

  @Then("the user should be registered successfully and receive a UUID")
  public void theUserShouldBeRegisteredSuccessfullyAndReceiveAUUID() {
    assertNotNull(createdId);
    assertTrue(readRepo.existsByBankAccountNumber(account.bankAccountNum()));
  }

  @Then("a {string} event should be published with uuid")
  public void aEventShouldBePublishedWithUuid(String eventName) {
    UUID id = readRepo.getAccountIdByBankAccountNumber(account.bankAccountNum());
    Event expected = new Event(eventName, new Object[] { id.toString(), correlationId });
    verify(queueExternal).publish(expected);
  }

  

  // Second scenario steps

  // @Given("a registration request for firstname {string} lastname {string} account {string} with correlation id {string}")
  // public void aRegistrationRequestForFirstnameLastnameAccountWithCorrelationId(String string, String string2,
  //     String string3, String string4) {
  //   // Write code here that turns the phrase above into concrete actions
  //   throw new io.cucumber.java.PendingException();
  // }

  // @When("the registration request is handled")
  // public void theRegistrationRequestIsHandled() {
  //   // Write code here that turns the phrase above into concrete actions
  //   throw new io.cucumber.java.PendingException();
  // }


  // // Third scenario steps

  // @Given("an existing account with firstname {string} lastname {string} account {string}")
  // public void anExistingAccountWithFirstnameLastnameAccount(String string, String string2, String string3) {
  //   // Write code here that turns the phrase above into concrete actions
  //   throw new io.cucumber.java.PendingException();
  // }

  // @Given("a deregistration request for that account id with correlation id {string}")
  // public void aDeregistrationRequestForThatAccountIdWithCorrelationId(String string) {
  //   // Write code here that turns the phrase above into concrete actions
  //   throw new io.cucumber.java.PendingException();
  // }


  // // Fourth scenario steps

  // @Given("a deregistration request for non-existing account id {string} with correlation id {string}")
  // public void aDeregistrationRequestForNonExistingAccountIdWithCorrelationId(String string, String string2) {
  //   // Write code here that turns the phrase above into concrete actions
  //   throw new io.cucumber.java.PendingException();
  // }

  // @When("the deregistration request is handled")
  // public void theDeregistrationRequestIsHandled() {
  //   // Write code here that turns the phrase above into concrete actions
  //   throw new io.cucumber.java.PendingException();
  // }

  // @Then("an event {string} is published")
  // public void anEventIsPublished(String string) {
  //   // Write code here that turns the phrase above into concrete actions
  //   throw new io.cucumber.java.PendingException();
  // }

  // @Then("the published event has message containing {string}")
  // public void thePublishedEventHasMessageContaining(String string) {
  //   // Write code here that turns the phrase above into concrete actions
  //   throw new io.cucumber.java.PendingException();
  // }

  // @Then("the published event has correlation id {string}")
  // public void thePublishedEventHasCorrelationId(String string) {
  //   // Write code here that turns the phrase above into concrete actions
  //   throw new io.cucumber.java.PendingException();
  // }

}
