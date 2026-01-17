package dtu.pay.factories;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Produces;
import messaging.MessageQueue;
import messaging.implementations.RabbitMqQueue;

@ApplicationScoped
public class QueueProducer {

  @Produces
  @ApplicationScoped
  public MessageQueue messageQueue() {
    String host = System.getenv().getOrDefault("RABBITMQ_HOST", "rabbitmq");
    return new RabbitMqQueue(host);
  }
}
