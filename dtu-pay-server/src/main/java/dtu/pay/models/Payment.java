package dtu.pay.models;

public record Payment(int amount, User customer, User merchant) {}

