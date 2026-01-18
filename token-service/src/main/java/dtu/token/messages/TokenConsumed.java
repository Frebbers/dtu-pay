package dtu.token.messages;

public record TokenConsumed(String commandId, String token, String customerId, long consumedAt) {
}
