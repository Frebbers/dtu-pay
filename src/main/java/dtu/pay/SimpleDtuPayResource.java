package dtu.pay;

import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.util.List;

@Path("")
public class SimpleDtuPayResource {

    SimpleDtuPayService service = new SimpleDtuPayService();

    @POST
    @Path("customers")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response registerCustomer(Customer customer) {
        String id = service.register(customer);
        return Response.ok(id).build();
    }

    @DELETE
    @Path("customers/{id}")
    public Response deleteCustomer(@PathParam("id") String id) {
        service.unregisterCustomerById(id);
        return Response.noContent().build();
    }

    @POST
    @Path("merchants")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response registerMerchant(Merchant merchant) {
        String id = service.register(merchant);
        return Response.ok(id).build();
    }
    
    @DELETE
    @Path("merchants/{id}")
    public Response deleteMerchant(@PathParam("id") String id) {
        service.unregisterMerchantById(id);
        return Response.noContent().build();
    }

    @POST
    @Path("payments")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response pay(PaymentRequest request) {
        boolean success = service.pay(request.amount, request.cid, request.mid);
        if (!success) {
            return Response.status(Response.Status.BAD_REQUEST).entity(service.getLatestError()).build();
        }
        return Response.ok().build();
    }

    @GET
    @Path("payments")
    @Produces(MediaType.APPLICATION_JSON)
    public List<Payment> getPayments() {
        return service.getPayments();
    }

    public static record PaymentRequest(int amount, String cid, String mid) {}
}
