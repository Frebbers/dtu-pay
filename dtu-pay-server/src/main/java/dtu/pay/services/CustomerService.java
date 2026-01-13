package dtu.pay.services;

import dtu.pay.Customer;
import dtu.pay.Payment;
import messaging.Event;
import messaging.implementations.RabbitMqQueue;

import java.util.List;

public class CustomerService {
    private final RabbitMqQueue mq;
    //we need correlations here

    public CustomerService(RabbitMqQueue mq) {
        this.mq = mq;
        mq.addHandler("CustomerRegistered", this::handleCustomerRegistered);
    }

    public String register(Customer customer) {
        return "Customer registered";
    }

    public void handleCustomerRegistered(Event e){

    }

    public void unregisterCustomerById(String id) {
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
