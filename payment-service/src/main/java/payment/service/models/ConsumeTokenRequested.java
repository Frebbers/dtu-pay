package payment.service.models;

public record ConsumeTokenRequested(String commandId, String token,
                                    String merchantId, //TODO remove merchantId
                                    Integer amount, // TODO remove amount
                                    long requestedAt) {
}
