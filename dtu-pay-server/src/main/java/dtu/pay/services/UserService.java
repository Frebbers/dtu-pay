package dtu.pay.services;

import dtu.pay.Payment;
import dtu.pay.models.User;

import dtu.pay.models.exceptions.UserAlreadyExistsException;
import messaging.Event;
import messaging.MessageQueue;


import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

public class UserService {
    private final MessageQueue mq;
    private final Map<CorrelationId, CompletableFuture<String>> correlations = new ConcurrentHashMap<>();

    public UserService(MessageQueue mq) {
        this.mq = mq;
        mq.addHandler("UserRegistered", this::handleUserRegistered);
        mq.addHandler("UserNotRegistered", this::handleUserNotRegistered);
    }

    public String register(User merchant) throws Exception, UserAlreadyExistsException {
        CorrelationId correlationId = CorrelationId.randomId();
        CompletableFuture<String> future = new CompletableFuture<>();
        correlations.put(correlationId, future);
        
        try {
            Event event = new Event("UserRegistrationRequested", new Object[]{merchant, correlationId});
            mq.publish(event);
            System.out.println("[UserService] Published UserRegistrationRequested event with correlationId: " + correlationId.getId());
            
            // Added timeout of 10 seconds to prevent indefinite blocking
            return future.orTimeout(10, java.util.concurrent.TimeUnit.SECONDS).join();
        } catch (java.util.concurrent.CompletionException e) {
            if (e.getCause() instanceof java.util.concurrent.TimeoutException) {
                System.err.println("[UserService] Timeout waiting for registration response after 10 seconds for correlationId: " + correlationId.getId());
                throw new Exception("Registration request timed out - no response from account service", e);
            }
            throw new Exception("Registration failed: " + e.getMessage(), e);
        } catch (Exception e) {
            System.err.println("[UserService] Exception during registration: " + e.getMessage());
            throw e;
        } finally {
            correlations.remove(correlationId);
        }
    }

    public void handleUserRegistered(Event e){
        String user = e.getArgument(0, String.class);
        CorrelationId correlationId = e.getArgument(1, CorrelationId.class);
        System.out.println("[UserService] Received UserRegistered event for correlationId: " + correlationId.getId() + " with userId: " + user);
        CompletableFuture<String> future = correlations.get(correlationId);
        if (future != null) {
            future.complete(user);
        } else {
            System.err.println("[UserService] WARNING: No pending future found for correlationId: " + correlationId.getId());
        }
    }

    public void handleUserNotRegistered(Event e) {
        String error = e.getArgument(0, String.class);
        CorrelationId correlationId = e.getArgument(1, CorrelationId.class);
        System.out.println("[UserService] Received UserNotRegistered event for correlationId: " + correlationId.getId() + " with error: " + error);
        CompletableFuture<String> future = correlations.get(correlationId);
        if(error.toLowerCase().contains("already exists")) {
            if (future != null) {
                future.completeExceptionally(new UserAlreadyExistsException());
            } else {
                System.err.println("[UserService] WARNING: No pending future found for correlationId: " + correlationId.getId());
            }
        } else {
            if (future != null) {
                future.completeExceptionally(new Exception(error));
            } else {
                System.err.println("[UserService] WARNING: No pending future found for correlationId: " + correlationId.getId());
            }
        }
    }

    public void unregisterUserById(String id) {

    }

    public boolean userExists(String id) {
        throw new UnsupportedOperationException("Not implemented yet");
    }

    public boolean pay(int amount, String cid, String mid) {
        throw new UnsupportedOperationException("Not implemented yet");
    }

}
