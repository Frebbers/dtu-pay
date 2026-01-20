package dtu.token;

import dtu.token.messages.*;

import java.util.List;
import java.util.UUID;
import messaging.Event;
import messaging.MessageQueue;

/// @author Christian Hyltoft
public class TokenService {
    private final MessageQueue mq;
    private final TokenStore store;

    public TokenService(MessageQueue mq) {
        this(mq, new TokenStore());
    }

    public TokenService(MessageQueue mq, TokenStore store) {
        this.mq = mq;
        this.store = store;
        mq.addHandler(TokenTopics.TOKEN_REQUEST_SUBMITTED, this::handleTokenRequestSubmitted);
        mq.addHandler(TokenTopics.CONSUME_TOKEN_REQUESTED, this::handleConsumeTokenRequested);
        mq.addHandler(TokenTopics.TOKEN_INVALIDATION_REQUESTED, this::handleTokenInvalidationRequested);
    }

    private void handleTokenInvalidationRequested(Event event) {
        TokenInvalidationRequested command = event.getArgument(0, TokenInvalidationRequested.class);
        String userId = command.userId();
        store.invalidateTokens(userId);
    }

    private void handleTokenRequestSubmitted(Event event) {
        TokenRequestSubmitted command;
        try {
            command = event.getArgument(0, TokenRequestSubmitted.class);
        } catch (Exception e) {
            publishTokenRequestRejected(new TokenRequestRejected(UUID.randomUUID().toString(), null,
                    "Invalid token request", now()));
            return;
        }

        String commandId = ensureId(command.commandId());
        String customerId = safe(command.customerId());
        int requestedCount = command.requestedCount();

        if (customerId.isEmpty()) {
            publishTokenRequestRejected(new TokenRequestRejected(commandId, null,
                    "Customer id is required", now()));
            return;
        }

        if (requestedCount < 1 || requestedCount > 5) {
            publishTokenRequestRejected(new TokenRequestRejected(commandId, customerId,
                    "Requested count must be between 1 and 5", now()));
            return;
        }

        int unusedCount = store.unusedCount(customerId);
        if (unusedCount > 1) {
            publishTokenRequestRejected(new TokenRequestRejected(commandId, customerId,
                    "Customer has more than one unused token", now()));
            return;
        }

        if (unusedCount + requestedCount > 6) {
            publishTokenRequestRejected(new TokenRequestRejected(commandId, customerId,
                    "Requested tokens exceed unused token limit", now()));
            return;
        }

        List<String> tokens = store.issueTokens(customerId, requestedCount, now());
        publishTokensIssued(new TokensIssued(commandId, customerId, tokens.size(), tokens, now()));
    }

    private void handleConsumeTokenRequested(Event event) {
        ConsumeTokenRequested command;
        try {
            command = event.getArgument(0, ConsumeTokenRequested.class);
        } catch (Exception e) {
            publishTokenConsumptionRejected(new TokenConsumptionRejected(UUID.randomUUID().toString(), null,
                    "Invalid token consumption request", now()));
            return;
        }

        String commandId = ensureId(command.commandId());
        String token = safe(command.token());
        if (token.isEmpty()) {
            publishTokenConsumptionRejected(new TokenConsumptionRejected(commandId, null,
                    "Token is required", now()));
            return;
        }

        TokenRecord record = store.consumeToken(token, now());
        if (record == null) {
            publishTokenConsumptionRejected(new TokenConsumptionRejected(commandId, token,
                    "Token is invalid or already used", now()));
            return;
        }

        publishTokenConsumed(new TokenConsumed(commandId, record.getToken(), record.getCustomerId(), now()));
    }

    private void publishTokensIssued(TokensIssued issued) {
        mq.publish(new Event(TokenTopics.TOKENS_ISSUED, issued));
    }

    private void publishTokenRequestRejected(TokenRequestRejected rejected) {
        mq.publish(new Event(TokenTopics.TOKEN_REQUEST_REJECTED, rejected));
    }

    private void publishTokenConsumed(TokenConsumed consumed) {
        mq.publish(new Event(TokenTopics.TOKEN_CONSUMED, consumed));
    }

    private void publishTokenConsumptionRejected(TokenConsumptionRejected rejected) {
        mq.publish(new Event(TokenTopics.TOKEN_CONSUMPTION_REJECTED, rejected));
    }

    private static String safe(String value) {
        return value == null ? "" : value.trim();
    }

    private static String ensureId(String commandId) {
        String trimmed = safe(commandId);
        return trimmed.isEmpty() ? UUID.randomUUID().toString() : trimmed;
    }

    private static long now() {
        return System.currentTimeMillis();
    }
}
