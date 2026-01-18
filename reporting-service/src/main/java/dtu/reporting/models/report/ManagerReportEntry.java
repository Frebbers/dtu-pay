package dtu.reporting.models.report;

import dtu.reporting.models.Payment;

public record ManagerReportEntry(
        int amount,
        String token,
        String merchantId,
        String customerId
) implements ReportEntry {
    public static ManagerReportEntry fromPayment(Payment p) {
        return new ManagerReportEntry(p.amount(), p.token(), p.merchantId(), p.customerId());
    }
}