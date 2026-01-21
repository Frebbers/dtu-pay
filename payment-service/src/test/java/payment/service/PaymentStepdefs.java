package payment.service;

import dtu.ws.fastmoney.BankService;
import io.cucumber.java.en.And;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import messaging.Event;
import messaging.MessageQueue;
import org.junit.jupiter.api.Assertions;
import org.mockito.ArgumentCaptor;
import payment.service.models.*;

import java.math.BigDecimal;
import java.util.function.Consumer;

import static org.mockito.Mockito.*;

public class PaymentStepdefs {
    private MessageQueue queue = mock(MessageQueue.class);
    private BankService bank = mock(BankService.class);
    // Service is instantiated with the mock queue and bank
    private PaymentService service = new PaymentService(queue, bank);
    
    private PaymentReq paymentReq;
    private String customerId;
    
    private CorrelationId correlationId;

    @Given("a {string} event is received")
    public void aEventIsReceived(String eventName) {
        Assertions.assertEquals("PaymentRequested", eventName);
        
        // 1. Capture the handler that the service registered
        ArgumentCaptor<Consumer<Event>> captor = ArgumentCaptor.forClass(Consumer.class);
        verify(queue).addHandler(eq("PaymentRequested"), captor.capture());
        Consumer<Event> paymentRequestedHandler = captor.getValue();

        // 2. Create the event payload
        paymentReq = new PaymentReq("token_123", "merchant_123", BigDecimal.valueOf(1000));
        correlationId = CorrelationId.randomId();
        Event event = new Event("PaymentRequested", new Object[]{paymentReq, correlationId});

        // 3. Run the handler; the service now reacts asynchronously via events
        paymentRequestedHandler.accept(event);
    }

    @And("the customerId is fetched via TokenService")
    public void theCustomerIdIsFetchedViaTokenService() {
        // Simulate the TokenService replying
        ArgumentCaptor<Consumer<Event>> handlerCaptor = ArgumentCaptor.forClass(Consumer.class);
        verify(queue).addHandler(eq(service.TOKEN_CONSUMED), handlerCaptor.capture());
        Consumer<Event> tokenConsumedHandler = handlerCaptor.getValue();

        // Create the reply event
        customerId = "customer_123"; // The expected customer ID
        TokenConsumed tokenConsumed = new TokenConsumed(this.correlationId.id().toString(), paymentReq.token(), customerId, 0);
        Event replyEvent = new Event(service.TOKEN_CONSUMED, new Object[]{tokenConsumed, this.correlationId});

        // Invoke the handler to unblock the service
        tokenConsumedHandler.accept(replyEvent);
    }

    @And("the customerBankAccNum is fetched via AccountService")
    public void theCustomerBankAccNumIsFetchedViaAccountService() {
        // Simulate the AccountService replying
        ArgumentCaptor<Consumer<Event>> handlerCaptor = ArgumentCaptor.forClass(Consumer.class);
        verify(queue).addHandler(eq(service.BANK_ACCOUNT_RETRIEVED), handlerCaptor.capture());
        Consumer<Event> bankAccountRetrievedHandler = handlerCaptor.getValue();

        // Create the reply event
        String customerBankAccNum = "customer_bank_account_1";
        Event replyEvent = new Event(service.BANK_ACCOUNT_RETRIEVED,
                new Object[]{customerBankAccNum, customerId, this.correlationId});

        // Invoke the handler to unblock the service
        bankAccountRetrievedHandler.accept(replyEvent);
    }

    @And("the merchantBankAccNum is fetched via AccountService")
    public void theMerchantBankAccNumIsFetchedViaAccountService() {
        // Reuse the already captured handler (registered in constructor, so same handler instance)
        ArgumentCaptor<Consumer<Event>> handlerCaptor = ArgumentCaptor.forClass(Consumer.class);
        verify(queue).addHandler(eq(service.BANK_ACCOUNT_RETRIEVED), handlerCaptor.capture());
        Consumer<Event> bankAccountRetrievedHandler = handlerCaptor.getValue();

        // Create the reply event
        String merchantBankAccNum = "merchant_bank_account_1";
        Event replyEvent = new Event(service.BANK_ACCOUNT_RETRIEVED,
                new Object[]{merchantBankAccNum, paymentReq.merchantId(), this.correlationId});

        // Invoke the handler to unblock the service
        bankAccountRetrievedHandler.accept(replyEvent);
    }

    @When("the bank processes the payment successfully")
    public void theBankProcessesThePaymentSuccessfully() {
        // The service logic is running in the background thread and will call processPayment internally.
        // We do not need to invoke it manually here. 
    }

    @Then("the payment request is processed successfully")
    public void thePaymentRequestIsProcessedSuccessfully() {
        // Verify the success event
        ArgumentCaptor<Event> eventCaptor = ArgumentCaptor.forClass(Event.class);
        verify(queue, atLeast(1)).publish(eventCaptor.capture());

        Event successEvent = findPublishedEvent(eventCaptor, service.BANK_TRANSFER_COMPLETED_SUCCESSFULLY);
        Assertions.assertNotNull(successEvent);

        PaymentRecord record = successEvent.getArgument(0, PaymentRecord.class);
        Assertions.assertEquals(paymentReq.amount(), record.amount());
        Assertions.assertEquals(customerId, record.customerId());
        Assertions.assertEquals(paymentReq.merchantId(), record.merchantId());

        Event paymentSucceeded = findPublishedEvent(eventCaptor, service.PAYMENT_SUCCEEDED);
        Assertions.assertNotNull(paymentSucceeded);
    }

    private Event findPublishedEvent(ArgumentCaptor<Event> eventCaptor, String topic) {
        for (Event event : eventCaptor.getAllValues()) {
            if (topic.equals(event.getTopic())) {
                return event;
            }
        }
        return null;
    }

}
