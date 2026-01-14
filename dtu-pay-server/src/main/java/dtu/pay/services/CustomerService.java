package dtu.pay.services;

import dtu.pay.Customer;
import dtu.pay.Payment;
import messaging.Event;
import messaging.MessageQueue;
import messaging.implementations.RabbitMqQueue;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

/// @author s253156
public class CustomerService {
    private final MessageQueue mq;
    private final Map<CorrelationId, CompletableFuture<String>> correlations = new ConcurrentHashMap<>();

    public CustomerService(MessageQueue mq) {
        this.mq = mq;
        mq.addHandler("CustomerRegistered", this::handleCustomerRegistered);
        mq.addHandler("CustomerNotRegistered", this::handleCustomerNotRegistered);
    }

    public String register(Customer customer) {
        try {
            CorrelationId correlationId = CorrelationId.randomId();
            correlations.put(correlationId, new CompletableFuture<>());
            Event event = new Event("CustomerRegistrationRequested", new Object[]{customer, correlationId});
            mq.publish(event);
            return correlations.get(correlationId).join();
        } catch (Exception e) {
            // TODO
            return "";
        }
    }

    public void handleCustomerRegistered(Event e) {
        String customer = e.getArgument(0, String.class);
        CorrelationId correlationId = e.getArgument(1, CorrelationId.class);
        correlations.get(correlationId).complete(customer);
    }

    public void handleCustomerNotRegistered(Event e) {

    }

    public void unregisterCustomerById(String id) {
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
