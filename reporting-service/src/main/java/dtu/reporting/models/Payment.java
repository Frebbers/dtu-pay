package dtu.reporting.models;

public record Payment(
        int amount,
        String token,
        String customerId,
        String merchantId
) {}
