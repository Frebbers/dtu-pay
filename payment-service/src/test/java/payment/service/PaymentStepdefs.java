package payment.service;

import dtu.ws.fastmoney.BankService;
import io.cucumber.java.After;
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
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.function.Consumer;

import static org.mockito.Mockito.*;

public class PaymentStepdefs {
    private MessageQueue queue = mock(MessageQueue.class);
    private BankService bank = mock(BankService.class);
    private ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
    // Service is instantiated with the mock queue, bank, scheduler, and 5 second expiration
    private PaymentService service = new PaymentService(queue, bank, scheduler, 5);

    private PaymentReq paymentReq;
    private String customerId;
    private String customerBankAccNum;
    private String merchantBankAccNum;
    private CorrelationId correlationId;
    private Consumer<Event> paymentRequestedHandler;
    private Consumer<Event> bankAccountRetrievedHandler;

    @After
    public void cleanup() {
        service.shutdown();
    }

    @Given("a {string} event is received")
    public void aEventIsReceived(String eventName) {
        Assertions.assertEquals("PaymentRequested", eventName);

        // 1. Capture the handler that the service registered
        ArgumentCaptor<Consumer<Event>> captor = ArgumentCaptor.forClass(Consumer.class);
        verify(queue).addHandler(eq("PaymentRequested"), captor.capture());
        paymentRequestedHandler = captor.getValue();

        // 2. Capture bank account retrieved handler
        ArgumentCaptor<Consumer<Event>> bankCaptor = ArgumentCaptor.forClass(Consumer.class);
        verify(queue).addHandler(eq(service.BANK_ACCOUNT_RETRIEVED), bankCaptor.capture());
        bankAccountRetrievedHandler = bankCaptor.getValue();

        // 3. Create the event payload and invoke handler directly (no separate thread needed)
        paymentReq = new PaymentReq("token_123", "merchant_123", BigDecimal.valueOf(1000));
        correlationId = CorrelationId.randomId();
        Event event = new Event("PaymentRequested", new Object[] { paymentReq, correlationId });
        paymentRequestedHandler.accept(event);
    }

    @And("the customerId is fetched via TokenService")
    public void theCustomerIdIsFetchedViaTokenService() {
        customerId = "customer_123";
        // No sleep needed - events are handled synchronously
    }

    @And("the customerBankAccNum is fetched via AccountService")
    public void theCustomerBankAccNumIsFetchedViaAccountService() {
        // Create the reply event
        customerBankAccNum = "customer_bank_account_1";
        Event replyEvent = new Event(service.BANK_ACCOUNT_RETRIEVED,
                new Object[] { customerId, customerBankAccNum, correlationId });

        // Invoke the handler directly
        bankAccountRetrievedHandler.accept(replyEvent);
    }

    @And("the merchantBankAccNum is fetched via AccountService")
    public void theMerchantBankAccNumIsFetchedViaAccountService() {
        // Create the reply event
        merchantBankAccNum = "merchant_bank_account_1";
        Event replyEvent = new Event(service.BANK_ACCOUNT_RETRIEVED,
                new Object[] { paymentReq.merchantId(), merchantBankAccNum, correlationId });

        // Invoke the handler directly - this triggers payment processing since all data is available
        bankAccountRetrievedHandler.accept(replyEvent);
    }

    @When("the bank processes the payment successfully")
    public void theBankProcessesThePaymentSuccessfully() {
        // Payment is triggered once the service has all required event data.
        // In the new implementation, this happens synchronously in the last handler call.
    }

    @Then("the payment request is processed successfully")
    public void thePaymentRequestIsProcessedSuccessfully() {
        // Verify the success events
        ArgumentCaptor<Event> eventCaptor = ArgumentCaptor.forClass(Event.class);
        // We expect 2 publish calls:
        // 1. BankTransferCompletedSuccessfully
        // 2. PaymentSucceeded (to DTU Pay Server)
        verify(queue, times(2)).publish(eventCaptor.capture());

        // Get the BankTransferCompletedSuccessfully event
        Event successEvent = eventCaptor.getAllValues().get(0);
        Assertions.assertEquals(service.BANK_TRANSFER_COMPLETED_SUCCESSFULLY, successEvent.getTopic());
        
        PaymentRecord record = successEvent.getArgument(0, PaymentRecord.class);
        Assertions.assertEquals(paymentReq.amount(), record.amount());
        Assertions.assertEquals(customerId, record.customerId());
        Assertions.assertEquals(paymentReq.merchantId(), record.merchantId());
    }
}
