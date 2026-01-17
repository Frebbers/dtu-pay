package dtu.reporting.models;

public record MerchantReportPayment(
        int amount,
        String token
) {
    public static MerchantReportPayment fromPayment(Payment payment) {
        return new MerchantReportPayment(payment.amount(), payment.token());
    }
}
