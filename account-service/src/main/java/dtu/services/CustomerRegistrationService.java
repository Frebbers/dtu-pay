package dtu.services;

import java.util.concurrent.CompletableFuture;

import messaging.Event;
import messaging.MessageQueue;

public class CustomerRegistrationService {

    private MessageQueue queue;
    private CompletableFuture<Customer> registeredCustomer;

    public CustomerRegistrationService(MessageQueue q) {
        queue = q;
        queue.addHandler("CustomerIdAssigned", this::handleCustomerIdAssigned);
    }

    public Customer register(Customer s) {
        registeredCustomer = new CompletableFuture<>();
        Event event = new Event("CustomerRegistrationRequested", new Object[] { s });
        queue.publish(event);
        return registeredCustomer.join();
    }

    public void handleCustomerIdAssigned(Event e) {
        var s = e.getArgument(0, Customer.class);
        registeredCustomer.complete(s);
    }
}
