package dtu.pay.factories;

import dtu.pay.services.CustomerService;

public class CustomerServiceFactory {
    public CustomerService getService() {
        return new CustomerService();
    }
}
