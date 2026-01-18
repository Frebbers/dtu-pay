package dtu.pay.tokens;

public record TokenConsumed(String commandId, String token, String customerId, long consumedAt) {
}
