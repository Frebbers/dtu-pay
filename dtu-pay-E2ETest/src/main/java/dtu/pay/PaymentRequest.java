package dtu.pay;

import java.math.BigDecimal;

public record PaymentRequest(String token,String mid,BigDecimal amount) {}

