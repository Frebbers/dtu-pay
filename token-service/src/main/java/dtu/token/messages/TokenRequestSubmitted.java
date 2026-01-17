package dtu.token.messages;

public record TokenRequestSubmitted(String commandId, String customerId, int requestedCount, long requestedAt) {
}
