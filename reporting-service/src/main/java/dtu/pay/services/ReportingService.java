package dtu.pay.services;

import dtu.pay.models.*;
import dtu.pay.repositories.ReportRepository;
import messaging.Event;
import messaging.MessageQueue;

public class ReportingService {
    private final MessageQueue mq;
    private final ReportRepository reportRepository;

    public ReportingService(MessageQueue mq, ReportRepository reportRepository) {
        this.mq = mq;
        this.reportRepository = reportRepository;
        mq.addHandler("BankTransferCompletedSuccessfully", this::handleCompletedBankTransfer);
        mq.addHandler("CustomerReportRequested", this::handleCustomerReportRequest);
        mq.addHandler("MerchantReportRequested", this::handleMerchantReport);
        mq.addHandler("ManagerReportRequested", this::handleManagerReport);
    }

    private void handleCompletedBankTransfer(Event event) {
        Payment payment = event.getArgument(0, Payment.class); // TODO: check
        reportRepository.createReport(payment);
    }

    private void handleCustomerReportRequest(Event e) {
        String customerId = e.getArgument(0, String.class);
        CorrelationId correlationId = e.getArgument(1, CorrelationId.class);
        CustomerReport customerReport = reportRepository.getCustomerReport(customerId);
        Event event = new Event("CustomerReportReturned", customerReport, correlationId);
        mq.publish(event);
    }

    private void handleMerchantReport(Event e) {
        String merchantId = e.getArgument(0, String.class);
        CorrelationId correlationId = e.getArgument(1, CorrelationId.class);
        MerchantReport merchantReport = reportRepository.getMerchantReport(merchantId);
        Event event = new Event("MerchantReportReturned", merchantReport, correlationId);
        mq.publish(event);
    }

    private void handleManagerReport(Event e) {
        CorrelationId correlationId = e.getArgument(0, CorrelationId.class);
        ManagerReport managerReport = reportRepository.getManagerReport();
        Event event = new Event("ManagerReportReturned", managerReport, correlationId);
        mq.publish(event);
    }
}
