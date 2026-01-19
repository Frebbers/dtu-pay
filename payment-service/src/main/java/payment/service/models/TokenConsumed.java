package payment.service.models;

public record TokenConsumed(String commandId, String token, String customerId, long consumedAt) {
}
