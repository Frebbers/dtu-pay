package payment.service.models;

import java.math.BigDecimal;

public record PaymentReq(String token, String merchantId, BigDecimal amount) {}
