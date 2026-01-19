package payment.service;

import dtu.ws.fastmoney.*;
import messaging.Event;
import messaging.MessageQueue;
import payment.service.models.*;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

public class PaymentService {
    BankService bank = new BankService_Service().getBankServicePort();
    MessageQueue queue;
    Map<UUID, PaymentReq> paymentHashMap = new HashMap<>();

    // Event names
    String PAYMENT_REQUESTED = "PaymentRequested";
    String PAYMENT_SUCCEEDED = "PaymentSucceeded";
    String CONSUME_TOKEN_REQUESTED = "token.commands.ConsumeTokenRequested";
    String TOKEN_CONSUMED = "token.events.TokenConsumed";
    String GET_BANK_ACCOUNT_REQUESTED = "accounts.commands.GetBankAccount";
    String BANK_ACCOUNT_RETRIEVED = "accounts.events.BankAccountRetrieved";
    String BANK_TRANSFER_COMPLETED_SUCCESSFULLY = "BankTransferCompletedSuccessfully";

    // Get customerId by token
    private final Map<String, CompletableFuture<String>> getCustomerIdCorrelations = new ConcurrentHashMap<>();
    private final Map<CorrelationId, CompletableFuture<String>> getBankAccCorrelations = new ConcurrentHashMap<>();

    public PaymentService(MessageQueue q) {
        this.queue = q;
        queue.addHandler(PAYMENT_REQUESTED, this::policyPaymentRequested);
        queue.addHandler(TOKEN_CONSUMED, this::handleTokenConsumed);
        queue.addHandler(BANK_ACCOUNT_RETRIEVED, this::handleBankAccNumberRetrieved);
    }

    /* Policies */

    public void policyPaymentRequested(Event event) {
        PaymentReq paymentReq = event.getArgument(0, PaymentReq.class);
        CorrelationId correlationId = event.getArgument(1, CorrelationId.class);

        String customerId = getCustomerIdByToken(paymentReq.token());

        String customerBankAccNum = getUserBankNumById(customerId);

        String merchantBankAccNum = getUserBankNumById(paymentReq.merchantId());

        var paymentSuccess = processPayment(customerBankAccNum, merchantBankAccNum, paymentReq.amount());
        if (paymentSuccess) {
            notifySuccessfulPayment(customerId, paymentReq.merchantId(), paymentReq.token(), paymentReq.amount(), correlationId);
        } else {
            notifyFailedPayment();
        }
    }

    public void handleTokenConsumed(Event event) {
        TokenConsumed tokenConsumed = event.getArgument(0, TokenConsumed.class);
        String correlationId = tokenConsumed.commandId();
        getCustomerIdCorrelations.get(correlationId).complete(tokenConsumed.customerId());
    }

    public void handleBankAccNumberRetrieved(Event event) {
        String bankAccNum = event.getArgument(0, String.class);
        CorrelationId correlationId = event.getArgument(1, CorrelationId.class);
        getBankAccCorrelations.get(correlationId).complete(bankAccNum);
    }


    /* Commands */

    /// Fetch the customerId by sending CONSUME_TOKEN_REQUESTED events to TokenService
    public String getCustomerIdByToken(String token) {
        String correlationId = UUID.randomUUID().toString();
        getCustomerIdCorrelations.put(correlationId, new CompletableFuture<>());
        ConsumeTokenRequested consumeTokenReq = new ConsumeTokenRequested(correlationId, token, "", 0, System.currentTimeMillis());
        queue.publish(new Event(CONSUME_TOKEN_REQUESTED, new Object[]{consumeTokenReq}));

        return getCustomerIdCorrelations.get(correlationId).join();
    }

    public String getUserBankNumById(String userId) {
        // Use userId to get customer/merchant bank account number by
        //  sending REQUEST_BANKACCNUM_BY_USER_ID events to AccountService
        CorrelationId correlationId = CorrelationId.randomId();
        getBankAccCorrelations.put(correlationId, new CompletableFuture<>());
        queue.publish(new Event(GET_BANK_ACCOUNT_REQUESTED, new Object[]{userId, correlationId}));

        return getBankAccCorrelations.get(correlationId).join();
    }

    public boolean processPayment(String customerBankAccNum, String merchantBankAccNum, int amount) {
        try {
            System.out.println("Processing payment of " + amount + " from " + customerBankAccNum + " to " + merchantBankAccNum);
            bank.transferMoneyFromTo(customerBankAccNum, merchantBankAccNum, BigDecimal.valueOf(amount), "Payment");
            return true;
        }
        catch (BankServiceException_Exception e) {return false;}
    }

    public void notifySuccessfulPayment(String customerId, String merchantId, String token, int amount, CorrelationId correlationId) {
        // Send PaymentProcessSuccess event to ReportService
        PaymentRecord record = new PaymentRecord(amount, token, customerId, merchantId);
        // No correlationId since we don't expect response
        queue.publish(new Event(BANK_TRANSFER_COMPLETED_SUCCESSFULLY, new Object[] {record}));

        // Send PAYMENT_SUCCEEDED to DTU pay server
        queue.publish(new Event(PAYMENT_SUCCEEDED, new Object[]{correlationId}));
    }

    public void notifyFailedPayment() {
        // TODO: Notify unsuccessful payment
    }
}
