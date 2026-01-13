package dtu.pay.services;

import dtu.pay.Customer;
import dtu.pay.Payment;
import messaging.implementations.RabbitMqQueue;

import java.util.List;

public class CustomerService {
    private final RabbitMqQueue mq;

    public CustomerService(RabbitMqQueue mq) {
        this.mq = mq;
    }

    public String register(Customer customer) {
        return "Customer registered";
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
