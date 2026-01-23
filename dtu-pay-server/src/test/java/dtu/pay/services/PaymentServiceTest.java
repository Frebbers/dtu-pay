package dtu.pay.services;

import dtu.pay.models.PaymentRequest;
import dtu.pay.models.User;
import dtu.pay.models.exceptions.UserAlreadyExistsException;
import messaging.Event;
import messaging.MessageQueue;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

import static org.junit.jupiter.api.Assertions.assertEquals;
/// @author Wenji Xie - s242597

public class PaymentServiceTest {
    private PaymentService paymentService;
    private CompletableFuture<Event> publishedEvent;

    private MessageQueue mq = new MessageQueue() {
        @Override
        public void publish(Event event) {
            publishedEvent.complete(event);
        }

        @Override
        public void addHandler(String eventType, Consumer<Event> handler) {
        }
    };

    @BeforeEach
    void setUp() {
        paymentService = new PaymentService(mq);
    }

    @AfterEach
    void tearDown() {
        mq = null;
        paymentService = null;
    }

    @Test
    void registerSuccessfully() throws Exception {
        PaymentRequest paymentReq = new PaymentRequest("random_token_here", "merchantId_here",
                BigDecimal.valueOf(1000));
        publishedEvent = new CompletableFuture<>();

        CompletableFuture<String> resultFuture = CompletableFuture.supplyAsync(() -> {
            try {
                return paymentService.pay(paymentReq);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });

        Event request = publishedEvent.get();
        assertEquals("PaymentRequested", request.getType());

        CorrelationId correlationId = request.getArgument(1, CorrelationId.class);

        String expectedResponse = "Success!";
        paymentService.handlePaymentSuccessful(new Event(
                "PaymentSuccessful",
                new Object[] { expectedResponse, correlationId }));

        assertEquals(expectedResponse, resultFuture.get());
    }
}
