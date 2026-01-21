package dtu.token.messages;

public record TokenConsumptionRejected(String token, String reason, long rejectedAt) {
}
