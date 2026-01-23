package dtu.reporting.services;

import dtu.reporting.events.ReportEvent;
import dtu.reporting.models.*;
import dtu.reporting.models.report.CustomerReport;
import dtu.reporting.models.report.ManagerReport;
import dtu.reporting.models.report.MerchantReport;
import dtu.reporting.repositories.ReportRepository;
import dtu.reporting.repositories.ReportRepositoryImpl;
import messaging.Event;
import messaging.MessageQueue;

/// @author Frederik Bode Hendrichsen - s224804
public class ReportingService {
    private final MessageQueue mq;
    private final ReportRepository reportRepository;

    public ReportingService(MessageQueue mq) {
        this(mq, new ReportRepositoryImpl());
    }

    public ReportingService(MessageQueue mq, ReportRepository reportRepository) {
        this.mq = mq;
        this.reportRepository = reportRepository;
        mq.addHandler(ReportEvent.BANK_TRANSFER_COMPLETED_SUCCESSFULLY, this::handleCompletedBankTransfer);
        mq.addHandler(ReportEvent.CUSTOMER_REPORT_REQUESTED, this::handleCustomerReportRequest);
        mq.addHandler(ReportEvent.MERCHANT_REPORT_REQUESTED, this::handleMerchantReport);
        mq.addHandler(ReportEvent.MANAGER_REPORT_REQUESTED, this::handleManagerReport);
        mq.addHandler(ReportEvent.DELETE_REPORT, this::deleteReport);
    }

    public void handleCompletedBankTransfer(Event event) {
        Payment payment = event.getArgument(0, Payment.class);
        reportRepository.createReport(payment);
    }

    public void handleCustomerReportRequest(Event e) {
        String customerId = e.getArgument(0, String.class);
        CorrelationId correlationId = e.getArgument(1, CorrelationId.class);
        CustomerReport customerReport = reportRepository.getCustomerReport(customerId);
        Event event = new Event(ReportEvent.CUSTOMER_REPORT_RETURNED, customerReport, correlationId);
        mq.publish(event);
    }

    public void handleMerchantReport(Event e) {
        String merchantId = e.getArgument(0, String.class);
        CorrelationId correlationId = e.getArgument(1, CorrelationId.class);
        MerchantReport merchantReport = reportRepository.getMerchantReport(merchantId);
        Event event = new Event(ReportEvent.MERCHANT_REPORT_RETURNED, merchantReport, correlationId);
        mq.publish(event);
    }

    public void handleManagerReport(Event e) {
        CorrelationId correlationId = e.getArgument(0, CorrelationId.class);
        ManagerReport managerReport = reportRepository.getManagerReport();
        Event event = new Event(ReportEvent.MANAGER_REPORT_RETURNED, managerReport, correlationId);
        mq.publish(event);
    }

    private void deleteReport(Event event) {
        reportRepository.deleteReport();
    }
}
