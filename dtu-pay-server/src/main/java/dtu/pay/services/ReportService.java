package dtu.pay.services;

import dtu.pay.models.CustomerReport;
import dtu.pay.models.ManagerReport;
import dtu.pay.models.MerchantReport;
import messaging.Event;
import messaging.MessageQueue;

import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

public class ReportService {
    private final MessageQueue mq;
    private final Map<CorrelationId, CompletableFuture<CustomerReport>> customerCorrelations = new ConcurrentHashMap<>();
    private final Map<CorrelationId, CompletableFuture<MerchantReport>> merchantCorrelations = new ConcurrentHashMap<>();
    private final Map<CorrelationId, CompletableFuture<ManagerReport>> managerCorrelations = new ConcurrentHashMap<>();

    public ReportService(MessageQueue mq) {
        this.mq = mq;
        mq.addHandler("CustomerReportReturned", this::handleCustomerReport);
        mq.addHandler("MerchantReportReturned", this::handleMerchantReport);
        mq.addHandler("ManagerReportReturned", this::handleManagerReport);
    }

    public CustomerReport getCustomerReport(String customerId) {
        try {
            CorrelationId correlationId = CorrelationId.randomId();
            customerCorrelations.put(correlationId, new CompletableFuture<>());
            Event event = new Event("CustomerReportRequested", customerId, correlationId);
            mq.publish(event);
            // TODO: check if joining timeout
            return customerCorrelations.get(correlationId).join();
        } catch (Exception e) {
            throw e;
        }
    }

    public MerchantReport getMerchantReport(String merchantId) {
        try {
            CorrelationId correlationId = CorrelationId.randomId();
            merchantCorrelations.put(correlationId, new CompletableFuture<>());
            Event event = new Event("MerchantReportRequested", merchantId, correlationId);
            mq.publish(event);
            // TODO: check if joining timeout
            return merchantCorrelations.get(correlationId).join();
        } catch (Exception e) {
            throw e;
        }
    }

    public ManagerReport getManagerReport() {
        try {
            CorrelationId correlationId = CorrelationId.randomId();
            managerCorrelations.put(correlationId, new CompletableFuture<>());
            Event event = new Event("ManagerReportRequested", correlationId);
            mq.publish(event);
            // TODO: check if joining timeout
            return managerCorrelations.get(correlationId).join();
        } catch (Exception e) {
            throw e;
        }
    }

    public void handleCustomerReport(Event e) {
        CustomerReport report = e.getArgument(0, CustomerReport.class);
        CorrelationId correlationId = e.getArgument(1, CorrelationId.class);
        customerCorrelations.get(correlationId).complete(report);
    }

    public void handleMerchantReport(Event e) {
        MerchantReport report = e.getArgument(0, MerchantReport.class);
        CorrelationId correlationId = e.getArgument(1, CorrelationId.class);
        merchantCorrelations.get(correlationId).complete(report);
    }

    public void handleManagerReport(Event e) {
        ManagerReport report = e.getArgument(0, ManagerReport.class);
        CorrelationId correlationId = e.getArgument(1, CorrelationId.class);
        managerCorrelations.get(correlationId).complete(report);
    }
}