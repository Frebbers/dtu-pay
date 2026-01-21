package dtu.token.messages;

public record ConsumeTokenRequested(String token, String merchantId, Integer amount, long requestedAt) { //TODO remove merchantid and amount
}
