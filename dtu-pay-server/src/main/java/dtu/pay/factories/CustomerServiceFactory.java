package dtu.pay.factories;

import dtu.pay.services.CustomerService;
import messaging.implementations.RabbitMqQueue;

public class CustomerServiceFactory {
    private final RabbitMqQueue mq;
    public CustomerServiceFactory() {
        mq = new RabbitMqQueue("customerServiceQueue");
    }
    public CustomerService getService() {
        return new CustomerService(mq);
    }
}
