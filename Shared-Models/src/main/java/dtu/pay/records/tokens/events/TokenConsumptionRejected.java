package dtu.pay.records.tokens.events;

public record TokenConsumptionRejected(String commandId, String token, String reason, long rejectedAt) {
}
