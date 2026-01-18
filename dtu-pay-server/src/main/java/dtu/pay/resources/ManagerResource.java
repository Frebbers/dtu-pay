package dtu.pay.resources;

import dtu.pay.factories.ReportingServiceFactory;
import dtu.pay.models.report.ManagerReport;
import dtu.pay.services.ReportingService;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

public class ManagerResource {
    private final ReportingService reportingService = new ReportingServiceFactory().getService();

    @GET
    @Path("reports")
    @Produces(MediaType.APPLICATION_JSON)
    public ManagerReport getReport() {
        return reportingService.getManagerReport();
    }
}
