package dtu.pay;

import java.math.BigDecimal;

public record PaymentRequest(BigDecimal amount, String token, String mid) {}

