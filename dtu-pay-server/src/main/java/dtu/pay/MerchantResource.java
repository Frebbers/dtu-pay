package dtu.pay;

import dtu.pay.factories.MerchantServiceFactory;
import dtu.pay.services.MerchantService;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

public class MerchantResource {
    private final MerchantService service = new MerchantServiceFactory().getService();

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
}
