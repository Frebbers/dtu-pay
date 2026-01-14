package dtu.pay.factories;

import dtu.pay.services.UserService;
import messaging.MessageQueue;
import messaging.implementations.RabbitMqQueue;


public class UserServiceFactory {
    private final MessageQueue mq;
    public UserServiceFactory() {
        mq = new RabbitMqQueue("userServiceQueue");
    }//TODO fix this
    public UserService getService() {
        return new UserService(mq);
    }
}
