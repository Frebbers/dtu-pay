package dtu.pay.tokens;

public record TokenRequestSubmitted(String commandId, String customerId, int requestedCount, long requestedAt) {
}
