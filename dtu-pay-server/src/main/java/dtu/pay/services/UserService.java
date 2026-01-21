package dtu.pay.services;

import dtu.pay.models.User;
import dtu.pay.models.exceptions.ConflictException;
import messaging.Event;
import messaging.MessageQueue;
import jakarta.ws.rs.NotFoundException;

import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.ConcurrentHashMap;

public class UserService {
    private final MessageQueue mq;
    private final Map<CorrelationId, CompletableFuture<String>> correlations = new ConcurrentHashMap<>();

    public UserService(MessageQueue mq) {
        this.mq = mq;
        mq.addHandler("UserRegistered", this::handleUserRegistered);
        mq.addHandler("UserAlreadyRegistered", this::handleUserAlreadyRegistered);
        mq.addHandler("UserRegistrationFailed", this::handleUserNotRegistered);

        mq.addHandler("UserDeregistered", this::handleUserDeregistered);
        mq.addHandler("UserDoesNotExist", this::handleUserDoesNotExist);
        mq.addHandler("UserDeregistrationFailed", this::handleUserNotDeregistered);

    }

    public String register(User merchant) throws ConflictException {
        System.out.println("DTU Pay publishing UserRegistrationRequested: "
                + "first=" + merchant.firstName()
                + " last=" + merchant.lastName()
                + " cpr=" + merchant.cprNumber()
                + " bank=" + merchant.bankAccountNum());
        CorrelationId correlationId = CorrelationId.randomId();
        CompletableFuture<String> future = new CompletableFuture<>();
        correlations.put(correlationId, future);
        Event event = new Event("UserRegistrationRequested", new Object[] { merchant, correlationId });
        mq.publish(event);
        try {
            return future.join();
        } catch (CompletionException e) {
            if (e.getCause() instanceof ConflictException ex) {
                throw ex;
            }
            throw e;
        } finally {
            correlations.remove(correlationId);
        }
    }

    public void handleUserRegistered(Event e) {
        String user = e.getArgument(0, String.class);
        CorrelationId correlationId = e.getArgument(1, CorrelationId.class);
        CompletableFuture<String> future = correlations.remove(correlationId);
        if (future != null) {
            future.complete(user);
        }
    }

    public void handleUserAlreadyRegistered(Event e) {
        String message = e.getArgument(0, String.class);
        CorrelationId correlationId = e.getArgument(1, CorrelationId.class);
        CompletableFuture<String> future = correlations.remove(correlationId);

        if (future != null) {
            future.completeExceptionally(new ConflictException(message));
        }
    }

    public void handleUserNotRegistered(Event e) {
        String error = e.getArgument(0, String.class);
        CorrelationId correlationId = e.getArgument(1, CorrelationId.class);
        CompletableFuture<String> future = correlations.remove(correlationId);
        if (future != null) {
            future.completeExceptionally(new RuntimeException(error));
        }
    }

    public void unregisterUserById(String id) {
        CorrelationId correlationId = CorrelationId.randomId();
        CompletableFuture<String> future = new CompletableFuture<>();
        correlations.put(correlationId, future);

        Event event = new Event("UserDeregistrationRequested",
                new Object[] { id, correlationId });
        mq.publish(event);

        try {
            future.join();
        } catch (CompletionException e) {
            if (e.getCause() instanceof NotFoundException nf) {
                throw nf;
            }
            throw e;
        } finally {
            correlations.remove(correlationId);
        }
    }

    public void handleUserDeregistered(Event e) {
        String id = e.getArgument(0, String.class);
        CorrelationId correlationId = e.getArgument(1, CorrelationId.class);

        CompletableFuture<String> future = correlations.remove(correlationId);
        if (future != null) {
            future.complete(id);
        }
    }

    public void handleUserDoesNotExist(Event e) {
        String message = e.getArgument(0, String.class);
        CorrelationId correlationId = e.getArgument(1, CorrelationId.class);

        CompletableFuture<String> future = correlations.remove(correlationId);
        if (future != null) {
            future.completeExceptionally(new NotFoundException(message));
        }
    }

    public void handleUserNotDeregistered(Event e) {
        String error = e.getArgument(0, String.class);
        CorrelationId correlationId = e.getArgument(1, CorrelationId.class);

        CompletableFuture<String> future = correlations.remove(correlationId);
        if (future != null) {
            future.completeExceptionally(new RuntimeException(error));
        }
    }

    public boolean userExists(String id) {
        throw new UnsupportedOperationException("Not implemented yet");
    }

    public boolean pay(int amount, String cid, String mid) {
        throw new UnsupportedOperationException("Not implemented yet");
    }

}
