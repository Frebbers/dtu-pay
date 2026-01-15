package dtu.token.messages;

public record TokenRequestRejected(String commandId, String customerId, String reason, long rejectedAt) {
}
