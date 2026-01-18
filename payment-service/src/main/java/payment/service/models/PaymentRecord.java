package payment.service.models;

public record PaymentRecord(int amount, String token, String customerId, String merchantId)
{
}
