package dtu.pay.models;

import java.util.List;

public record CustomerReport(
        List<Payment> payments
) {}
