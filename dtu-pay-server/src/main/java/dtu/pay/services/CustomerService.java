package dtu.pay.services;

import dtu.pay.Customer;
import dtu.pay.Payment;

import java.util.List;

public class CustomerService {
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
