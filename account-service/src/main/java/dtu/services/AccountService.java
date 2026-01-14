package dtu.services;

import messaging.Event;
import messaging.implementations.RabbitMqQueue;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;

public class AccountService {
    private static final Logger logger = Logger.getLogger(AccountService.class.getName());
    private final RabbitMqQueue mq;
    private final Map<String, String> customers = new ConcurrentHashMap<>(); // key: customerId, value: customerData (could be JSON or similar)
    private final Map<String, String> merchants = new ConcurrentHashMap<>(); // key: merchantId, value: merchantData

    public AccountService(RabbitMqQueue mq) {
        this.mq = mq;

        // Subscribe to registration events
        mq.addHandler("customer.register", this::handleCustomerRegistration);
        mq.addHandler("merchant.register", this::handleMerchantRegistration);
    }

    public void handleCustomerRegistration(Event e) {
        logger.info("Received customer registration event: " + e);
        String customerId = e.getArgument(0, String.class);
        String customerData = e.getArgument(1, String.class); // adjust as needed
        if (customerId == null || customerData == null) {
            logger.warning("Invalid customer registration event: missing data");
            mq.publish(new Event("customer.register.error", "Missing customerId or customerData"));
            return;
        }
        if (customers.containsKey(customerId)) {
            logger.warning("Duplicate customer registration: " + customerId);
            mq.publish(new Event("customer.register.error", "Customer already registered: " + customerId));
            return;
        }
        customers.put(customerId, customerData);
        logger.info("Customer registered: " + customerId);
        mq.publish(new Event("customer.registered", customerId));
    }

    public void handleMerchantRegistration(Event e) {
        logger.info("Received merchant registration event: " + e);
        String merchantId = e.getArgument(0, String.class);
        String merchantData = e.getArgument(1, String.class); // adjust as needed
        if (merchantId == null || merchantData == null) {
            logger.warning("Invalid merchant registration event: missing data");
            mq.publish(new Event("merchant.register.error", "Missing merchantId or merchantData"));
            return;
        }
        if (merchants.containsKey(merchantId)) {
            logger.warning("Duplicate merchant registration: " + merchantId);
            mq.publish(new Event("merchant.register.error", "Merchant already registered: " + merchantId));
            return;
        }
        merchants.put(merchantId, merchantData);
        logger.info("Merchant registered: " + merchantId);
        mq.publish(new Event("merchant.registered", merchantId));
    }
}
