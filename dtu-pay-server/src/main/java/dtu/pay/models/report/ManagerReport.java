package dtu.pay.models.report;

import java.util.List;

public record ManagerReport(List<ManagerReportEntry> payments, int totalMoneyTransferred) implements Report {}
