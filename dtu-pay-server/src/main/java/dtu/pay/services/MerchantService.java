package dtu.pay.services;

import dtu.pay.Merchant;
import messaging.implementations.RabbitMqQueue;

public class MerchantService {
    RabbitMqQueue mq;
    public MerchantService (RabbitMqQueue mq) {
        this.mq = mq;
    }
    public String register(Merchant merchant) {
        throw new UnsupportedOperationException();
    }

    public void unregisterMerchantById(String id) {
        throw new UnsupportedOperationException();
    }
}
