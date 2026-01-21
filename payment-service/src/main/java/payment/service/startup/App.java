package payment.service.startup;

import messaging.implementations.RabbitMqQueue;
import payment.service.PaymentService;

/**
 * Hello world!
 *
 */
public class App
{
    public static void main(String[] args) throws Exception {
        String host = System.getenv().getOrDefault("RABBITMQ_HOST", "rabbitmq");
        new PaymentService(new RabbitMqQueue(host));
    }
}
