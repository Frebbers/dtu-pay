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
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

public class DtuPayClient {

    private Response lastResponse;
    private final Client client;
    private final WebTarget base;
    private String latestError;
    private static final int REQUEST_TIMEOUT_SECONDS = 5;

    public Response getLastResponse() {
        return lastResponse;
    }

    public DtuPayClient() {
        this.client = ClientBuilder.newClient()
                .property("jakarta.ws.rs.client.ConnectTimeout", REQUEST_TIMEOUT_SECONDS * 1000)
                .property("jakarta.ws.rs.client.ReadTimeout", REQUEST_TIMEOUT_SECONDS * 1000);
        this.base = client.target("http://localhost:8080");
    }
    
    public String registerDTUPayMerchant(User user) {
        return registerDTUPayAccount(user, "merchants");
    }
    
    public String registerDTUPayCustomer(User user) {
        return registerDTUPayAccount(user, "customers");
    }
    
    String registerDTUPayAccount(User user, String endpoint) {
        try {
            System.out.println("Attempting to register user at: http://localhost:8080/" + endpoint);
            
            // Use CompletableFuture to enforce timeout on the entire request
            CompletableFuture<Response> responseFuture = CompletableFuture.supplyAsync(() -> {
                try {
                    return base.path(endpoint).request()
                            .post(Entity.entity(user, MediaType.APPLICATION_JSON));
                } catch (Exception e) {
                    System.err.println("Error making POST request: " + e.getMessage());
                    throw new RuntimeException("Failed to make HTTP POST request", e);
                }
            });
            
            lastResponse = responseFuture.orTimeout(REQUEST_TIMEOUT_SECONDS, TimeUnit.SECONDS).join();
            
            System.out.println("Response status: " + lastResponse.getStatus());
            if (lastResponse.getStatus() == 200) {
                String result = lastResponse.readEntity(String.class);
                System.out.println("Registration successful. User ID: " + result);
                return result;
            }
            else {
                String errorBody = lastResponse.readEntity(String.class);
                System.err.println("Registration failed with status " + lastResponse.getStatus() + ": " + errorBody);
                throw new RuntimeException(errorBody);
            }
        } catch (java.util.concurrent.CompletionException e) {
            if (e.getCause() instanceof java.util.concurrent.TimeoutException) {
                System.err.println("HTTP Request TIMEOUT after " + REQUEST_TIMEOUT_SECONDS + " seconds to http://localhost:8080/" + endpoint);
                throw new RuntimeException("HTTP request timeout - server is not responding. Check if server is running.", e);
            }
            System.err.println("Exception during HTTP request: " + e.getMessage());
            if (e.getCause() != null) {
                e.getCause().printStackTrace();
            }
            throw new RuntimeException("Failed to register user: " + e.getMessage(), e);
        } catch (Exception e) {
            System.err.println("Exception during registration: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Failed to register user: " + e.getMessage(), e);
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

        return null;
    }




}
