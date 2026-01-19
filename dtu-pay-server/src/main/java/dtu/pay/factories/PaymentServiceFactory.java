package dtu.pay.factories;

import dtu.pay.services.PaymentService;
import messaging.MessageQueue;
import messaging.implementations.RabbitMqQueue;


public class PaymentServiceFactory {
    private final MessageQueue mq;
    public PaymentServiceFactory() {
        String host = System.getenv().getOrDefault("RABBITMQ_HOST", "rabbitmq");
        mq = new RabbitMqQueue(host);
    }
    public PaymentService getService() {
        return new PaymentService(mq);
    }
}
