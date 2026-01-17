package dtu.reporting.models;

import java.util.ArrayList;
import java.util.List;

public class MerchantReport {
    private final List<MerchantReportPayment> payments = new ArrayList<>();

    public void add(MerchantReportPayment payment) {
        payments.add(payment);
    }
}
