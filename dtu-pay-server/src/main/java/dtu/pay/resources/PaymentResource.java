package dtu.pay.resources;

import dtu.pay.factories.PaymentServiceFactory;
import dtu.pay.models.PaymentRequest;
import dtu.pay.services.PaymentService;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

public class PaymentResource {
    private final PaymentService service = new PaymentServiceFactory().getService();

    @POST
    @Path("payments")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response requestPayment(PaymentRequest paymentReq) {
        String returnedInfo;
        try {
            returnedInfo = service.pay(paymentReq);
        }
//        catch (UserAlreadyExistsException e) {
//            return Response.status(Response.Status.BAD_REQUEST).entity("User already exists!").type(MediaType.TEXT_PLAIN).build();
//        }
        // TODO: Handle different exceptions
        catch (Exception e) {
            return Response.serverError().build();
        }
        return Response.ok(returnedInfo).build();
    }

}
