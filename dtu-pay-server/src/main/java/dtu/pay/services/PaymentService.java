package dtu.pay.services;

import dtu.pay.models.Payment;
import dtu.pay.models.PaymentRequest;
import dtu.pay.models.exceptions.UserAlreadyExistsException;
import messaging.Event;
import messaging.MessageQueue;

import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

public class PaymentService {

    private final MessageQueue mq;
    private final Map<CorrelationId, CompletableFuture<String>> correlations = new ConcurrentHashMap<>();

    public PaymentService(MessageQueue mq) {
        this.mq = mq;
        mq.addHandler("PaymentSuccessful", this::handlePaymentSuccessful);
        mq.addHandler("PaymentFailed", this::handlePaymentFailed);
    }

    public String pay(PaymentRequest paymentRequest) throws Exception {
        try {
            CorrelationId correlationId = CorrelationId.randomId();
            correlations.put(correlationId, new CompletableFuture<>());
            Event event = new Event("PaymentRequested", new Object[]{paymentRequest, correlationId});
            mq.publish(event);
            // TODO: check if joining timeout
            return correlations.get(correlationId).join();
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
        // TODO
    }
}
