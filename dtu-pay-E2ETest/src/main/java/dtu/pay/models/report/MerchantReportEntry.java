package dtu.pay.models.report;


import dtu.pay.models.Payment;

public record MerchantReportEntry(
        int amount,
        String token
) implements ReportEntry {
    public static MerchantReportEntry fromPayment(Payment p) {
        return new MerchantReportEntry(p.amount(), p.token());
    }
}