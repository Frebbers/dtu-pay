package payment.service;

import dtu.ws.fastmoney.*;
import messaging.Event;
import messaging.MessageQueue;
import payment.service.models.ConsumeTokenRequested;
import payment.service.models.PaymentRecord;
import payment.service.models.PaymentReq;
import payment.service.models.TokenConsumed;

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
    String CONSUME_TOKEN_REQUESTED = "token.commands.ConsumeTokenRequested";
    String TOKEN_CONSUMED = "token.events.TokenConsumed";

    // Get customerId by token
    private Map<String, CompletableFuture<String>> getCustomerIdCorrelations = new ConcurrentHashMap<>();

    public PaymentService(MessageQueue q) {
        this.queue = q;
        queue.addHandler("PaymentRequested", this::policyPaymentRequested);
        queue.addHandler(TOKEN_CONSUMED, this::handleTokenConsumed);
    }

    /* Policies */

    public void policyPaymentRequested(Event event) {
        PaymentReq paymentReq = event.getArgument(0, PaymentReq.class);

        String customerId = getCustomerIdByToken(paymentReq.token());

        String customerBankAccNum = getUserBankNumById(customerId);

        String merchantBankAccNum = getUserBankNumById(paymentReq.merchantId());

        var paymentSuccess = processPayment(customerBankAccNum, merchantBankAccNum, paymentReq.amount());
        if (paymentSuccess) {
            notifySuccessfulPayment(customerId, paymentReq.merchantId(), paymentReq.token(), paymentReq.amount());
        } else {
            // TODO: Handle unsuccessful payments
        }
    }

    public void handleTokenConsumed(Event event) {
        TokenConsumed tokenConsumed = event.getArgument(0, TokenConsumed.class);
        String correlationId = tokenConsumed.commandId();
        getCustomerIdCorrelations.get(correlationId).complete(tokenConsumed.customerId());
    }


    /* Commands */

    public String getCustomerIdByToken(String token) {
        // Fetch the customerId by sending CONSUME_TOKEN_REQUESTED events to TokenService
        String correlationId = UUID.randomUUID().toString();
        getCustomerIdCorrelations.put(correlationId, new CompletableFuture<>());
        ConsumeTokenRequested consumeTokenReq = new ConsumeTokenRequested(correlationId, token, "", 0, System.currentTimeMillis());
        queue.publish(new Event(CONSUME_TOKEN_REQUESTED, new Object[]{consumeTokenReq}));

        return getCustomerIdCorrelations.get(correlationId).join();
    }

    public String getUserBankNumById(String userId) {
        // TODO: Use userId to get customer/merchant bank account number by
        //  sending GetBankAccNumReq events to AccountService
        return "user_bank_num_1";
    }

    public boolean processPayment(String customerBankAccNum, String merchantBankAccNum, int amount) {
        // Comment out the following code when other parts are ready.
        // Call the SOAP bank service to process payment:
//        try {
//            bank.transferMoneyFromTo(customerBankAccNum, merchantBankAccNum, BigDecimal.valueOf(amount), "Payment");
//            return true;
//        }
//        catch (BankServiceException_Exception e) {
//            return false;
//        }
        return true;
    }

    public void notifySuccessfulPayment(String customerId, String merchantId, String token, int amount) {
        // Send PaymentProcessSuccess event to ReportService
        PaymentRecord record = new PaymentRecord(customerId, merchantId, token, amount);
        // No correlationId since we don't expect response
        queue.publish(new Event("PaymentProcessSuccess", new Object[] {record}));
    }
}
