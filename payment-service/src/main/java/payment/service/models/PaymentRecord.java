package payment.service.models;

public record PaymentRecord(String customerId, String merchantId, String token, int amount)
{
}
