package dtu.services;

import messaging.Event;
import messaging.MessageQueue;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;
import java.util.UUID;

public class AccountService {
    private static final Logger logger = Logger.getLogger(AccountService.class.getName());
    private static final String CUSTOMER_REGISTRATION_REQUESTED = "CustomerRegistrationRequested";
    private static final String CUSTOMER_REGISTERED = "CustomerRegistered";
    private static final String MERCHANT_REGISTRATION_REQUESTED = "MerchantRegistrationRequested";
    private static final String MERCHANT_REGISTERED = "MerchantRegistered";

    private final MessageQueue mq;
    private final Map<AccountKey, String> idByKey = new ConcurrentHashMap<>();
    private final Map<String, Account> accountsById = new ConcurrentHashMap<>();

    public AccountService(MessageQueue mq) {
        this.mq = mq;

        // Subscribe to registration events
        mq.addHandler(CUSTOMER_REGISTRATION_REQUESTED, this::handleCustomerRegistrationRequested);
        mq.addHandler(MERCHANT_REGISTRATION_REQUESTED, this::handleMerchantRegistrationRequested);
    }

    public void handleCustomerRegistrationRequested(Event event) {
        handleRegistration(event, "customer", CUSTOMER_REGISTERED);
    }

    public void handleMerchantRegistrationRequested(Event event) {
        handleRegistration(event, "merchant", MERCHANT_REGISTERED);
    }

    private void handleRegistration(Event event, String type, String successTopic) {
        RegistrationRequest request = event.getArgument(0, RegistrationRequest.class);
        Object correlationId = event.getArgument(1, Object.class);
        if (request == null) {
            logger.warning("Registration request missing body for type " + type);
            String fallbackId = UUID.randomUUID().toString();
            mq.publish(new Event(successTopic, fallbackId, correlationId));
            return;
        }

        String id = registerAccount(request, type);
        mq.publish(new Event(successTopic, id, correlationId));
    }

    private String registerAccount(RegistrationRequest request, String type) {
        String cprNumber = safe(request.cprNumber());
        String bankAccountNum = safe(request.bankAccountNum());

        if (cprNumber.isEmpty() || bankAccountNum.isEmpty()) {
            logger.warning("Registration missing cpr or bank account for " + type + ": " + request);
            String uuid = UUID.randomUUID().toString();
            accountsById.put(uuid, new Account(uuid, request.firstName(), request.lastName(),
                    request.cprNumber(), request.bankAccountNum(), type));
            return uuid;
        }

        AccountKey key = new AccountKey(cprNumber, bankAccountNum, type);
        return idByKey.computeIfAbsent(key, unusedKey -> {
            String uuid = UUID.randomUUID().toString();
            accountsById.put(uuid, new Account(uuid, request.firstName(), request.lastName(),
                    request.cprNumber(), request.bankAccountNum(), type));
            return uuid;
        });
    }

    private static String safe(String value) {
        return value == null ? "" : value.trim();
    }

    public record RegistrationRequest(String firstName, String lastName, String cprNumber,
                                      String bankAccountNum) {}
    private record AccountKey(String cprNumber, String bankAccountNum, String type) {}
}
