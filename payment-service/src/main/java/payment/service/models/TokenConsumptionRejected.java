package payment.service.models;

public record TokenConsumptionRejected(String token, String reason, long rejectedAt) {
}

