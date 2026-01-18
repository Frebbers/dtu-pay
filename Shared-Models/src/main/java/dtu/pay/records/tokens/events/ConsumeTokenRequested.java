package dtu.pay.records.tokens.events;

public record ConsumeTokenRequested(String commandId, String token, String merchantId, Integer amount,
                                    long requestedAt) {
}
