package dtu.reporting;

import dtu.reporting.events.ReportEvent;
import dtu.reporting.models.CorrelationId;
import dtu.reporting.models.Payment;
import dtu.reporting.models.report.CustomerReport;
import dtu.reporting.models.report.ManagerReport;
import dtu.reporting.models.report.MerchantReport;
import dtu.reporting.repositories.ReportRepository;
import dtu.reporting.repositories.ReportRepositoryImpl;
import dtu.reporting.services.ReportingService;
import io.cucumber.java.Before;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import messaging.Event;
import messaging.MessageQueue;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
/// @author Elias Mortensen - s235109

public class ReportingServiceSteps {
    private TestMessageQueue mq;
    private final String customerId = "customerId";
    private final String merchantId = "merchantId";
    private CorrelationId correlationId;
    Payment payment = new Payment(100, "token", customerId, merchantId);

    public static class TestMessageQueue implements MessageQueue {
        private final Map<String, Consumer<Event>> handlers = new HashMap<>();
        private final List<Event> published = new ArrayList<>();

        @Override
        public void publish(Event event) {
            published.add(event);
        }

        @Override
        public void addHandler(String topic, Consumer<Event> handler) {
            handlers.put(topic, handler);
        }

        public Event getLastPublished() {
            if (published.isEmpty())
                return null;
            return published.getLast();
        }

        public void send(Event event) {
            Consumer<Event> handler = handlers.get(event.getTopic());
            if (handler != null)
                handler.accept(event);
        }
    }

    @Before
    public void setup() {
        mq = new TestMessageQueue();
        ReportRepository reportRepository = new ReportRepositoryImpl();
        new ReportingService(mq, reportRepository);
    }

    @Given("the BankTransferCompletedSuccessfully event is received")
    public void theBankTransferCompletedSuccessfullyEventIsReceived() {
        Event event = new Event(ReportEvent.BANK_TRANSFER_COMPLETED_SUCCESSFULLY, payment);
        mq.send(event);
    }

    @When("the {string} event is received")
    public void theEventIsReceived(String eventType) {
        correlationId = CorrelationId.randomId();
        Event event;

        switch (eventType) {
            case ReportEvent.CUSTOMER_REPORT_REQUESTED -> {
                event = new Event(eventType, customerId, correlationId);
                mq.send(event);
            }
            case ReportEvent.MERCHANT_REPORT_REQUESTED -> {
                event = new Event(eventType, merchantId, correlationId);
                mq.send(event);
            }
            case ReportEvent.MANAGER_REPORT_REQUESTED -> {
                event = new Event(eventType, correlationId);
                mq.send(event);
            }
        }
    }

    @Then("the {string} event is published and the report is returned to the customer")
    public void theEventIsPublishedAndTheReportIsReturnedToTheCustomer(String expectedEventType) {
        Event lastEvent = mq.getLastPublished();
        assertNotNull(lastEvent, "No published events");
        assertEquals(expectedEventType, lastEvent.getTopic());

        CorrelationId actualCorrelationId = lastEvent.getArgument(1, CorrelationId.class);
        assertEquals(correlationId, actualCorrelationId);

        CustomerReport expectedReport = new CustomerReport();
        expectedReport.add(payment);
        CustomerReport report = lastEvent.getArgument(0, CustomerReport.class);
        assertNotNull(report);
        assertEquals(expectedReport, report);
    }

    @Then("the {string} event is published and the report is returned to the merchant")
    public void theEventIsPublishedAndTheReportIsReturnedToTheMerchant(String expectedEventType) {
        Event lastEvent = mq.getLastPublished();
        assertNotNull(lastEvent, "No published events");
        assertEquals(expectedEventType, lastEvent.getTopic());

        CorrelationId actualCorrelationId = lastEvent.getArgument(1, CorrelationId.class);
        assertEquals(correlationId, actualCorrelationId);

        MerchantReport expectedReport = new MerchantReport();
        expectedReport.add(payment);
        MerchantReport report = lastEvent.getArgument(0, MerchantReport.class);
        assertNotNull(report);
        assertEquals(expectedReport, report);
    }

    @Then("the {string} event is published and the report is returned to the manager")
    public void theEventIsPublishedAndTheReportIsReturnedToTheManager(String expectedEventType) {
        Event lastEvent = mq.getLastPublished();
        assertNotNull(lastEvent, "No published events");
        assertEquals(expectedEventType, lastEvent.getTopic());

        CorrelationId actualCorrelationId = lastEvent.getArgument(1, CorrelationId.class);
        assertEquals(correlationId, actualCorrelationId);

        ManagerReport expectedReport = new ManagerReport();
        expectedReport.add(payment);
        ManagerReport report = lastEvent.getArgument(0, ManagerReport.class);
        assertNotNull(report);
        assertEquals(expectedReport, report);
    }
}
