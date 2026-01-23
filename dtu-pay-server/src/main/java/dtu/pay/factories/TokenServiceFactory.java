package dtu.pay.factories;

import dtu.pay.services.TokenServiceClient;
import messaging.MessageQueue;
import messaging.implementations.RabbitMqQueue;

/// @author Mattia Zanellato - s253156

public class TokenServiceFactory {
    private final MessageQueue mq;

    public TokenServiceFactory() {
        String host = System.getenv().getOrDefault("RABBITMQ_HOST", "rabbitmq");
        mq = new RabbitMqQueue(host);
    }

    public TokenServiceClient getService() {
        return new TokenServiceClient(mq);
    }
}
