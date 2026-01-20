package dtu.reporting.models.report;

import dtu.reporting.models.Payment;

public record CustomerReportEntry(
        int amount,
        String token,
        String merchantId
) implements ReportEntry {
    public static CustomerReportEntry fromPayment(Payment p) {
        return new CustomerReportEntry(p.amount(), p.token(), p.merchantId());
    }
}