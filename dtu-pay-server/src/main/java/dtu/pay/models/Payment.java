package dtu.pay.models;

public record Payment(
        int amount,
        String token,
        String merchantId,
        String customerId
) {}
