package dtu.pay.factories;

import dtu.pay.services.ReportService;
import messaging.MessageQueue;
import messaging.implementations.RabbitMqQueue;

public class ReportServiceFactory {
    private final MessageQueue mq;
    public ReportServiceFactory() {
        mq = new RabbitMqQueue("userServiceQueue");//TODO fix this
    }
    public ReportService getService() {
        return new ReportService(mq);
    }
}
