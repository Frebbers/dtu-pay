package dtu.pay.factories;

import dtu.pay.services.CustomerService;
import messaging.implementations.RabbitMqQueue;

public class CustomerServiceFactory {
    public CustomerServiceFactory() {
        var mq = new RabbitMqQueue("customerServiceQueue");
    }
    public CustomerService getService() {
        return new CustomerService();
    }
}
