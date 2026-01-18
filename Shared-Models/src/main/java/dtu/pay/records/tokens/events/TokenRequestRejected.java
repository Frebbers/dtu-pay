package dtu.pay.records.tokens.events;

public record TokenRequestRejected(String commandId, String customerId, String reason, long rejectedAt) {
}
