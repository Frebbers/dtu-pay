package dtu.pay.resources;

import dtu.pay.factories.ReportServiceFactory;
import dtu.pay.models.ManagerReport;
import dtu.pay.services.ReportService;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

public class ManagerResource {
    private final ReportService reportService = new ReportServiceFactory().getService();

    @GET
    @Path("reports")
    @Produces(MediaType.APPLICATION_JSON)
    public ManagerReport getReport() {
        return reportService.getManagerReport();
    }
}
