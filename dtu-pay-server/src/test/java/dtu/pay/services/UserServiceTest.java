package dtu.pay.services;

import dtu.pay.models.User;
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
class UserServiceTest {
    private UserService userService;
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
        userService = new UserService(mq);
    }

    @AfterEach
    void tearDown() {
        mq = null;
        userService = null;
    }

//    @Test
//    void registerSuccessfully() {
//        User User = new User("John", "Doe", "120805-1234", "12345678");
//        publishedEvent = new CompletableFuture<Event>();
//        new Thread(() -> {
//            var result = UserService.register(User);
//        }).start();
//        mq.publish(new Event("UserRegistered"));
//
//    }


    @Test
    void registerSuccessfully() throws Exception {
        User user = new User("John", "Doe", "12345678");
        publishedEvent = new CompletableFuture<>();

        CompletableFuture<String> resultFuture = CompletableFuture.supplyAsync(() -> {
            try {
                return userService.register(user);
            } catch (UserAlreadyExistsException | Exception e) {
                throw new RuntimeException(e);
            }
        });

        Event request = publishedEvent.get(); // the "UserRegistrationRequested" event
        assertEquals("UserRegistrationRequested", request.getType());

        CorrelationId correlationId = request.getArgument(1, CorrelationId.class);

        String expectedResponse = "Success!";
        userService.handleUserRegistered(new Event(
                "UserRegistered",
                new Object[] { expectedResponse, correlationId }
        ));

        assertEquals(expectedResponse, resultFuture.get());
    }
    @Test
    void handleUserRegistered() {
    }
}