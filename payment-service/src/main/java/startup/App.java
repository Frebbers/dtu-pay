package startup;

import messaging.implementations.RabbitMqQueue;
import service.PaymentService;

/**
 * Hello world!
 *
 */
public class App
{
    public static void main(String[] args) throws Exception {
        new PaymentService(new RabbitMqQueue("rabbitMq"));
    }
}
