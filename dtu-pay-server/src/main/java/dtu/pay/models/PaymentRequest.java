package dtu.pay.models;

import java.math.BigDecimal;

public record PaymentRequest(String token, String merchantId, BigDecimal amount) {

    public PaymentRequest(String token, String merchantId, BigDecimal amount) {
        this.token = token;
        this.merchantId = merchantId;
        this.amount = amount;
    }

}
