package dtu.pay.tokens;

public record TokenConsumptionRejected(String commandId, String token, String reason, long rejectedAt) {
}
