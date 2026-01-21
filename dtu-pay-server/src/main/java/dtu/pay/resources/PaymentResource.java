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

@Path("")
public class PaymentResource {
    private final PaymentService paymentService = new PaymentServiceFactory().getService();

    @POST
    @Path("payments")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response pay(PaymentRequest paymentRequest) {
        try {
            String result = paymentService.pay(paymentRequest);
            return Response.ok(result).build();
        } catch (Exception e) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(e.getMessage())
                    .type(MediaType.TEXT_PLAIN)
                    .build();
        }
    }
}
