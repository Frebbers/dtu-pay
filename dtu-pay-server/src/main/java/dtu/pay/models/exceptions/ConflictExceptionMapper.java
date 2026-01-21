package dtu.pay.models.exceptions;

import io.quarkus.logging.Log;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;

@Provider
public class ConflictExceptionMapper implements ExceptionMapper<ConflictException> {
    @Override
    public Response toResponse(ConflictException e) {
        Log.info(e.getMessage());
        return Response.status(e.getResponse().getStatus())
                .entity(e.getMessage())
                .build();
    }
}
