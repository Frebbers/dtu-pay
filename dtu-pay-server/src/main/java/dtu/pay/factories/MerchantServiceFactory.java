package dtu.pay.factories;

import dtu.pay.services.MerchantService;
import messaging.implementations.RabbitMqQueue;
/// @author s224804
public class MerchantServiceFactory {
    private final RabbitMqQueue mq;
    public MerchantServiceFactory() {
         mq = new RabbitMqQueue("merchantServiceQueue");
    }
    public MerchantService getService() {
        return new MerchantService(mq);
    }
}
