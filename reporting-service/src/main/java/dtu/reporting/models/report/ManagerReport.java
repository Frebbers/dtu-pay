package dtu.reporting.models.report;

import dtu.reporting.models.Payment;

import java.util.Objects;

public class ManagerReport extends Report<ManagerReportEntry> {
    private int totalMoneyTransferred = 0;

    public void add(Payment payment) {
        super.addEntry(ManagerReportEntry.fromPayment(payment));
        totalMoneyTransferred += payment.amount();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        ManagerReport that = (ManagerReport) o;
        return totalMoneyTransferred == that.totalMoneyTransferred;
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), totalMoneyTransferred);
    }
}