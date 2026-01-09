package dtu.pay;

public record Payment(int amount, Customer customer, Merchant merchant) {}