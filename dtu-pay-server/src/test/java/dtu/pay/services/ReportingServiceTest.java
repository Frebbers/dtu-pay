package dtu.pay.services;

import dtu.pay.models.*;
import messaging.Event;
import messaging.MessageQueue;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ReportingServiceTest {
    private ReportingService reportingService;
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
        reportingService = new ReportingService(mq);
    }

    @AfterEach
    void tearDown() {
        mq = null;
        reportingService = null;
    }

    @Test
    void getCustomerReportSuccessfully() throws Exception {
        String customerId = "customerId";
        publishedEvent = new CompletableFuture<>();

        CompletableFuture<CustomerReport> resultFuture = CompletableFuture.supplyAsync(() -> reportingService.getCustomerReport(customerId));

        Event request = publishedEvent.get();
        assertEquals("CustomerReportRequested", request.getType());

        CorrelationId correlationId = request.getArgument(1, CorrelationId.class);

        CustomerReport expectedResponse = new CustomerReport(
                List.of(
                        new Payment(1, "token1", "merchantId1"),
                        new Payment(2, "token2", "merchantId2")
                )
        );
        reportingService.handleCustomerReport(new Event("CustomerReportReturned", expectedResponse, correlationId));

        assertEquals(expectedResponse, resultFuture.get());
    }

    @Test
    void getMerchantReportSuccessfully() throws Exception {
        String merchantId = "merchantId";
        publishedEvent = new CompletableFuture<>();

        CompletableFuture<MerchantReport> resultFuture = CompletableFuture.supplyAsync(() -> reportingService.getMerchantReport(merchantId));

        Event request = publishedEvent.get();
        assertEquals("MerchantReportRequested", request.getType());

        CorrelationId correlationId = request.getArgument(1, CorrelationId.class);

        MerchantReport expectedResponse = new MerchantReport(
                List.of(
                        new MerchantPayment(1, "token1"),
                        new MerchantPayment(2, "token2")
                )
        );
        reportingService.handleMerchantReport(new Event("MerchantReportReturned", expectedResponse, correlationId));

        assertEquals(expectedResponse, resultFuture.get());
    }

    @Test
    void getManagerReportSuccessfully() throws Exception {
        publishedEvent = new CompletableFuture<>();

        CompletableFuture<ManagerReport> resultFuture = CompletableFuture.supplyAsync(() -> reportingService.getManagerReport());

        Event request = publishedEvent.get();
        assertEquals("ManagerReportRequested", request.getType());

        CorrelationId correlationId = request.getArgument(0, CorrelationId.class);

        ManagerReport expectedResponse = new ManagerReport(
                List.of(
                        new Payment(1, "token1", "merchantId1"),
                        new Payment(2, "token2", "merchantId2")
                ),
                3
        );
        reportingService.handleManagerReport(new Event("ManagerReportReturned", expectedResponse, correlationId));

        assertEquals(expectedResponse, resultFuture.get());
    }
}