package dtu.pay.records.payments.events;

public record PaymentRequest(String token, String merchantId, int amount) {}
