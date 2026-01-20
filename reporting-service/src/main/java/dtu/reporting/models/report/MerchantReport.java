package dtu.reporting.models.report;

import dtu.reporting.models.Payment;

public class MerchantReport extends Report<MerchantReportEntry> {
    public void add(Payment payment) {
        super.addEntry(MerchantReportEntry.fromPayment(payment));
    }
}
