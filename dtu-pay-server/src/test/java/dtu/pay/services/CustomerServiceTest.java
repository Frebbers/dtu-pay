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

    private MessageQueue mq = new MessageQueue() {
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

//    @Test
//    void registerSuccessfully() {
//        Customer customer = new Customer("John", "Doe", "120805-1234", "12345678");
//        publishedEvent = new CompletableFuture<Event>();
//        new Thread(() -> {
//            var result = customerService.register(customer);
//        }).start();
//        mq.publish(new Event("CustomerRegistered"));
//
//    }


    @Test
    void registerSuccessfully() throws Exception {
        Customer customer = new Customer("John", "Doe", "120805-1234", "12345678");
        publishedEvent = new CompletableFuture<>();

        CompletableFuture<String> resultFuture = CompletableFuture.supplyAsync(() -> customerService.register(customer));

        Event request = publishedEvent.get(); // the "CustomerRegistrationRequested" event
        assertEquals("CustomerRegistrationRequested", request.getType());

        CorrelationId correlationId = request.getArgument(1, CorrelationId.class);

        String expectedResponse = "Success!";
        customerService.handleCustomerRegistered(new Event(
                "CustomerRegistered",
                new Object[] { expectedResponse, correlationId }
        ));

        assertEquals(expectedResponse, resultFuture.get());
    }
    @Test
    void handleCustomerRegistered() {
    }
}