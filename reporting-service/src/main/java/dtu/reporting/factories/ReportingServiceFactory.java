package dtu.reporting.factories;

import dtu.reporting.repositories.ReportRepository;
import dtu.reporting.repositories.ReportRepositoryImpl;
import dtu.reporting.services.ReportingService;
import messaging.MessageQueue;
import messaging.implementations.RabbitMqQueue;
/// @author Wenji Xie - s242597

public class ReportingServiceFactory {
    private final MessageQueue mq;

    public ReportingServiceFactory() {
        mq = new RabbitMqQueue("reportServiceQueue");
    }

    public ReportingService getService() {
        ReportRepository reportRepository = new ReportRepositoryImpl();
        return new ReportingService(mq, reportRepository);
    }

    public ReportingService getService(ReportRepository reportRepository) {
        return new ReportingService(mq, reportRepository);
    }
}
