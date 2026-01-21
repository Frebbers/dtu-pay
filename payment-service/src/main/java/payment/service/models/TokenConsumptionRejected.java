package payment.service.models;

public record TokenConsumptionRejected(String commandId, String token, String reason, long rejectedAt) {
}
