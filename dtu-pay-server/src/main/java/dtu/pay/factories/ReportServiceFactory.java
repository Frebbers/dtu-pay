package dtu.pay.factories;

import dtu.pay.services.ReportService;
import messaging.MessageQueue;
import messaging.implementations.RabbitMqQueue;

public class ReportServiceFactory {
    private final MessageQueue mq;
    public ReportServiceFactory() {
        mq = new RabbitMqQueue("rabbitmq");
    }
    public ReportService getService() {
        return new ReportService(mq);
    }
}
