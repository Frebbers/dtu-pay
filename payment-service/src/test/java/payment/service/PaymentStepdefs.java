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
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

import static org.mockito.Mockito.*;

public class PaymentStepdefs {
    private MessageQueue queue = mock(MessageQueue.class);
    private BankService bank = mock(BankService.class);
    // Service is instantiated with the mock queue and bank
    private PaymentService service = new PaymentService(queue, bank);
    
    private PaymentReq paymentReq;
    private String customerId;
    private int amount;
    private String customerBankAccNum;
    private String merchantBankAccNum;
    
    // Thread management for the async service call
    private CompletableFuture<Boolean> paymentStatus = new CompletableFuture<>();

    @Given("a {string} event is received")
    public void aEventIsReceived(String eventName) {
        Assertions.assertEquals("PaymentRequested", eventName);
        
        // 1. Capture the handler that the service registered
        ArgumentCaptor<Consumer<Event>> captor = ArgumentCaptor.forClass(Consumer.class);
        verify(queue).addHandler(eq("PaymentRequested"), captor.capture());
        Consumer<Event> paymentRequestedHandler = captor.getValue();

        // 2. Create the event payload
        paymentReq = new PaymentReq("token_123", "merchant_123", BigDecimal.valueOf(1000));
        CorrelationId correlationId = CorrelationId.randomId();
        Event event = new Event("PaymentRequested", new Object[]{paymentReq, correlationId});

        // 3. Run the handler in a separate thread because the service blocks waiting for a reply
        new Thread(() -> {
            try {
                paymentRequestedHandler.accept(event);
                // If we get here without exception, we assume success for this simple test structure
                paymentStatus.complete(true); 
            } catch (Exception e) {
                paymentStatus.completeExceptionally(e);
            }
        }).start();
    }

    @And("the customerId is fetched via TokenService")
    public void theCustomerIdIsFetchedViaTokenService() {
        // 1. Verify the service requested the token consumption
        ArgumentCaptor<Event> eventCaptor = ArgumentCaptor.forClass(Event.class);
        verify(queue, timeout(5000)).publish(eventCaptor.capture());
        
        Event publishedEvent = eventCaptor.getValue();
        Assertions.assertEquals(service.CONSUME_TOKEN_REQUESTED, publishedEvent.getTopic());
        
        ConsumeTokenRequested req = publishedEvent.getArgument(0, ConsumeTokenRequested.class);
        String correlationId = req.commandId();
        amount = req.amount();

        // 2. Simulate the TokenService replying
        // First, we need to capture the handler for TokenConsumed
        ArgumentCaptor<Consumer<Event>> handlerCaptor = ArgumentCaptor.forClass(Consumer.class);
        verify(queue).addHandler(eq(service.TOKEN_CONSUMED), handlerCaptor.capture());
        Consumer<Event> tokenConsumedHandler = handlerCaptor.getValue();

        // Create the reply event
        customerId = "customer_123"; // The expected customer ID
        TokenConsumed tokenConsumed = new TokenConsumed(correlationId, paymentReq.token(), customerId, 0);
        Event replyEvent = new Event(service.TOKEN_CONSUMED, new Object[]{tokenConsumed});

        // Invoke the handler to unblock the service
        tokenConsumedHandler.accept(replyEvent);
    }

    @And("the customerBankAccNum is fetched via AccountService")
    public void theCustomerBankAccNumIsFetchedViaAccountService() {
        // 1. Verify the service requested the bank account
        ArgumentCaptor<Event> eventCaptor = ArgumentCaptor.forClass(Event.class);
        // We expect this to be the second event published (first was Token request)
        verify(queue, timeout(5000).times(2)).publish(eventCaptor.capture());

        Event publishedEvent = eventCaptor.getAllValues().get(1);
        Assertions.assertEquals(service.GET_BANK_ACCOUNT_REQUESTED, publishedEvent.getTopic());

        String requestedUserId = publishedEvent.getArgument(0, String.class);
        CorrelationId correlationId = publishedEvent.getArgument(1, CorrelationId.class);

        Assertions.assertEquals(customerId, requestedUserId);

        // 2. Simulate the AccountService replying
        ArgumentCaptor<Consumer<Event>> handlerCaptor = ArgumentCaptor.forClass(Consumer.class);
        verify(queue).addHandler(eq(service.BANK_ACCOUNT_RETRIEVED), handlerCaptor.capture());
        Consumer<Event> bankAccountRetrievedHandler = handlerCaptor.getValue();

        // Create the reply event
        customerBankAccNum = "customer_bank_account_1";
        Event replyEvent = new Event(service.BANK_ACCOUNT_RETRIEVED, new Object[]{customerBankAccNum, correlationId});

        // Invoke the handler to unblock the service
        bankAccountRetrievedHandler.accept(replyEvent);
    }

    @And("the merchantBankAccNum is fetched via AccountService")
    public void theMerchantBankAccNumIsFetchedViaAccountService() {
        // 1. Verify the service requested the bank account
        ArgumentCaptor<Event> eventCaptor = ArgumentCaptor.forClass(Event.class);
        // Third event published (Token, CustomerAcc, MerchantAcc)
        verify(queue, timeout(5000).times(3)).publish(eventCaptor.capture());

        Event publishedEvent = eventCaptor.getAllValues().get(2);
        Assertions.assertEquals(service.GET_BANK_ACCOUNT_REQUESTED, publishedEvent.getTopic());

        String requestedUserId = publishedEvent.getArgument(0, String.class);
        CorrelationId correlationId = publishedEvent.getArgument(1, CorrelationId.class);

        Assertions.assertEquals(paymentReq.merchantId(), requestedUserId);

        // 2. Reuse the already captured handler (registered in constructor, so same handler instance)
        ArgumentCaptor<Consumer<Event>> handlerCaptor = ArgumentCaptor.forClass(Consumer.class);
        verify(queue).addHandler(eq(service.BANK_ACCOUNT_RETRIEVED), handlerCaptor.capture());
        Consumer<Event> bankAccountRetrievedHandler = handlerCaptor.getValue();

        // Create the reply event
        merchantBankAccNum = "merchant_bank_account_1";
        Event replyEvent = new Event(service.BANK_ACCOUNT_RETRIEVED, new Object[]{merchantBankAccNum, correlationId});

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
        // Wait for the async process to finish
        paymentStatus.join();

        // Verify the success event
        ArgumentCaptor<Event> eventCaptor = ArgumentCaptor.forClass(Event.class);
        // We expect 5 publish calls: 
        // 1. ConsumeTokenRequested
        // 2. GetBankAccount (Customer)
        // 3. GetBankAccount (Merchant)
        // 4. PaymentProcessSuccess
        // 5. PaymentSucceeded (to DTU Pay Server)
        verify(queue, times(5)).publish(eventCaptor.capture());
        
        // Get the PaymentProcessSuccess event (index 3, starting from 0)
        Event successEvent = eventCaptor.getAllValues().get(3);
        Assertions.assertEquals(service.BANK_TRANSFER_COMPLETED_SUCCESSFULLY, successEvent.getTopic());
        
        PaymentRecord record = successEvent.getArgument(0, PaymentRecord.class);
        Assertions.assertEquals(paymentReq.amount(), record.amount());
        Assertions.assertEquals(customerId, record.customerId());
        Assertions.assertEquals(paymentReq.merchantId(), record.merchantId());
    }
}
