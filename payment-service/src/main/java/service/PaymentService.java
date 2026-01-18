package service;

import messaging.Event;
import messaging.MessageQueue;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class PaymentService {
    MessageQueue queue;
    Map<UUID, PaymentReq> paymentHashMap = new HashMap<>();

    public PaymentService(MessageQueue q) {
        this.queue = q;
        queue.addHandler("PaymentRequested", this::policyPaymentRequested);
    }

    /* Policies */

    public void policyPaymentRequested(Event event) {
        PaymentReq paymentReq = event.getArgument(0, PaymentReq.class);
        // public record Payment(String token, String merchantId, int amount)

        // TODO: Validate the payment.token and fetch the customerId
        //      by sending TokenValidateRequest events to TokenService

        // TODO: Use customerId to get customer bank account number by
        //  sending GetBankAccNumReq events to AccountService

        // TODO: Use merchantId to get merchant bank account number by
        //  sending GetBankAccNumReq events to AccountService

        processPayment(paymentReq);
    }


    /* Commands */

    public void processPayment(PaymentReq payment) {
        // TODO: Call the SOAP bank service to process payment:
        //  bank.transferMoneyFromTo(customer.bankAccountNum(), merchant.bankAccountNum(), BigDecimal.valueOf(amount), "Payment");

        UUID paymentId = UUID.randomUUID();
        paymentHashMap.put(paymentId, payment);

        // TODO: Send RecordSuccessfulPayment events to ReportService to record this payment

        queue.publish(new Event("PaymentSuccessful", new Object[] {paymentId}));
    }
    public void handleTokenConsumed(Event TokenConsumed){
        TokenConsumed consumed = lastPublished.getArgument(0, TokenConsumed.class);
        customerId = consumed.customerId();
    }
}
