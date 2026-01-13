package dtu.pay.services;

import dtu.pay.Merchant;
import dtu.pay.Payment;
import messaging.Event;
import messaging.implementations.RabbitMqQueue;
import java.util.List;

public class MerchantService {
    private final RabbitMqQueue mq;
    //we need correlations here

    public MerchantService(RabbitMqQueue mq) {
        this.mq = mq;
        mq.addHandler("Merchant Registered", this::handleMerchantRegistered);
    }

    public String register(Merchant customer) {
        return "Merchant registered";
    }

    public void handleMerchantRegistered(Event e){

    }

    public void unregisterMerchantById(String id) {
    }

    public boolean pay(int amount, String cid, String mid) {
        throw new UnsupportedOperationException();
    }

    public Object getLatestError() {
        throw new UnsupportedOperationException();
    }

    public List<Payment> getPayments() {
        throw new UnsupportedOperationException();
    }
}
