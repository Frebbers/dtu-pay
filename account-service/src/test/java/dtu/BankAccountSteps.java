package dtu;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.verify;

import dtu.Exceptions.AccountDoesNotExistsException;
import dtu.tokens.AccountServiceTopics;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import messaging.Event;


public class BankAccountSteps {

  private final SharedContext context;

  public BankAccountSteps(SharedContext context) {
    this.context = context;
  }

  @Given("a bank account number request is sent for that user")
  public void aBankAccountNumberRequestIsSentForThatUser() {
    context.correlationId = CorrelationId.randomId();

    context.bankAccountRequestedEvent = new Event(
        AccountServiceTopics.BANK_ACCOUNT_REQUESTED,
        context.createdCpr,
        context.correlationId);
  }

  @When("the bank account request is handled")
  public void theBankAccountRequestIsHandled() throws AccountDoesNotExistsException {
    context.accountService.handleBankAccountNumberRequested(context.bankAccountRequestedEvent);
  }

  @Then("a {string} event is published with bank account {string}")
  public void aEventIsPublishedWithBankAccount(String eventName, String expectedBankAccount) {
    verify(context.queueExternal).publish(
        new Event(eventName, expectedBankAccount, context.correlationId));
  }

  @Then("the stored bank account for that user is {string}")
  public void theStoredBankAccountForThatUserIs(String expectedBankAccount) throws AccountDoesNotExistsException {
    assertEquals(expectedBankAccount, context.readRepo.getBankAccount(context.createdCpr));
  }

  @Given("a bank account number request is sent for a non-existing user with CPR {string}")
  public void aBankAccountNumberRequestIsSentForANonExistingUserWithCPR(String cpr) {
    context.createdCpr = cpr;
    context.correlationId = CorrelationId.randomId();

    context.bankAccountRequestedEvent = new Event(
        AccountServiceTopics.BANK_ACCOUNT_REQUESTED,
        context.createdCpr,
        context.correlationId);
  }

  @Then("the retrieval failure message is {string}")
  public void theRetrievalFailureMessageIs(String expectedMessage) {
    verify(context.queueExternal).publish(
        new Event(
            AccountServiceTopics.BANK_ACCOUNT_RETRIEVAL_FAILED,
            new Object[] { expectedMessage, context.correlationId }));
  }

}
