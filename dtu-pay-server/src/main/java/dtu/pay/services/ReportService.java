package dtu.pay.services;

import dtu.pay.models.CustomerReport;
import messaging.MessageQueue;

public class ReportService {
    private MessageQueue mq;

    public ReportService(MessageQueue mq) {
        this.mq = mq;
    }

    public CustomerReport getCustomerReport(int customerId) {
        throw new UnsupportedOperationException();
    }
}
