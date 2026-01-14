package dtu.pay.services;

import dtu.pay.Customer;
import dtu.pay.factories.CustomerServiceFactory;
import messaging.Event;
import messaging.MessageQueue;
import messaging.implementations.RabbitMqQueue;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

import static org.junit.jupiter.api.Assertions.*;
/// @author s224804
class CustomerServiceTest {
    private CustomerService customerService;
    private CompletableFuture<Event> publishedEvent;

    private RabbitMqQueue mq = new RabbitMqQueue() {
        @Override
        public void publish(Event event) {
            publishedEvent.complete(event);
        }
        @Override
        public void addHandler(String eventType, Consumer<Event> handler) {}
    };

    @BeforeEach
    void setUp() {
        customerService = new CustomerService(mq);
    }

    @AfterEach
    void tearDown() {
        mq = null;
        customerService = null;
    }

    @Test
    void registerSuccessfully() {
        Customer customer = new Customer("John", "Doe", "120805-1234", "12345678");
//        publishedEvent = new CompletableFuture<Event>();
//        new Thread(() -> {
//            var result = customerService.register(customer);
//
//        })
        var result = customerService.register(customer);

    }

    @Test
    void handleCustomerRegistered() {
    }
}