package dtu.pay.resources;

import dtu.pay.factories.PaymentServiceFactory;
import dtu.pay.models.PaymentRequest;
import dtu.pay.models.exceptions.UserAlreadyExistsException;
import dtu.pay.services.PaymentService;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
//TODO this should probably be part of the merchant resource
@Path("")
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
        catch (Exception e) {
            return Response.status(Response.Status.BAD_REQUEST).entity(e.getMessage()).build();
        }
        System.out.println("Payment service returns: " + returnedInfo);
        return Response.ok(returnedInfo).build();
    }

}
