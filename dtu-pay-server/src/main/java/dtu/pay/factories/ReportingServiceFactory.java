package dtu.pay.factories;

import dtu.pay.services.ReportingService;
import messaging.MessageQueue;
import messaging.implementations.RabbitMqQueue;

public class ReportingServiceFactory {
    private final MessageQueue mq;

    public ReportingServiceFactory() {
        String host = System.getenv().getOrDefault("RABBITMQ_HOST", "rabbitmq");
        mq = new RabbitMqQueue(host);
    }

    public ReportingService getService() {
        return new ReportingService(mq);
    }
}
