package payment.service.models;

import java.math.BigDecimal;

public record PaymentRecord(BigDecimal amount, String token, String customerId, String merchantId) {}
