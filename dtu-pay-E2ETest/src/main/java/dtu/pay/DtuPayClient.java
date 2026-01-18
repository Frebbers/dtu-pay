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
            }
            else {
                throw new RuntimeException(r.readEntity(String.class));
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

    public List<String> requestTokens(String customerId, int amount) {
        TokenRequest request = new TokenRequest(amount);

        lastResponse = base.path("customers")
                .path(customerId)
                .path("tokens")
                .request()
                .post(Entity.json(request));

        if (lastResponse.getStatus() == 200) {
            return lastResponse.readEntity(new GenericType<List<String>>() {});
        }

        return null; // rejection case handled in steps
    }




}
