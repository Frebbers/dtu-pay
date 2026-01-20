package dtu.pay.resources;

import dtu.pay.factories.ReportingServiceFactory;
import dtu.pay.factories.TokenServiceFactory;
import dtu.pay.factories.UserServiceFactory;
import dtu.pay.models.report.CustomerReport;
import dtu.pay.models.User;
import dtu.pay.models.exceptions.UserAlreadyExistsException;
import dtu.pay.services.ReportingService;
import dtu.pay.services.TokenServiceClient;
import dtu.pay.services.UserService;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.util.List;

@Path("")
public class CustomerResource {

    UserService service = new UserServiceFactory().getService();
    ReportingService reportingService = new ReportingServiceFactory().getService();
    TokenServiceClient tokenService = new TokenServiceFactory().getService();

    @POST
    @Path("customers")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response registerCustomer(User customer) {

        return getResponse(customer, service);
    }

    static Response getResponse(User user, UserService service) {
        String id;
        if (user.cprNumber() == null) {
            return Response.status(Response.Status.BAD_REQUEST).entity("Missing cprNumber")
                    .type(MediaType.TEXT_PLAIN).build();
        }
        if (user.bankAccountNum() == null){
            return Response.status(Response.Status.BAD_REQUEST).entity("Missing bankAccountNum")
                    .type(MediaType.TEXT_PLAIN).build();
        }
        try {
            id = service.register(user);
            return Response.ok(id).build();
        } catch (UserAlreadyExistsException e) {
            return Response.status(Response.Status.BAD_REQUEST).entity("User already exists!")
                    .type(MediaType.TEXT_PLAIN).build();
        } catch (Exception e) {
            return Response.serverError().build();
        }
    }

    @DELETE
    @Path("customers/{id}")
    public Response deleteCustomer(@PathParam("id") String id) {
        service.unregisterUserById(id);
        return Response.noContent().build();
    }

    @GET
    @Path("customers/{id}")
    public Response customerExists(@PathParam("id") String id) {
        service.userExists(id);
        return Response.noContent().build();
    }

    @GET
    @Path("customers/{customerId}/reports")
    @Produces(MediaType.APPLICATION_JSON)
    public CustomerReport getReport(@PathParam("customerId") String customerId) {
        return reportingService.getCustomerReport(customerId);
    }

    @POST
    @Path("customers/{id}/tokens")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response requestTokens(@PathParam("id") String customerId, TokenRequest request) {
        int count = request == null ? 0 : request.count();
        try {
            List<String> tokens = tokenService.requestTokens(customerId, count);
            return Response.ok(tokens).build();
        } catch (RuntimeException e) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(e.getMessage())
                    .type(MediaType.TEXT_PLAIN)
                    .build();
        }
    }
    @DELETE
    @Path("customers/{id}/tokens")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response invalidateTokens(@PathParam("id") String customerId) {
        try {
            tokenService.invalidateTokens(customerId);
            return Response.noContent().build();
        } catch (RuntimeException e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(e.getMessage())
                    .type(MediaType.TEXT_PLAIN)
                    .build();
        }
    }
}
