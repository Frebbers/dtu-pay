package dtu.services;

import messaging.Event;
import messaging.implementations.RabbitMqQueue;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;
import java.util.UUID;

public class AccountService {
    private static final Logger logger = Logger.getLogger(AccountService.class.getName());
    private final RabbitMqQueue mq;
    private final Map<String, Customer> customers = new ConcurrentHashMap<>(); // key: uuid, value: Customer
    private final Map<String, Merchant> merchants = new ConcurrentHashMap<>(); // key: uuid, value: Merchant

    public AccountService(RabbitMqQueue mq) {
        this.mq = mq;

        // Subscribe to registration events
        mq.addHandler("customer.register", this::handleCustomerRegistration);
        mq.addHandler("merchant.register", this::handleMerchantRegistration);
    }

    public void handleCustomerRegistration(Event e) {
        logger.info("Received customer registration event: " + e);
        String name = e.getArgument(0, String.class);
        String accountNumber = e.getArgument(1, String.class);
        if (name == null || accountNumber == null) {
            logger.warning("Invalid customer registration event: missing data");
            mq.publish(new Event("customer.register.error", "Missing name or accountNumber"));
            return;
        }
        // Check for duplicate (by name+accountNumber)
        boolean exists = customers.values().stream().anyMatch(c -> c.name().equals(name) && c.accountNumber().equals(accountNumber));
        if (exists) {
            logger.warning("Duplicate customer registration: " + name + ", " + accountNumber);
            mq.publish(new Event("customer.register.error", "Customer already registered: " + name + ", " + accountNumber));
            return;
        }
        String uuid = UUID.randomUUID().toString();
        Customer customer = new Customer(uuid, name, accountNumber);
        customers.put(uuid, customer);
        logger.info("Customer registered: " + uuid);
        mq.publish(new Event("customer.registered", uuid, name, accountNumber));
    }

    public void handleMerchantRegistration(Event e) {
        logger.info("Received merchant registration event: " + e);
        String name = e.getArgument(0, String.class);
        String accountNumber = e.getArgument(1, String.class);
        if (name == null || accountNumber == null) {
            logger.warning("Invalid merchant registration event: missing data");
            mq.publish(new Event("merchant.register.error", "Missing name or accountNumber"));
            return;
        }
        // Check for duplicate (by name+accountNumber)
        boolean exists = merchants.values().stream().anyMatch(m -> m.name().equals(name) && m.accountNumber().equals(accountNumber));
        if (exists) {
            logger.warning("Duplicate merchant registration: " + name + ", " + accountNumber);
            mq.publish(new Event("merchant.register.error", "Merchant already registered: " + name + ", " + accountNumber));
            return;
        }
        String uuid = UUID.randomUUID().toString();
        Merchant merchant = new Merchant(uuid, name, accountNumber);
        merchants.put(uuid, merchant);
        logger.info("Merchant registered: " + uuid);
        mq.publish(new Event("merchant.registered", uuid, name, accountNumber));
    }

    // Simple record for customer/merchant details
    public record Customer(String uuid, String name, String accountNumber) {}
    public record Merchant(String uuid, String name, String accountNumber) {}
}
