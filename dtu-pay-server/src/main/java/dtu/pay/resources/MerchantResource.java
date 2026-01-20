package dtu.pay.resources;

import dtu.pay.factories.ReportingServiceFactory;
import dtu.pay.factories.UserServiceFactory;
import dtu.pay.models.report.MerchantReport;
import dtu.pay.models.User;
import dtu.pay.models.exceptions.UserAlreadyExistsException;
import dtu.pay.services.ReportingService;
import dtu.pay.services.UserService;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

@Path("")
public class MerchantResource {
    private final UserService userService = new UserServiceFactory().getService();
    private final ReportingService reportingService = new ReportingServiceFactory().getService();

    @POST
    @Path("merchants")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response registerMerchant(User merchant) {
        String id = userService.register(merchant);
        return Response.ok(id).build();
    }

    @DELETE
    @Path("merchants/{id}")
    public Response deleteMerchant(@PathParam("id") String id) {
        try {
            userService.unregisterUserById(id);
            return Response.noContent().build(); // 204
        } catch (jakarta.ws.rs.NotFoundException e) {
            return Response.status(Response.Status.NOT_FOUND) // 404
                    .entity(e.getMessage())
                    .type(MediaType.TEXT_PLAIN)
                    .build();
        }
    }

    @GET
    @Path("merchants/{merchantId}/reports")
    @Produces(MediaType.APPLICATION_JSON)
    public MerchantReport getReport(@PathParam("merchantId") String merchantId) {
        return reportingService.getMerchantReport(merchantId);
    }
}
