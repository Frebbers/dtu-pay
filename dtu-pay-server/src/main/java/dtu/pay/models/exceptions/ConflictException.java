package dtu.pay.models.exceptions;

import jakarta.ws.rs.ClientErrorException;
import jakarta.ws.rs.core.Response;

/// http code 409 - ConflictException thrown when attempting to create a resource that already exists
public class ConflictException extends ClientErrorException {
    public ConflictException(String message) {
        super(message, Response.Status.CONFLICT);
    }
}
