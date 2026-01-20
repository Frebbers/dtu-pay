package dtu.pay.models.report;

import java.util.List;

public record CustomerReport(List<CustomerReportEntry> payments) implements Report {}
