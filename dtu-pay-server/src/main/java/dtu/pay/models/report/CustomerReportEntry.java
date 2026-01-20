package dtu.pay.models.report;

import dtu.pay.models.Payment;

public record CustomerReportEntry(
        int amount,
        String token,
        String merchantId
) implements ReportEntry {
    public static CustomerReportEntry fromPayment(Payment p) {
        return new CustomerReportEntry(p.amount(), p.token(), p.merchantId());
    }
}