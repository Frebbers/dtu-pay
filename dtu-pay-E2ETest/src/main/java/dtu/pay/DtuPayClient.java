package dtu.pay;

import dtu.pay.models.report.CustomerReport;
import dtu.pay.models.report.ManagerReport;
import dtu.pay.models.report.MerchantReport;
import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.client.ClientBuilder;
import jakarta.ws.rs.client.Entity;
import jakarta.ws.rs.client.WebTarget;
import jakarta.ws.rs.core.GenericType;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.util.List;
import java.math.BigDecimal;

public class DtuPayClient {

    private Response lastResponse;
    private final Client client;
    private final WebTarget base;
    private String latestError;

    public Response getLastResponse() {
        return lastResponse;
    }

    public DtuPayClient() {
        this.client = ClientBuilder.newClient();
        this.base = client.target("http://localhost:8080");
    }

    public String registerDTUPayMerchant(User user) {
        return registerDTUPayAccount(user, "merchants");
    }

    public String registerDTUPayCustomer(User user) {
        return registerDTUPayAccount(user, "customers");
    }

    private String registerDTUPayAccount(User user, String endpoint) {
        Response r = base.path(endpoint).request()
                .post(Entity.entity(user, MediaType.APPLICATION_JSON));

        if (r.getStatus() == 200) {
            return r.readEntity(String.class);
        } else {
            throw new RuntimeException(r.readEntity(String.class));
        }
    }

    public void pay(String token, String merchantId, BigDecimal amount) {
        PaymentRequest req = new PaymentRequest(token, merchantId, amount);
        try (Response r = base.path("payments").request()
                .post(Entity.entity(req, MediaType.APPLICATION_JSON))) {
            if (r.getStatus() == 200) {
                latestError = null;
                return;
            }
            throw new RuntimeException(r.readEntity(String.class));
        }
    }

    public CustomerReport getCustomerReport(String customerId) {
        try (Response r = base
                .path("customers")
                .path(customerId)
                .path("reports")
                .request()
                .get()
        ) {
            if (r.getStatus() == 200) {
                return r.readEntity(CustomerReport.class);
            }
            return new CustomerReport(List.of());
        }
    }

    public MerchantReport getMerchantReport(String merchantId) {
        try (Response r = base
                .path("merchants")
                .path(merchantId)
                .path("reports")
                .request()
                .get()
        ) {
            if (r.getStatus() == 200) {
                return r.readEntity(MerchantReport.class);
            }
            return new MerchantReport(List.of());
        }
    }

    public ManagerReport getManagerReport() {
        try (Response r = base.path("manager/reports").request().get()) {
            if (r.getStatus() == 200) {
                return r.readEntity(ManagerReport.class);
            }
            return new ManagerReport(List.of(), 0);
        }
    }

    public void unregisterCustomer(String id) {
        if (id != null) {
            base.path("customers").path(id).request().delete().close();
        }
    }

    public void unregisterMerchant(String id) {
        if (id != null) {
            base.path("merchants").path(id).request().delete().close();
        }
    }

    public String getLatestError() {
        return latestError;
    }

    public boolean customerExists(String customerId) {
        try (Response r = base.path("customers").path(customerId).request().get()) {
            return r.getStatus() == 200;
        }
    }

    public List<String> requestTokens(String customerId, int amount) {
        TokenRequest request = new TokenRequest(amount);

        lastResponse = base.path("customers")
                .path(customerId)
                .path("tokens")
                .request()
                .post(Entity.json(request));

        if (lastResponse.getStatus() == 200) {
            latestError = null;
            return lastResponse.readEntity(new GenericType<List<String>>() {
            });
        }
        latestError = lastResponse.readEntity(String.class);
        return null; // rejection case handled in steps
    }

}
