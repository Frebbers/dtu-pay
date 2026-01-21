package payment.service;

import dtu.ws.fastmoney.*;
import messaging.Event;
import messaging.MessageQueue;
import payment.service.models.*;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class PaymentService {
    BankService bank;
    MessageQueue queue;
    Map<UUID, PaymentReq> paymentHashMap = new HashMap<>();

    // Event names
    String PAYMENT_REQUESTED = "PaymentRequested";
    String PAYMENT_SUCCEEDED = "PaymentSuccessful";
    String PAYMENT_FAILED = "PaymentFailed";
    String TOKEN_CONSUME_REQUESTED = "ConsumeTokenRequested";
    String TOKEN_CONSUMED = "TokenConsumed";
    String TOKEN_CONSUMPTION_REJECTED = "TokenConsumptionRejected";
    String BANK_ACCOUNT_REQUESTED = "GetBankAccount";
    String BANK_ACCOUNT_RETRIEVAL_FAILED = "BankAccountRetrievalFailed";
    String BANK_ACCOUNT_RETRIEVED = "BankAccountRetrieved";
    String BANK_TRANSFER_COMPLETED_SUCCESSFULLY = "BankTransferCompletedSuccessfully";

    private final Map<CorrelationId, PaymentContext> paymentContexts = new ConcurrentHashMap<>();

    public PaymentService(MessageQueue q) {
        this(q, new BankService_Service().getBankServicePort());
    }

    public PaymentService(MessageQueue q, BankService bank) {
        this.queue = q;
        this.bank = bank;
        queue.addHandler(PAYMENT_REQUESTED, this::policyPaymentRequested);
        queue.addHandler(TOKEN_CONSUMED, this::handleTokenConsumed);
        queue.addHandler(TOKEN_CONSUMPTION_REJECTED, this::handleTokenConsumptionRejected);
        queue.addHandler(BANK_ACCOUNT_RETRIEVED, this::handleBankAccNumberRetrieved);
        queue.addHandler(BANK_ACCOUNT_RETRIEVAL_FAILED, this::handleBankAccNumRetrievalFailed);
    }

    /* Policies */

    public void policyPaymentRequested(Event event) {
        PaymentReq paymentReq = event.getArgument(0, PaymentReq.class);
        CorrelationId correlationId = event.getArgument(1, CorrelationId.class);

        PaymentContext context = new PaymentContext(correlationId, paymentReq);
        paymentContexts.put(correlationId, context);
        requestTokenConsumption(context);
        requestBankAccount(paymentReq.merchantId(), correlationId);
    }

    public void handleTokenConsumed(Event event) {
        TokenConsumed tokenConsumed = event.getArgument(0, TokenConsumed.class);
        CorrelationId correlationId = tryGetArgument(event, 1, CorrelationId.class);
        if (correlationId == null) {
            correlationId = correlationIdFromCommandId(tokenConsumed.commandId());
        }
        if (correlationId == null) {
            return;
        }

        PaymentContext context = paymentContexts.get(correlationId);
        if (context == null) {
            return;
        }

        context.customerId = tokenConsumed.customerId();
        if (context.customerBankAccNum == null && context.customerId != null) {
            requestBankAccount(context.customerId, correlationId);
        }
        attemptProcessPayment(context);
    }

    public void handleTokenConsumptionRejected(Event event) {
        TokenConsumptionRejected rejected = event.getArgument(0, TokenConsumptionRejected.class);
        CorrelationId correlationId = tryGetArgument(event, 1, CorrelationId.class);
        if (correlationId == null) {
            correlationId = correlationIdFromCommandId(rejected.commandId());
        }
        if (correlationId == null) {
            return;
        }

        notifyFailedPayment(correlationId, rejected.reason());
        clearContext(correlationId);
    }

    public void handleBankAccNumberRetrieved(Event event) {
        String bankAccNum = event.getArgument(0, String.class);
        CorrelationId correlationId = tryGetArgument(event, 1, CorrelationId.class);
        String cpr = null;
        if (correlationId == null) {
            cpr = tryGetArgument(event, 1, String.class);
            correlationId = tryGetArgument(event, 2, CorrelationId.class);
        }
        PaymentContext context = paymentContexts.get(correlationId);
        if (context == null) {
            return;
        }

        if (cpr != null && !cpr.isBlank()) {
            if (cpr.equals(context.paymentReq.merchantId())) {
                if (context.merchantBankAccNum == null) {
                    context.merchantBankAccNum = bankAccNum;
                }
            } else {
                if (context.customerBankAccNum == null) {
                    context.customerBankAccNum = bankAccNum;
                }
                if (context.customerId == null) {
                    context.customerId = cpr;
                }
            }
        } else {
            if (context.customerBankAccNum == null) {
                context.customerBankAccNum = bankAccNum;
            } else if (context.merchantBankAccNum == null) {
                context.merchantBankAccNum = bankAccNum;
            }
        }

        attemptProcessPayment(context);
    }

    public void handleBankAccNumRetrievalFailed(Event event) {
        String errorMessage = event.getArgument(0, String.class);
        CorrelationId correlationId = tryGetArgument(event, 2, CorrelationId.class);
        if (correlationId == null) {
            correlationId = tryGetArgument(event, 1, CorrelationId.class);
        }
        if (correlationId == null) {
            return;
        }
        notifyFailedPayment(correlationId, errorMessage);
        clearContext(correlationId);
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
        queue.publish(new Event(BANK_TRANSFER_COMPLETED_SUCCESSFULLY, new Object[] { record, correlationId }));

        // Send PAYMENT_SUCCEEDED to DTU pay server
        queue.publish(new Event(PAYMENT_SUCCEEDED, new Object[] { "OK", correlationId }));
    }

    public void notifyFailedPayment(CorrelationId correlationId, String error) {
        queue.publish(new Event(PAYMENT_FAILED, new Object[] { error, correlationId }));
    }

    private void clearContext(CorrelationId correlationId) {
        paymentContexts.remove(correlationId);
    }

    private CorrelationId correlationIdFromCommandId(String commandId) {
        if (commandId == null || commandId.isBlank()) {
            return null;
        }
        try {
            return new CorrelationId(UUID.fromString(commandId));
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    private <T> T tryGetArgument(Event event, int index, Class<T> cls) {
        try {
            return event.getArgument(index, cls);
        } catch (Exception e) {
            return null;
        }
    }

    private void attemptProcessPayment(PaymentContext context) {
        if (context == null) {
            return;
        }
        if (context.customerId == null || context.customerBankAccNum == null || context.merchantBankAccNum == null) {
            return;
        }
        boolean paymentSuccess = processPayment(context.customerBankAccNum, context.merchantBankAccNum,
                context.paymentReq.amount());
        if (paymentSuccess) {
            notifySuccessfulPayment(
                    context.customerId,
                    context.paymentReq.merchantId(),
                    context.paymentReq.token(),
                    context.paymentReq.amount(),
                    context.correlationId);
        } else {
            notifyFailedPayment(context.correlationId, "Bank transfer failed");
        }
        clearContext(context.correlationId);
    }

    private void requestTokenConsumption(PaymentContext context) {
        ConsumeTokenRequested command = new ConsumeTokenRequested(
                context.correlationId.id().toString(),
                context.paymentReq.token(),
                context.paymentReq.merchantId(),
                context.paymentReq.amount() == null ? null : context.paymentReq.amount().intValue(),
                System.currentTimeMillis());
        queue.publish(new Event(TOKEN_CONSUME_REQUESTED, command));
    }

    private void requestBankAccount(String cpr, CorrelationId correlationId) {
        if (cpr == null || cpr.isBlank() || correlationId == null) {
            return;
        }
        queue.publish(new Event(BANK_ACCOUNT_REQUESTED, new Object[] { cpr, correlationId }));
    }

    private static class PaymentContext {
        private final CorrelationId correlationId;
        private final PaymentReq paymentReq;
        private String customerId;
        private String customerBankAccNum;
        private String merchantBankAccNum;

        private PaymentContext(CorrelationId correlationId, PaymentReq paymentReq) {
            this.correlationId = correlationId;
            this.paymentReq = paymentReq;
        }
    }
}
