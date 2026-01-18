package dtu.reporting.models;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class ManagerReport {
    private final List<Payment> payments = new ArrayList<>();
    private int totalMoneyTransferred = 0;

    public void add(Payment payment) {
        payments.add(payment);
        totalMoneyTransferred += payment.amount();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ManagerReport that = (ManagerReport) o;
        if (totalMoneyTransferred != that.totalMoneyTransferred) return false;
        return Objects.equals(payments, that.payments);
    }

    @Override
    public int hashCode() {
        return Objects.hash(payments, totalMoneyTransferred);
    }
}