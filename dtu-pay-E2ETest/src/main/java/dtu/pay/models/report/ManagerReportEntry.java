package dtu.pay.models.report;


import dtu.pay.models.Payment;

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