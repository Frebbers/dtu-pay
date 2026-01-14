package dtu.pay.services;

import dtu.pay.Merchant;
import dtu.pay.Payment;
import dtu.pay.models.exceptions.UserAlreadyExistsException;
import messaging.Event;
import messaging.MessageQueue;
import messaging.implementations.RabbitMqQueue;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

public class MerchantService {
    private final MessageQueue mq;
    private final Map<CorrelationId, CompletableFuture<String>> correlations = new ConcurrentHashMap<>();

    public MerchantService(MessageQueue mq) {
        this.mq = mq;
        mq.addHandler("MerchantRegistered", this::handleMerchantRegistered);
        mq.addHandler("MerchantNotRegistered", this::handleMerchantNotRegistered);
    }

    public String register(Merchant merchant) throws Exception, UserAlreadyExistsException {
        try {
            CorrelationId correlationId = CorrelationId.randomId();
            correlations.put(correlationId, new CompletableFuture<>());
            Event event = new Event("MerchantRegistrationRequested", new Object[]{merchant, correlationId});
            mq.publish(event);
            // TODO: check if joining timeout
            return correlations.get(correlationId).join();
        } catch (Exception e) {
            throw e;
        }
    }

    public void handleMerchantRegistered(Event e){
        String merchant = e.getArgument(0, String.class);
        CorrelationId correlationId = e.getArgument(1, CorrelationId.class);
        correlations.get(correlationId).complete(merchant);
    }

    public void handleMerchantNotRegistered(Event e) {
        String error = e.getArgument(0, String.class);
        CorrelationId correlationId = e.getArgument(1, CorrelationId.class);
        if(error.toLowerCase().contains("already exists")) {
            correlations.get(correlationId).completeExceptionally(new UserAlreadyExistsException());
        }
        correlations.get(correlationId).completeExceptionally(new Exception(error));
    }

    public void unregisterMerchantById(String id) {
    }

    public boolean pay(int amount, String cid, String mid) {
        throw new UnsupportedOperationException();
    }

    public Object getLatestError() {
        throw new UnsupportedOperationException();
    }

    public List<Payment> getPayments() {
        throw new UnsupportedOperationException();
    }
}
