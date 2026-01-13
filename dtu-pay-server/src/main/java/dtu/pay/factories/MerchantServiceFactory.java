package dtu.pay.factories;

import dtu.pay.services.MerchantService;

public class MerchantServiceFactory {
    public MerchantService getService() {
        return new MerchantService();
    }
}
