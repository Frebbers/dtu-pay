package payment.service.startup;

import messaging.implementations.RabbitMqQueue;
import payment.service.PaymentService;

import java.util.concurrent.CountDownLatch;
import java.util.logging.Logger;

/**
 * Hello world!
 *
 */

public class App {
    private static final Logger logger = Logger.getLogger(App.class.getName());

    public static void main(String[] args) throws InterruptedException {
        String rabbitHost = System.getenv().getOrDefault("RABBITMQ_HOST", "rabbitmq");
        int maxAttempts = readIntEnv("RABBITMQ_CONNECT_RETRIES", 30);
        long delayMs = readLongEnv("RABBITMQ_CONNECT_DELAY_MS", 2000L);
        RabbitMqQueue mq = connectWithRetry(rabbitHost, maxAttempts, delayMs);
        new PaymentService(mq);
        logger.info("Reporting service started. Waiting for events on RabbitMQ host: " + rabbitHost);
        new CountDownLatch(1).await();
    }

    private static RabbitMqQueue connectWithRetry(String host, int maxAttempts, long delayMs)
            throws InterruptedException {
        int attempt = 0;
        while (maxAttempts <= 0 || attempt < maxAttempts) {
            attempt++;
            try {
                return new RabbitMqQueue(host);
            } catch (RuntimeException | Error e) {
                logger.warning("RabbitMQ connection failed (attempt " + attempt + "): " + e);
                Thread.sleep(delayMs);
            }
        }
        throw new IllegalStateException("RabbitMQ connection failed after " + attempt + " attempts");
    }

    private static int readIntEnv(String key, int defaultValue) {
        String raw = System.getenv(key);
        if (raw == null || raw.isBlank()) {
            return defaultValue;
        }
        try {
            return Integer.parseInt(raw.trim());
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }

    private static long readLongEnv(String key, long defaultValue) {
        String raw = System.getenv(key);
        if (raw == null || raw.isBlank()) {
            return defaultValue;
        }
        try {
            return Long.parseLong(raw.trim());
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }
}
