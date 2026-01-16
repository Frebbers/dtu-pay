package dtu.pay.models;

public record ReportPayment(
        int amount,
        String token,
        String merchantId
) {
    public static ReportPayment fromPayment(Payment payment) {
        return new ReportPayment(payment.amount(), payment.token(), payment.merchantId());
    }
}
