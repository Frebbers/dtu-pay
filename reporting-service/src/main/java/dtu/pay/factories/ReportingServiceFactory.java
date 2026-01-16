package dtu.pay.factories;

import dtu.pay.repositories.ReportRepository;
import dtu.pay.repositories.ReportRepositoryImpl;
import dtu.pay.services.ReportingService;
import messaging.MessageQueue;
import messaging.implementations.RabbitMqQueue;

public class ReportingServiceFactory {
    private final MessageQueue mq;

    public ReportingServiceFactory() {
        mq = new RabbitMqQueue("reportServiceQueue");
    }

    public ReportingService getService() {
        ReportRepository reportRepository = new ReportRepositoryImpl();
        return new ReportingService(mq, reportRepository);
    }

    public ReportingService getService(ReportRepository reportRepository ) {
        return new ReportingService(mq, reportRepository);
    }
}
