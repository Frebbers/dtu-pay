package dtu.pay.records.tokens.events;

public record TokenConsumed(String commandId, String token, String customerId, long consumedAt) {
}
