package dtu.pay.models;

import java.util.List;

public record ManagerReport(
        List<Payment> payments,
        int totalMoneyTransferred
) {}