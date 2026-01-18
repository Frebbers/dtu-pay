package service;

public record PaymentReq(String token
        , String merchantId
        , int amount) {
}
