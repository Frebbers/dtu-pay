package dtu.pay.contracts;

import dtu.pay.User;

public record Payment(int amount, User customer, User merchant) {}

