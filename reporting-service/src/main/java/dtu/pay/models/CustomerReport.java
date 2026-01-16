package dtu.pay.models;

import java.util.ArrayList;
import java.util.List;

public class CustomerReport {
    private final List<Payment> payments = new ArrayList<>();

    public void add(Payment payment) {
        payments.add(payment);
    }
}
