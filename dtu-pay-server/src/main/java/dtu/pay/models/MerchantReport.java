package dtu.pay.models;

import java.util.List;

public record MerchantReport(
        List<MerchantPayment> payments
) {}
