package payment.service.models;

public record ConsumeTokenRequested(String commandId, String token, String merchantId, Integer amount,
                                    long requestedAt) {
}
