package service;

public record Payment(String token
        , String merchantId
        , int amount) {
}
