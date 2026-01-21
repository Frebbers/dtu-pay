package dtu.pay.models.report;

import java.util.List;

public record MerchantReport(List<MerchantReportEntry> payments) implements Report {}
