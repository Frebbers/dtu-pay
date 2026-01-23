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

@Path("customers")
public class CustomerResource {

    //    SimpleDtuPayService service = new SimpleDtuPayService();
    UserService service = new UserServiceFactory().getService();
    ReportingService reportingService = new ReportingServiceFactory().getService();
    TokenServiceClient tokenService = new TokenServiceFactory().getService();

    @POST
    @Path("")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response registerCustomer(User customer) {
        String id = service.register(customer);
        return Response.ok(id).build();
    }

    @DELETE
    @Path("{id}")
    public Response deleteCustomer(@PathParam("id") String id) {
        try {
            service.unregisterUserById(id);
            return Response.noContent().build(); // 204
        } catch (jakarta.ws.rs.NotFoundException e) {
            return Response.status(Response.Status.NOT_FOUND) // 404
                    .entity(e.getMessage())
                    .type(MediaType.TEXT_PLAIN)
                    .build();
        }
    }

    @GET
    @Path("{id}")
    public Response customerExists(@PathParam("id") String id) {
        Boolean exists = service.userExists(id);
        return Response.ok(exists).build();
    }

    @GET
    @Path("{customerId}/reports")
    @Produces(MediaType.APPLICATION_JSON)
    public CustomerReport getReport(@PathParam("customerId") String customerId) {
        return reportingService.getCustomerReport(customerId);
    }

    @POST
    @Path("{id}/tokens")
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
    @Path("{id}/tokens")
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
