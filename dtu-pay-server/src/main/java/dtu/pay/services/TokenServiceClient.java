package dtu.pay.services;

import dtu.pay.tokens.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import messaging.Event;
import messaging.MessageQueue;

public class TokenServiceClient {
    private final MessageQueue mq;
    private final Map<String, CompletableFuture<List<String>>> tokenRequests = new ConcurrentHashMap<>();
    private final Map<String, CompletableFuture<String>> tokenConsumptions = new ConcurrentHashMap<>();

    public TokenServiceClient(MessageQueue mq) {
        this.mq = mq;
        mq.addHandler(TokenTopics.TOKENS_ISSUED, this::handleTokensIssued);
        mq.addHandler(TokenTopics.TOKEN_REQUEST_REJECTED, this::handleTokenRequestRejected);
    }

    public List<String> requestTokens(String customerId, int requestedCount) {
        String commandId = UUID.randomUUID().toString();
        CompletableFuture<List<String>> future = new CompletableFuture<>();
        tokenRequests.put(commandId, future);
        TokenRequestSubmitted command = new TokenRequestSubmitted(commandId, customerId, requestedCount,
                System.currentTimeMillis());
        mq.publish(new Event(TokenTopics.TOKEN_REQUEST_SUBMITTED, command));
        try {
            return future.get(5, TimeUnit.SECONDS);
        } catch (TimeoutException e) {
            tokenRequests.remove(commandId);
            throw new RuntimeException("Token request timed out for commandId " + commandId, e);
        } catch (ExecutionException e) {
            Throwable cause = e.getCause();
            if (cause instanceof RuntimeException runtimeException) {
                throw runtimeException;
            }
            throw new RuntimeException(cause == null ? e : cause);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Token request interrupted for commandId " + commandId, e);
        }
    }

    public String consumeToken(String token, String merchantId, Integer amount) {
        String commandId = UUID.randomUUID().toString();
        CompletableFuture<String> future = new CompletableFuture<>();
        tokenConsumptions.put(commandId, future);
        ConsumeTokenRequested command = new ConsumeTokenRequested(commandId, token, merchantId, amount,
                System.currentTimeMillis());
        mq.publish(new Event(TokenTopics.CONSUME_TOKEN_REQUESTED, command));
        try {
            return future.get(5, TimeUnit.SECONDS);
        } catch (TimeoutException e) {
            tokenConsumptions.remove(commandId);
            throw new RuntimeException("Token consumption timed out for commandId " + commandId, e);
        } catch (ExecutionException e) {
            Throwable cause = e.getCause();
            if (cause instanceof RuntimeException runtimeException) {
                throw runtimeException;
            }
            throw new RuntimeException(cause == null ? e : cause);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Token consumption interrupted for commandId " + commandId, e);
        }
    }

    private void handleTokensIssued(Event event) {
        TokensIssued issued = event.getArgument(0, TokensIssued.class);
        CompletableFuture<List<String>> future = tokenRequests.remove(issued.commandId());
        if (future != null) {
            future.complete(issued.tokens());
        }
    }

    private void handleTokenRequestRejected(Event event) {
        TokenRequestRejected rejected = event.getArgument(0, TokenRequestRejected.class);
        CompletableFuture<List<String>> future = tokenRequests.remove(rejected.commandId());
        if (future != null) {
            future.completeExceptionally(new RuntimeException(rejected.reason()));
        }
    }

    /// Fire and forget invalidation of all tokens for a customer
    /// TODO remove this method as we should only invalidate tokens when deleting a user
    public void invalidateTokens(String customerId) {
        TokenInvalidationRequested command = new TokenInvalidationRequested(customerId,
                System.currentTimeMillis());
        mq.publish(new Event(TokenTopics.TOKEN_INVALIDATION_REQUESTED, command));
    }
}
