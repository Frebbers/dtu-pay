package dtu.pay.tokens;

public record TokenRequestRejected(String commandId, String customerId, String reason, long rejectedAt) {
}
