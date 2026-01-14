package dtu.services.adapter.rest;

import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;

import dtu.services.Customer;
import dtu.services.CustomerRegistrationService;

@Path("/customers")
public class CustomerResource {

    CustomerRegistrationService service = new CustomerRegistrationFactory().getService();

    @POST
    @Consumes("application/json")
    @Produces("application/json")
    public Customer registerCustomer(Customer customer) {
        return service.register(customer);
    }
}
