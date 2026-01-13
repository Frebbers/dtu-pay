package dtu.services;

import messaging.Event;
import messaging.implementations.RabbitMqQueue;

public class AccountService {

    private final RabbitMqQueue mq;

    public AccountService(RabbitMqQueue mq) {
        this.mq = mq;
    }

    public void handleCustomerRegistration(Event e) {
        //handle the event and send a response event
    }
    public void handleMerchantRegistration(Event e) {
        //handle the event and send a response event
    }
}
