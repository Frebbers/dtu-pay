package dtu.pay.resources;

import dtu.pay.factories.UserServiceFactory;
import dtu.pay.models.User;
import dtu.pay.models.exceptions.UserAlreadyExistsException;
import dtu.pay.services.UserService;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import static dtu.pay.resources.CustomerResource.getResponse;

@Path("")
public class MerchantResource {
    private final UserService service = new UserServiceFactory().getService();

    @POST
    @Path("merchants")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response registerMerchant(User merchant) {
        String id;
        return getResponse(merchant, service);
    }

    @DELETE
    @Path("merchants/{id}")
    public Response deleteMerchant(@PathParam("id") String id) {
        service.unregisterUserById(id);
        return Response.noContent().build();
    }
}
