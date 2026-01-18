package dtu.reporting.models;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class CustomerReport {
    private final List<Payment> payments = new ArrayList<>();

    public void add(Payment payment) {
        payments.add(payment);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CustomerReport that = (CustomerReport) o;
        return Objects.equals(payments, that.payments);
    }

    @Override
    public int hashCode() {
        return Objects.hash(payments);
    }
}
