package dtu.pay.factories;

import dtu.pay.services.ReportService;
import messaging.MessageQueue;
import messaging.implementations.RabbitMqQueue;

public class ReportServiceFactory {
    private final MessageQueue mq;
    public ReportServiceFactory() {
        String host = System.getenv().getOrDefault("RABBITMQ_HOST", "rabbitmq");
        mq = new RabbitMqQueue(host);
    }
    public ReportService getService() {
        return new ReportService(mq);
    }
}
