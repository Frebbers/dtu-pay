package dtu.pay.repositories;

import dtu.pay.models.*;

import java.util.HashMap;
import java.util.Map;

public class ReportRepositoryImpl implements ReportRepository {
    private final Map<String, CustomerReport> customerReportMap = new HashMap<>();
    private final Map<String, MerchantReport> merchantReportMap = new HashMap<>();
    private final ManagerReport managerReport = new ManagerReport();

    public ReportRepositoryImpl() {}

    @Override
    public void createReport(Payment payment) {
        MerchantReportPayment merchantReportPayment = MerchantReportPayment.fromPayment(payment);

        CustomerReport customerReport = customerReportMap.getOrDefault(payment.customerId(), new CustomerReport());
        customerReport.add(payment);

        MerchantReport merchantReport = merchantReportMap.getOrDefault(payment.merchantId(), new MerchantReport());
        merchantReport.add(merchantReportPayment);

        customerReportMap.put(payment.customerId(), customerReport);
        merchantReportMap.put(payment.merchantId(), merchantReport);
        managerReport.add(payment);
    }

    @Override
    public CustomerReport getCustomerReport(String customerId) {
        return customerReportMap.get(customerId);
    }

    @Override
    public MerchantReport getMerchantReport(String merchantId) {
        return merchantReportMap.get(merchantId);
    }

    @Override
    public ManagerReport getManagerReport() {
        return managerReport;
    }
}
