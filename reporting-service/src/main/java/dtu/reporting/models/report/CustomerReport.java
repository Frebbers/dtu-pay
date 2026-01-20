package dtu.reporting.models.report;

import dtu.reporting.models.Payment;

public class CustomerReport extends Report<CustomerReportEntry> {
    public void add(Payment payment) {
        super.addEntry(CustomerReportEntry.fromPayment(payment));
    }
}
