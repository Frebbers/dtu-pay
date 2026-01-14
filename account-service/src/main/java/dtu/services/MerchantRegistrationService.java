package dtu.services;

import java.util.concurrent.CompletableFuture;

import messaging.Event;
import messaging.MessageQueue;
import dtu.services.Merchant;

public class MerchantRegistrationService {

    private MessageQueue queue;
    private CompletableFuture<Merchant> registeredMerchant;

    public MerchantRegistrationService(MessageQueue q) {
        queue = q;
        queue.addHandler("MerchantIdAssigned", this::handleMerchantIdAssigned);
    }

    public Merchant register(Merchant s) {
        registeredMerchant = new CompletableFuture<>();
        Event event = new Event("MerchantRegistrationRequested", new Object[] { s });
        queue.publish(event);
        return registeredMerchant.join();
    }

    public void handleMerchantIdAssigned(Event e) {
        var s = e.getArgument(0, Merchant.class);
        registeredMerchant.complete(s);
    }
}