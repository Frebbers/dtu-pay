package dtu.pay;

import java.math.BigDecimal;

public record PaymentRequest(String token,String merchantId,BigDecimal amount) {}

