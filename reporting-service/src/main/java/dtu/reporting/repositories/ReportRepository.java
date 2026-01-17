package dtu.reporting.repositories;

import dtu.reporting.models.CustomerReport;
import dtu.reporting.models.ManagerReport;
import dtu.reporting.models.MerchantReport;
import dtu.reporting.models.Payment;

public interface ReportRepository {
    void createReport(Payment payment);
    CustomerReport getCustomerReport(String customerId);
    MerchantReport getMerchantReport(String merchantId);
    ManagerReport getManagerReport();
}
