package payment.service;

import dtu.ws.fastmoney.*;
import messaging.Event;
import messaging.MessageQueue;
import payment.service.models.*;

import java.math.BigDecimal;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

public class PaymentService {
    BankService bank;
    MessageQueue queue;
    private final Map<CorrelationId, PaymentContext> paymentContexts = new ConcurrentHashMap<>();
    private final Map<CorrelationId, Map<String, String>> pendingBankAccountEvents = new ConcurrentHashMap<>();

    // Event names
    String PAYMENT_REQUESTED = "PaymentRequested";
    String PAYMENT_SUCCEEDED = "PaymentSuccessful";
    String PAYMENT_FAILED = "PaymentFailed";
    String BANK_ACCOUNT_RETRIEVAL_FAILED = "BankAccountRetrievalFailed";
    String BANK_ACCOUNT_RETRIEVED = "BankAccountRetrieved";
    String BANK_TRANSFER_COMPLETED_SUCCESSFULLY = "BankTransferCompletedSuccessfully";

    public PaymentService(MessageQueue q) {
        this(q, new BankService_Service().getBankServicePort());
    }

    public PaymentService(MessageQueue q, BankService bank) {
        this.queue = q;
        this.bank = bank;
        queue.addHandler(PAYMENT_REQUESTED, this::policyPaymentRequested);
        queue.addHandler(BANK_ACCOUNT_RETRIEVED, this::handleBankAccNumberRetrieved);
        queue.addHandler(BANK_ACCOUNT_RETRIEVAL_FAILED, this::handleBankAccNumRetrievalFailed);
    }

    /* Policies */

    public void policyPaymentRequested(Event event) {
        PaymentReq paymentReq;
        CorrelationId correlationId;
        try {
            paymentReq = event.getArgument(0, PaymentReq.class);
            correlationId = event.getArgument(1, CorrelationId.class);
        } catch (Exception e) {
            return;
        }

        PaymentContext context = new PaymentContext(paymentReq);
        paymentContexts.put(correlationId, context);
        // If we got a bank account retrieved event before the payment request, apply it here
        Map<String, String> pending = pendingBankAccountEvents.remove(correlationId);
        if (pending != null) {
            for (Map.Entry<String, String> entry : pending.entrySet()) {
                applyBankAccountEvent(context, entry.getKey(), entry.getValue());
            }
        }

        // Run asynchronously to avoid blocking the message handler thread
        CompletableFuture.allOf(context.customerBankAccFuture, context.merchantBankAccFuture)
                .orTimeout(5, TimeUnit.SECONDS)
                .whenComplete((result, ex) -> {
                    try {
                        if (ex != null) {
                            String errorMessage = ex.getMessage();
                            if (errorMessage == null || errorMessage.isEmpty()) {
                                errorMessage = "Payment timed out waiting for bank account information";
                            }
                            notifyFailedPayment(correlationId, errorMessage);
                            return;
                        }

                        String customerBankAccNum = context.customerBankAccFuture.join();
                        String merchantBankAccNum = context.merchantBankAccFuture.join();
                        String customerId = context.customerId != null ? context.customerId : paymentReq.token();

                        boolean paymentSuccess = processPayment(customerBankAccNum, merchantBankAccNum, paymentReq.amount());
                        if (paymentSuccess) {
                            notifySuccessfulPayment(customerId, paymentReq.merchantId(), paymentReq.token(), paymentReq.amount(),
                                    correlationId);
                        } else {
                            notifyFailedPayment(correlationId, "Bank transfer failed");
                        }
                    } finally {
                        paymentContexts.remove(correlationId);
                    }
                });
    }

    /// Handles the event when a bank account number is retrieved by putting it into
    public void handleBankAccNumberRetrieved(Event event) {
        String userId;
        String bankAccNum;
        CorrelationId correlationId;
        try {
            userId = event.getArgument(0, String.class);
            bankAccNum = event.getArgument(1, String.class);
            correlationId = event.getArgument(2, CorrelationId.class);
        } catch (Exception e) {
            return;
        }

        PaymentContext context = paymentContexts.get(correlationId);
        if (context == null) {
            // Store in pending and also schedule a retry in case of race condition
            pendingBankAccountEvents
                    .computeIfAbsent(correlationId, id -> new ConcurrentHashMap<>())
                    .put(userId, bankAccNum);

            // Brief delay then retry - handles race where PaymentRequested is being processed
            CompletableFuture.delayedExecutor(50, TimeUnit.MILLISECONDS).execute(() -> {
                PaymentContext retryContext = paymentContexts.get(correlationId);
                if (retryContext != null) {
                    applyBankAccountEvent(retryContext, userId, bankAccNum);
                }
            });
            return;
        }
        applyBankAccountEvent(context, userId, bankAccNum);
    }

    public void handleBankAccNumRetrievalFailed(Event event) {
        String errorMessage;
        CorrelationId correlationId;
        try {
            errorMessage = event.getArgument(0, String.class);
            correlationId = event.getArgument(1, CorrelationId.class);
        } catch (Exception e) {
            return;
        }
        PaymentContext context = paymentContexts.get(correlationId);
        if (context != null) {
            context.customerBankAccFuture.completeExceptionally(new RuntimeException(errorMessage));
            context.merchantBankAccFuture.completeExceptionally(new RuntimeException(errorMessage));
            return;
        }
        notifyFailedPayment(correlationId, errorMessage);
    }

    /// Applies the bank account event to the appropriate future in the payment context
    private void applyBankAccountEvent(PaymentContext context, String userId, String bankAccNum) {
        if (userId.equals(context.paymentReq.merchantId())) {
            context.merchantBankAccFuture.complete(bankAccNum);
        } else if (context.customerId == null || userId.equals(context.customerId)) {
            context.customerId = userId;
            context.customerBankAccFuture.complete(bankAccNum);
        }
    }

    public boolean processPayment(String customerBankAccNum, String merchantBankAccNum, BigDecimal amount) {
        try {
            System.out.println(
                    "Processing payment of " + amount + " from " + customerBankAccNum + " to " + merchantBankAccNum);
            bank.transferMoneyFromTo(customerBankAccNum, merchantBankAccNum, amount, "Payment");
            return true;
        } catch (BankServiceException_Exception e) {
            return false;
        }
    }

    public void notifySuccessfulPayment(String customerId, String merchantId, String token, BigDecimal amount,
            CorrelationId correlationId) {
        // Send PaymentProcessSuccess event to ReportService
        PaymentRecord record = new PaymentRecord(amount, token, customerId, merchantId);
        // No correlationId since we don't expect response
        queue.publish(new Event(BANK_TRANSFER_COMPLETED_SUCCESSFULLY, new Object[] { record }));

        // Send PAYMENT_SUCCEEDED to DTU pay server
        queue.publish(new Event(PAYMENT_SUCCEEDED, new Object[] { "OK", correlationId }));
    }

    public void notifyFailedPayment(CorrelationId correlationId, String error) {
        queue.publish(new Event(PAYMENT_FAILED, new Object[] { error, correlationId }));
    }

    private static class PaymentContext {
        private final PaymentReq paymentReq;
        private final CompletableFuture<String> customerBankAccFuture = new CompletableFuture<>();
        private final CompletableFuture<String> merchantBankAccFuture = new CompletableFuture<>();
        private String customerId;

        private PaymentContext(PaymentReq paymentReq) {
            this.paymentReq = paymentReq;
        }
    }
}
