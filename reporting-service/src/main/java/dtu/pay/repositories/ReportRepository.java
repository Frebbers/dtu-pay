package dtu.pay.repositories;

import dtu.pay.models.CustomerReport;
import dtu.pay.models.ManagerReport;
import dtu.pay.models.MerchantReport;
import dtu.pay.models.Payment;

public interface ReportRepository {
    void createReport(Payment payment);
    CustomerReport getCustomerReport(String customerId);
    MerchantReport getMerchantReport(String merchantId);
    ManagerReport getManagerReport();
}
