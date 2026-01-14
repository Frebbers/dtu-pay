package dtu.pay;

import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.client.ClientBuilder;
import jakarta.ws.rs.client.Entity;
import jakarta.ws.rs.client.WebTarget;
import jakarta.ws.rs.core.GenericType;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.util.List;
import java.math.BigDecimal;
import java.util.ArrayList;

public class SimpleDtuPayClient {

    private final Client client;
    private final WebTarget base;
    private String latestError;

    public SimpleDtuPayClient() {
        this.client = ClientBuilder.newClient();
        this.base = client.target("http://localhost:8080");
    }

    public String registerDTUPayCustomer(User customer) {
        Response r = base.path("customers").request()
                .post(Entity.entity(customer, MediaType.APPLICATION_JSON));

            if (r.getStatus() == 200) {
                return r.readEntity(String.class);
            }
            else {
                throw new RuntimeException(r.readEntity(String.class));
            }
    }

    public String registerDTUPayMerchant(User merchant) {
        try (Response r = base.path("merchants").request()
                .post(Entity.entity(merchant, MediaType.APPLICATION_JSON))) {
            if (r.getStatus() == 200) {
                return r.readEntity(String.class);
            }
            latestError = r.readEntity(String.class);
            return null;
        }
    }

    public boolean pay(BigDecimal amount, String cid, String mid) {
        PaymentRequest req = new PaymentRequest(amount, cid, mid);
        try (Response r = base.path("payments").request()
                .post(Entity.entity(req, MediaType.APPLICATION_JSON))) {
            if (r.getStatus() == 200) {
                latestError = null;
                return true;
            }
            latestError = r.readEntity(String.class);
            return false;
        }
    }

    public List<Payment> getPayments() {
        try (Response r = base.path("payments").request().get()) {
            if (r.getStatus() == 200) {
                return r.readEntity(new GenericType<List<Payment>>() {
                });
            }
            return new ArrayList<>();
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
}
