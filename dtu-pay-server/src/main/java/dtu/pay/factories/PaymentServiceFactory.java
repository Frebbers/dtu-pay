package dtu.pay.factories;

import dtu.pay.services.PaymentService;
import messaging.MessageQueue;
import messaging.implementations.RabbitMqQueue;


public class PaymentServiceFactory {
    private final MessageQueue mq;
    public PaymentServiceFactory() {
        mq = new RabbitMqQueue("paymentServiceQueue");
    }//TODO fix this
    public PaymentService getService() {
        return new PaymentService(mq);
    }
}