package payment.service;

import dtu.ws.fastmoney.*;
import messaging.Event;
import messaging.MessageQueue;
import payment.service.models.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * PaymentService handles payment processing by listening for three events:
 * - PaymentRequested: initiates a payment context
 * - BankAccountRetrieved: provides bank account info for customer or merchant
 * - BankAccountRetrievalFailed: marks payment as failed
 *
 * Each event independently updates the PaymentContext. Once all required data
 * is available, the payment is processed. Contexts that exceed the timeout
 * are automatically expired by a background thread.
 */
public class PaymentService {
    private static final long DEFAULT_EXPIRATION_SECONDS = 5;
    private static final long EXPIRATION_CHECK_INTERVAL_MS = 1000;

    private final BankService bank;
    private final MessageQueue queue;
    private final ScheduledExecutorService scheduler;
    private final long expirationSeconds;
    private final Map<CorrelationId, PaymentContext> paymentContexts = new ConcurrentHashMap<>();

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
        this(q, bank, Executors.newSingleThreadScheduledExecutor(), DEFAULT_EXPIRATION_SECONDS);
    }

    public PaymentService(MessageQueue q, BankService bank, ScheduledExecutorService scheduler, long expirationSeconds) {
        this.queue = q;
        this.bank = bank;
        this.scheduler = scheduler;
        this.expirationSeconds = expirationSeconds;

        queue.addHandler(PAYMENT_REQUESTED, this::handlePaymentRequested);
        queue.addHandler(BANK_ACCOUNT_RETRIEVED, this::handleBankAccountRetrieved);
        queue.addHandler(BANK_ACCOUNT_RETRIEVAL_FAILED, this::handleBankAccountRetrievalFailed);

        startExpirationChecker();
    }

    public void shutdown() {
        scheduler.shutdown();
        try {
            if (!scheduler.awaitTermination(5, TimeUnit.SECONDS)) {
                scheduler.shutdownNow();
            }
        } catch (InterruptedException e) {
            scheduler.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }

    public void handlePaymentRequested(Event event) {
        PaymentReq paymentReq;
        CorrelationId correlationId;
        try {
            paymentReq = event.getArgument(0, PaymentReq.class);
            correlationId = event.getArgument(1, CorrelationId.class);
        } catch (Exception e) {
            return;
        }

        PaymentContext context = getOrCreateContext(correlationId);
        synchronized (context) {
            context.paymentReq = paymentReq;
        }
        tryProcessPayment(correlationId);
    }

    public void handleBankAccountRetrieved(Event event) {
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

        PaymentContext context = getOrCreateContext(correlationId);
        synchronized (context) {
            // Store the bank account - we'll determine which one it is when we have the payment request
            context.addBankAccount(userId, bankAccNum);
        }
        tryProcessPayment(correlationId);
    }

    public void handleBankAccountRetrievalFailed(Event event) {
        String errorMessage;
        CorrelationId correlationId;
        try {
            errorMessage = event.getArgument(0, String.class);
            correlationId = event.getArgument(1, CorrelationId.class);
        } catch (Exception e) {
            return;
        }

        PaymentContext context = getOrCreateContext(correlationId);
        synchronized (context) {
            context.failed = true;
            context.errorMessage = errorMessage;
        }
        tryProcessPayment(correlationId);
    }

    /* Core Logic */

    private PaymentContext getOrCreateContext(CorrelationId correlationId) {
        return paymentContexts.computeIfAbsent(correlationId, id -> new PaymentContext());
    }

    private void tryProcessPayment(CorrelationId correlationId) {
        PaymentContext context = paymentContexts.get(correlationId);
        if (context == null) {
            return;
        }

        synchronized (context) {
            // Check if already processed
            if (context.processed) {
                return;
            }

            // Check for failure condition
            if (context.failed) {
                context.processed = true;
                paymentContexts.remove(correlationId);
                notifyFailedPayment(correlationId, context.errorMessage);
                return;
            }

            // Check if we have all required data
            if (!context.isReady()) {
                return;
            }

            // Mark as processed before releasing lock
            context.processed = true;
        }

        // Remove from map
        paymentContexts.remove(correlationId);

        // Process the payment (outside synchronized block)
        PaymentReq req = context.paymentReq;
        String customerBankAccNum = context.getCustomerBankAccNum();
        String merchantBankAccNum = context.getMerchantBankAccNum();
        String customerId = context.customerId;

        boolean success = processPayment(customerBankAccNum, merchantBankAccNum, req.amount());
        if (success) {
            notifySuccessfulPayment(customerId, req.merchantId(), req.token(), req.amount(), correlationId);
        } else {
            notifyFailedPayment(correlationId, "Bank transfer failed");
        }
    }

    private void startExpirationChecker() {
        scheduler.scheduleAtFixedRate(this::checkExpiredContexts,
                EXPIRATION_CHECK_INTERVAL_MS, EXPIRATION_CHECK_INTERVAL_MS, TimeUnit.MILLISECONDS);
    }

    private void checkExpiredContexts() {
        Instant expirationThreshold = Instant.now().minusSeconds(expirationSeconds);
        Iterator<Map.Entry<CorrelationId, PaymentContext>> iterator = paymentContexts.entrySet().iterator();

        while (iterator.hasNext()) {
            Map.Entry<CorrelationId, PaymentContext> entry = iterator.next();
            CorrelationId correlationId = entry.getKey();
            PaymentContext context = entry.getValue();

            synchronized (context) {
                if (context.processed) {
                    continue;
                }
                if (context.creationTime.isBefore(expirationThreshold)) {
                    context.processed = true;
                    iterator.remove();
                    notifyFailedPayment(correlationId, "Payment timed out waiting for required information");
                }
            }
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
        PaymentRecord record = new PaymentRecord(amount, token, customerId, merchantId);
        queue.publish(new Event(BANK_TRANSFER_COMPLETED_SUCCESSFULLY, new Object[] { record }));
        queue.publish(new Event(PAYMENT_SUCCEEDED, new Object[] { "OK", correlationId }));
    }

    public void notifyFailedPayment(CorrelationId correlationId, String error) {
        queue.publish(new Event(PAYMENT_FAILED, new Object[] { error, correlationId }));
    }

    /* Payment Context */

    private static class PaymentContext {
        private final Instant creationTime = Instant.now();
        private final Map<String, String> bankAccounts = new ConcurrentHashMap<>();

        private PaymentReq paymentReq;
        private String customerId;
        private boolean failed = false;
        private String errorMessage;
        private boolean processed = false;

        void addBankAccount(String userId, String bankAccNum) {
            bankAccounts.put(userId, bankAccNum);
            // If we already have the payment request, determine which account this is
            if (paymentReq != null && !userId.equals(paymentReq.merchantId())) {
                customerId = userId;
            }
        }

        boolean isReady() {
            if (paymentReq == null) {
                return false;
            }
            // Need both customer and merchant bank accounts
            String merchantId = paymentReq.merchantId();
            boolean hasMerchant = bankAccounts.containsKey(merchantId);

            // Customer is anyone who is not the merchant
            String customerKey = null;
            for (String key : bankAccounts.keySet()) {
                if (!key.equals(merchantId)) {
                    customerKey = key;
                    customerId = key;
                    break;
                }
            }
            return hasMerchant && customerKey != null;
        }

        String getCustomerBankAccNum() {
            return bankAccounts.get(customerId);
        }

        String getMerchantBankAccNum() {
            return paymentReq != null ? bankAccounts.get(paymentReq.merchantId()) : null;
        }
    }
}
