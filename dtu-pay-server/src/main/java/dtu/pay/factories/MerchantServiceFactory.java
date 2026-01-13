package dtu.pay.factories;

import dtu.pay.services.MerchantService;
import messaging.implementations.RabbitMqQueue;

public class MerchantServiceFactory {
    public MerchantServiceFactory() {
        var mq = new RabbitMqQueue("merchantServiceQueue");
    }
    public MerchantService getService() {
        return new MerchantService();
    }
}
