package dtu.pay.models;

public record MerchantPayment(
        int amount,
        String token
) {}
