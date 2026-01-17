package dtu.reporting.models;

import java.util.ArrayList;
import java.util.List;

public class ManagerReport {
    private final List<Payment> payments = new ArrayList<>();
    private int totalMoneyTransferred = 0;

    public void add(Payment payment) {
        payments.add(payment);
        totalMoneyTransferred += payment.amount();
    }
}