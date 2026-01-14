package dtu.pay.services;

import dtu.pay.Merchant;
import dtu.pay.models.exceptions.UserAlreadyExistsException;
import messaging.Event;
import messaging.MessageQueue;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

import static org.junit.jupiter.api.Assertions.*;
/// @author s224804
class MerchantServiceTest {
    private MerchantService merchantService;
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
        MerchantService = new MerchantService(mq);
    }

    @AfterEach
    void tearDown() {
        mq = null;
        MerchantService = null;
    }

//    @Test
//    void registerSuccessfully() {
//        Merchant Merchant = new Merchant("John", "Doe", "120805-1234", "12345678");
//        publishedEvent = new CompletableFuture<Event>();
//        new Thread(() -> {
//            var result = MerchantService.register(Merchant);
//        }).start();
//        mq.publish(new Event("MerchantRegistered"));
//
//    }


    @Test
    void registerSuccessfully() throws Exception {
        Merchant merchant = new Merchant("John", "Doe", "120805-1234", "12345678");
        publishedEvent = new CompletableFuture<>();

        CompletableFuture<String> resultFuture = CompletableFuture.supplyAsync(() -> {
            try {
                return MerchantService.register(merchant);
            } catch (UserAlreadyExistsException | Exception e) {
                throw new RuntimeException(e);
            }
        });

        Event request = publishedEvent.get(); // the "MerchantRegistrationRequested" event
        assertEquals("MerchantRegistrationRequested", request.getType());

        CorrelationId correlationId = request.getArgument(1, CorrelationId.class);

        String expectedResponse = "Success!";
        MerchantService.handleMerchantRegistered(new Event(
                "MerchantRegistered",
                new Object[] { expectedResponse, correlationId }
        ));

        assertEquals(expectedResponse, resultFuture.get());
    }
    @Test
    void handleMerchantRegistered() {
    }
}