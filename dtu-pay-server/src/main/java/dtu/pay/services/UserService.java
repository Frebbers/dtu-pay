package dtu.pay.services;

import dtu.pay.models.User;

import dtu.pay.models.exceptions.UserAlreadyExistsException;
import messaging.Event;
import messaging.MessageQueue;

import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

public class UserService {
    private final MessageQueue mq;
    private final Map<CorrelationId, CompletableFuture<String>> correlations = new ConcurrentHashMap<>();

    public UserService(MessageQueue mq) {
        this.mq = mq;
        mq.addHandler("UserRegistered", this::handleUserRegistered);
        mq.addHandler("UserNotRegistered", this::handleUserNotRegistered);
        mq.addHandler("UserRegisteredFailed", this::handleUserNotRegistered);
    }

    public String register(User merchant) throws Exception, UserAlreadyExistsException {
        try {
            CorrelationId correlationId = CorrelationId.randomId();
            CompletableFuture<String> future = new CompletableFuture<>();
            correlations.put(correlationId, future);
            Event event = new Event("UserRegistrationRequested", new Object[]{merchant, correlationId});
            mq.publish(event);
            // TODO: check if joining timeout
            return future.orTimeout(5, TimeUnit.SECONDS).join();
        } catch (Exception e) {
            throw e;
        }
    }

    public void handleUserRegistered(Event e){
        String user = e.getArgument(0, String.class);
        CorrelationId correlationId = e.getArgument(1, CorrelationId.class);
        CompletableFuture<String> future = correlations.remove(correlationId);
        if (future != null) {
            future.complete(user);
        }
    }

    public void handleUserNotRegistered(Event e) {
        String error = e.getArgument(0, String.class);
        CorrelationId correlationId = e.getArgument(1, CorrelationId.class);
        CompletableFuture<String> future = correlations.remove(correlationId);
        if (future == null) {
            return;
        }
        if (error.toLowerCase().contains("already exists")) {
            future.completeExceptionally(new UserAlreadyExistsException());
        } else {
            future.completeExceptionally(new Exception(error));
        }
    }

    public void unregisterUserById(String cpr) {
        //TODO: implement unregister user
        throw new UnsupportedOperationException("Not implemented yet");
    }

    public boolean userExists(String id) {
        throw new UnsupportedOperationException("Not implemented yet");
    }

    public boolean pay(int amount, String cid, String mid) {
        throw new UnsupportedOperationException("Not implemented yet");
    }

}
