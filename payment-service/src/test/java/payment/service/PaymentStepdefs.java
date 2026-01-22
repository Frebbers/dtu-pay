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
import payment.service.models.CorrelationId;
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
    private String customerBankAccNum;
    private String merchantBankAccNum;
    private CorrelationId correlationId;
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
        correlationId = CorrelationId.randomId();
        Event event = new Event("PaymentRequested", new Object[] { paymentReq, correlationId });

        // 3. Run the handler in a separate thread since it waits for other events
        new Thread(() -> {
            try {
                paymentRequestedHandler.accept(event);
                paymentStatus.complete(true);
            } catch (Exception e) {
                paymentStatus.completeExceptionally(e);
            }
        }).start();
    }

    @And("the customerId is fetched via TokenService")
    public void theCustomerIdIsFetchedViaTokenService() throws InterruptedException {
        customerId = "customer_123";
        // Wait briefly to ensure the PaymentRequested handler thread has started
        Thread.sleep(100);
    }

    @And("the customerBankAccNum is fetched via AccountService")
    public void theCustomerBankAccNumIsFetchedViaAccountService() {
        // 1. Simulate the AccountService replying
        ArgumentCaptor<Consumer<Event>> handlerCaptor = ArgumentCaptor.forClass(Consumer.class);
        verify(queue).addHandler(eq(service.BANK_ACCOUNT_RETRIEVED), handlerCaptor.capture());
        Consumer<Event> bankAccountRetrievedHandler = handlerCaptor.getValue();

        // Create the reply event
        customerBankAccNum = "customer_bank_account_1";
        Event replyEvent = new Event(service.BANK_ACCOUNT_RETRIEVED,
                new Object[] { customerId, customerBankAccNum, correlationId });

        // Invoke the handler to unblock the service
        bankAccountRetrievedHandler.accept(replyEvent);
    }

    @And("the merchantBankAccNum is fetched via AccountService")
    public void theMerchantBankAccNumIsFetchedViaAccountService() {
        // 1. Reuse the already captured handler (registered in constructor, so same handler instance)
        ArgumentCaptor<Consumer<Event>> handlerCaptor = ArgumentCaptor.forClass(Consumer.class);
        verify(queue).addHandler(eq(service.BANK_ACCOUNT_RETRIEVED), handlerCaptor.capture());
        Consumer<Event> bankAccountRetrievedHandler = handlerCaptor.getValue();

        // Create the reply event
        merchantBankAccNum = "merchant_bank_account_1";
        Event replyEvent = new Event(service.BANK_ACCOUNT_RETRIEVED,
                new Object[] { paymentReq.merchantId(), merchantBankAccNum, correlationId });

        // Invoke the handler to unblock the service
        bankAccountRetrievedHandler.accept(replyEvent);
    }

    @When("the bank processes the payment successfully")
    public void theBankProcessesThePaymentSuccessfully() {
        // Payment is triggered once the service has all required event data.
    }

    @Then("the payment request is processed successfully")
    public void thePaymentRequestIsProcessedSuccessfully() {
        paymentStatus.join();

        // Verify the success events
        ArgumentCaptor<Event> eventCaptor = ArgumentCaptor.forClass(Event.class);
        // We expect 2 publish calls:
        // 1. PaymentProcessSuccess
        // 2. PaymentSucceeded (to DTU Pay Server)
        verify(queue, times(2)).publish(eventCaptor.capture());

        // Get the PaymentProcessSuccess event
        Event successEvent = eventCaptor.getAllValues().get(0);
        Assertions.assertEquals(service.BANK_TRANSFER_COMPLETED_SUCCESSFULLY, successEvent.getTopic());
        
        PaymentRecord record = successEvent.getArgument(0, PaymentRecord.class);
        Assertions.assertEquals(paymentReq.amount(), record.amount());
        Assertions.assertEquals(customerId, record.customerId());
        Assertions.assertEquals(paymentReq.merchantId(), record.merchantId());
    }
}
