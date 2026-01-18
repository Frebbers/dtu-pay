package dtu.pay.records;

import dtu.pay.models.User;

public record Payment(int amount, User customer, User merchant) {}

