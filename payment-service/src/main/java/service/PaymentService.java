package service;

import messaging.Event;
import messaging.MessageQueue;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class PaymentService {
    MessageQueue queue;
    Map<UUID, Payment> paymentHashMap = new HashMap<>();

    public PaymentService(MessageQueue q) {
        this.queue = q;
        queue.addHandler("PaymentRequested", this::policyPaymentRequested);
    }

    /* Policies */

    public void policyPaymentRequested(Event event) {
        Payment payment = event.getArgument(0, Payment.class);
        // TODO: Validate the payment.token and fetch the customerId
        //      by sending TokenValidateRequest events to TokenService

        // TODO: Use customerId to get customer bank account number by
        //  sending GetBankAccNumReq events to AccountService

        // TODO: Use merchantId to get merchant bank account number by
        //  sending GetBankAccNumReq events to AccountService

        processPayment(payment);
    }


    /* Commands */

    public void processPayment(Payment payment) {
        // TODO: Call the SOAP bank service to process payment:
        //  bank.transferMoneyFromTo(customer.bankAccountNum(), merchant.bankAccountNum(), BigDecimal.valueOf(amount), "Payment");

        UUID paymentId = UUID.randomUUID();
        paymentHashMap.put(paymentId, payment);

        // TODO: Send RecordSuccessfulPayment events to ReportService to record this payment

        queue.publish(new Event("PaymentSuccessful", new Object[] {paymentId}));
    }
}
