package dtu.reporting.repositories;

import dtu.reporting.models.Payment;
import dtu.reporting.models.report.CustomerReport;
import dtu.reporting.models.report.ManagerReport;
import dtu.reporting.models.report.MerchantReport;

public interface ReportRepository {
    void createReport(Payment payment);
    CustomerReport getCustomerReport(String customerId);
    MerchantReport getMerchantReport(String merchantId);
    ManagerReport getManagerReport();
}
