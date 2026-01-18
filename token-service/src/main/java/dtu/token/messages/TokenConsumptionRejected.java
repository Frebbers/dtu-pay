package dtu.token.messages;

public record TokenConsumptionRejected(String commandId, String token, String reason, long rejectedAt) {
}
