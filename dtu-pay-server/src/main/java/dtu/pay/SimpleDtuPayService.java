package dtu.pay;

import java.math.BigDecimal;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

// TODO: delete if no longer needed
public class SimpleDtuPayService {
//    private Map<String, Customer> customers = new ConcurrentHashMap<>();
//    private Map<String, Merchant> merchants = new ConcurrentHashMap<>();
//    private List<Payment> payments = Collections.synchronizedList(new ArrayList<>());
//    private String latestError;
//    private BankService bank = new BankService_Service().getBankServicePort();
//
//    public String register(Customer customer) {
//        String id = UUID.randomUUID().toString();
//        customers.put(id, customer);
//        return id;
//    }
//
//    public String register(Merchant merchant) {
//        String id = UUID.randomUUID().toString();
//        merchants.put(id, merchant);
//        return id;
//    }
//
//    public boolean pay(int amount, String customerId, String merchantId) {
//        Customer customer = customers.get(customerId);
//        if (customer == null) {
//            latestError = "customer with id " + customerId + " is unknown";
//            return false;
//        }
//        Merchant merchant = merchants.get(merchantId);
//        if (merchant == null) {
//            latestError = "merchant with id " + merchantId + " is unknown";
//            return false;
//        }
//        try {
//            bank.transferMoneyFromTo(customer.bankAccountNum(), merchant.bankAccountNum(), BigDecimal.valueOf(amount), "Payment");
//            payments.add(new Payment(amount, customer, merchant));
//            latestError = null;
//            return true;
//        } catch (BankServiceException_Exception e) {
//            latestError = e.getMessage();
//            return false;
//        }
//    }
//
//    public List<Payment> getPayments() {
//        return new ArrayList<>(payments);
//    }
//
//    public String getLatestError() {
//        return latestError;
//    }
//
//    public void unregister(Customer customer) {
//        customers.entrySet().removeIf(entry -> entry.getValue().equals(customer));
//    }
//
//    public void unregister(Merchant merchant) {
//        merchants.entrySet().removeIf(entry -> entry.getValue().equals(merchant));
//    }
//
//    public void unregisterCustomerById(String id) {
//        customers.remove(id);
//    }
//
//    public void unregisterMerchantById(String id) {
//        merchants.remove(id);
//    }
}

