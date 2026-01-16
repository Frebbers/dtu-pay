package dtu.pay;

import java.math.BigDecimal;

public record PaymentRequest(BigDecimal amount, String cid, String mid) {}

