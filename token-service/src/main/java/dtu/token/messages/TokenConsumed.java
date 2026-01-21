package dtu.token.messages;

public record TokenConsumed(String token, String customerId, long consumedAt) {
}
