package dtu.reporting.models.report;

import dtu.reporting.models.Payment;

public record MerchantReportEntry(
        int amount,
        String token
) implements ReportEntry {
    public static MerchantReportEntry fromPayment(Payment p) {
        return new MerchantReportEntry(p.amount(), p.token());
    }
}