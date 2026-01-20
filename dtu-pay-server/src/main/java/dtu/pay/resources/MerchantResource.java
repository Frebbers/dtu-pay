package dtu.pay.resources;

import dtu.pay.factories.PaymentServiceFactory;
import dtu.pay.factories.UserServiceFactory;
import dtu.pay.models.PaymentRequest;
import dtu.pay.models.User;
import dtu.pay.models.exceptions.UserAlreadyExistsException;
import dtu.pay.services.PaymentService;
import dtu.pay.services.UserService;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.util.concurrent.CompletionException;

@Path("")
public class MerchantResource {
    private final UserService service = new UserServiceFactory().getService();
    private final PaymentService paymentService = new PaymentServiceFactory().getService();

    @POST
    @Path("merchants")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response registerMerchant(User merchant) {
        String id;
        try {id = service.register(merchant);}
        catch (UserAlreadyExistsException e) {
            return Response.status(Response.Status.BAD_REQUEST).entity("User already exists!").type(MediaType.TEXT_PLAIN).build();
        }
        catch (Exception e) {
            return Response.serverError().build();
        }
        return Response.ok(id).build();
    }

    @DELETE
    @Path("merchants/{id}")
    public Response deleteMerchant(@PathParam("id") String id) {
        service.unregisterUserById(id);
        return Response.noContent().build();
    }

    @POST
    @Path("payments")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response requestPayment(PaymentRequest paymentReq) {
        String returnedInfo;
        try {
            returnedInfo = paymentService.pay(paymentReq);
        } catch (CompletionException e) {
            Throwable cause = e.getCause();
            String message = cause == null ? e.getMessage() : cause.getMessage();
            if (message == null || message.isBlank()) {
                message = "Payment failed";
            }
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(message)
                    .type(MediaType.TEXT_PLAIN)
                    .build();
        } catch (IllegalArgumentException e) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(e.getMessage())
                    .type(MediaType.TEXT_PLAIN)
                    .build();
        } catch (Exception e) {
            return Response.serverError().build();
        }
        System.out.println("Payment service returns: " + returnedInfo);
        return Response.ok(returnedInfo).build();
    }
}
