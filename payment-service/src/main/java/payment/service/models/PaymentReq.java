package payment.service.models;

public record PaymentReq(String token
        , String merchantId
        , int amount) {
}
