package dtu.pay.records.tokens.events;

public record TokenRequestSubmitted(String commandId, String customerId, int requestedCount, long requestedAt) {
}
