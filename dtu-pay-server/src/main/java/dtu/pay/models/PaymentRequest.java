package dtu.pay.models;

public record PaymentRequest(String token, String merchantId, int amount) {}
