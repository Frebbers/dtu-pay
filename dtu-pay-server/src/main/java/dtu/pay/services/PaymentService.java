package dtu.pay.services;

import dtu.pay.models.Payment;
import dtu.pay.models.PaymentRequest;
import messaging.Event;
import messaging.MessageQueue;

import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

public class PaymentService {

    private final MessageQueue mq;
    private final Map<CorrelationId, CompletableFuture<String>> correlations = new ConcurrentHashMap<>();

    public PaymentService(MessageQueue mq) {
        this.mq = mq;
        mq.addHandler("PaymentSuccessful", this::handlePaymentSuccessful);
        mq.addHandler("PaymentFailed", this::handlePaymentFailed);
    }

    /// Forward payment request to message queue
    /// @param paymentRequest the request for payment containing a merchant id, an amount and a token
    public String pay(PaymentRequest paymentRequest) throws Exception {
        try {
            CorrelationId correlationId = CorrelationId.randomId();
            CompletableFuture<String> future = new CompletableFuture<>();
            correlations.put(correlationId, future);
            Event event = new Event("PaymentRequested", new Object[]{paymentRequest, correlationId});
            mq.publish(event);
            return future.orTimeout(5, TimeUnit.SECONDS).join();
        } catch (Exception e) {
            throw e;
        }
    }

    public void handlePaymentSuccessful(Event e){
        String returnedInfo = e.getArgument(0, String.class);
        CorrelationId correlationId = e.getArgument(1, CorrelationId.class);
        correlations.get(correlationId).complete(returnedInfo);
    }

    public void handlePaymentFailed(Event e) {
        String error = e.getArgument(0, String.class);
        CorrelationId correlationId = e.getArgument(1, CorrelationId.class);
        CompletableFuture<String> future = correlations.remove(correlationId);
        if (future == null) {
            return;
        }
        String message = (error == null || error.isBlank()) ? "Payment failed" : error;
        future.completeExceptionally(new RuntimeException(message));
    }
}
