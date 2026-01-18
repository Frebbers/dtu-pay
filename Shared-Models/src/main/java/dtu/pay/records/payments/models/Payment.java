package dtu.pay.records.payments.models;
import dtu.pay.models.User;

public record Payment(int amount, User customer, User merchant) {}

