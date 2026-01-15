package dtu.pay.services;

import dtu.pay.tokens.ConsumeTokenRequested;
import dtu.pay.tokens.TokenConsumed;
import dtu.pay.tokens.TokenConsumptionRejected;
import dtu.pay.tokens.TokenRequestRejected;
import dtu.pay.tokens.TokenRequestSubmitted;
import dtu.pay.tokens.TokenTopics;
import dtu.pay.tokens.TokensIssued;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
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
        mq.addHandler(TokenTopics.TOKEN_CONSUMED, this::handleTokenConsumed);
        mq.addHandler(TokenTopics.TOKEN_CONSUMPTION_REJECTED, this::handleTokenConsumptionRejected);
    }

    public List<String> requestTokens(String customerId, int requestedCount) {
        String commandId = UUID.randomUUID().toString();
        CompletableFuture<List<String>> future = new CompletableFuture<>();
        tokenRequests.put(commandId, future);
        TokenRequestSubmitted command = new TokenRequestSubmitted(commandId, customerId, requestedCount,
                System.currentTimeMillis());
        mq.publish(new Event(TokenTopics.TOKEN_REQUEST_SUBMITTED, command));
        return future.join();
    }

    public String consumeToken(String token, String merchantId, Integer amount) {
        String commandId = UUID.randomUUID().toString();
        CompletableFuture<String> future = new CompletableFuture<>();
        tokenConsumptions.put(commandId, future);
        ConsumeTokenRequested command = new ConsumeTokenRequested(commandId, token, merchantId, amount,
                System.currentTimeMillis());
        mq.publish(new Event(TokenTopics.CONSUME_TOKEN_REQUESTED, command));
        return future.join();
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

    private void handleTokenConsumed(Event event) {
        TokenConsumed consumed = event.getArgument(0, TokenConsumed.class);
        CompletableFuture<String> future = tokenConsumptions.remove(consumed.commandId());
        if (future != null) {
            future.complete(consumed.customerId());
        }
    }

    private void handleTokenConsumptionRejected(Event event) {
        TokenConsumptionRejected rejected = event.getArgument(0, TokenConsumptionRejected.class);
        CompletableFuture<String> future = tokenConsumptions.remove(rejected.commandId());
        if (future != null) {
            future.completeExceptionally(new RuntimeException(rejected.reason()));
        }
    }
}
