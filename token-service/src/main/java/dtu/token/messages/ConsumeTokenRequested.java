package dtu.token.messages;

public record ConsumeTokenRequested(String commandId, String token, String merchantId, Integer amount,
                                    long requestedAt) {
}
