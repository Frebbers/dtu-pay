package dtu.pay.services;

import dtu.pay.events.ReportEvent;
import dtu.pay.models.report.CustomerReport;
import dtu.pay.models.report.ManagerReport;
import dtu.pay.models.report.MerchantReport;
import dtu.pay.models.report.Report;
import messaging.Event;
import messaging.MessageQueue;

import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
/// @author Elias Mortensen - s235109

public class ReportingService {
    private final MessageQueue mq;
    private final Map<CorrelationId, CompletableFuture<CustomerReport>> customerCorrelationMap = new ConcurrentHashMap<>();
    private final Map<CorrelationId, CompletableFuture<MerchantReport>> merchantCorrelationMap = new ConcurrentHashMap<>();
    private final Map<CorrelationId, CompletableFuture<ManagerReport>> managerCorrelationMap = new ConcurrentHashMap<>();

    public ReportingService(MessageQueue mq) {
        this.mq = mq;
        mq.addHandler(ReportEvent.CUSTOMER_REPORT_RETURNED, this::handleCustomerReport);
        mq.addHandler(ReportEvent.MERCHANT_REPORT_RETURNED, this::handleMerchantReport);
        mq.addHandler(ReportEvent.MANAGER_REPORT_RETURNED, this::handleManagerReport);
    }

    private <T extends Report> T requestReport(
            Map<CorrelationId, CompletableFuture<T>> correlationMap,
            Function<CorrelationId, Event> eventFactory) {
        try {
            var correlationId = CorrelationId.randomId();
            var future = new CompletableFuture<T>();
            correlationMap.put(correlationId, future);
            Event event = eventFactory.apply(correlationId);
            mq.publish(event);
            return future.join();
        } catch (Exception e) {
            throw e;
        }
    }

    public CustomerReport getCustomerReport(String customerId) {
        return requestReport(
                customerCorrelationMap,
                corrId -> new Event(ReportEvent.CUSTOMER_REPORT_REQUESTED, customerId, corrId));
    }

    public MerchantReport getMerchantReport(String merchantId) {
        return requestReport(
                merchantCorrelationMap,
                corrId -> new Event(ReportEvent.MERCHANT_REPORT_REQUESTED, merchantId, corrId));
    }

    public ManagerReport getManagerReport() {
        return requestReport(
                managerCorrelationMap,
                corrId -> new Event(ReportEvent.MANAGER_REPORT_REQUESTED, corrId));
    }

    public void handleCustomerReport(Event e) {
        CustomerReport report = e.getArgument(0, CustomerReport.class);
        CorrelationId correlationId = e.getArgument(1, CorrelationId.class);
        customerCorrelationMap.get(correlationId).complete(report);
    }

    public void handleMerchantReport(Event e) {
        MerchantReport report = e.getArgument(0, MerchantReport.class);
        CorrelationId correlationId = e.getArgument(1, CorrelationId.class);
        merchantCorrelationMap.get(correlationId).complete(report);
    }

    public void handleManagerReport(Event e) {
        ManagerReport report = e.getArgument(0, ManagerReport.class);
        CorrelationId correlationId = e.getArgument(1, CorrelationId.class);
        managerCorrelationMap.get(correlationId).complete(report);
    }

    public void deleteReport() {
        mq.publish(new Event(ReportEvent.DELETE_REPORT));
    }
}