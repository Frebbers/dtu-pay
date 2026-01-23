package dtu.token;

import dtu.token.messages.*;

import dtu.token.CorrelationId;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import io.vertx.codegen.doc.Token;
import messaging.Event;
import messaging.MessageQueue;

/// @author Christian Hyltoft
public class TokenService {
    private final MessageQueue mq;
    private final TokenStore store;
    private static final String TOKEN_VALIDATED = "TokenValidated";

    public TokenService(MessageQueue mq) {
        this(mq, new TokenStore());
    }

    /// package private for testing only
    TokenStore getTokenStoreForTest() {
        return this.store;
    }

    public TokenService(MessageQueue mq, TokenStore store) {
        this.mq = mq;
        this.store = store;
        mq.addHandler(TokenTopics.TOKEN_REQUEST_SUBMITTED, this::handleTokenRequestSubmitted);
        mq.addHandler(TokenTopics.USER_DEREGISTERED_REQUESTED, this::handleUserDeregistrationRequested);
        mq.addHandler(TokenTopics.PAYMENT_REQUESTED, this::handlePaymentRequested);
    }

    private void handlePaymentRequested(Event event) {
        PaymentRequest command;
        CorrelationId correlationId = null;
        try {
            command = event.getArgument(0, PaymentRequest.class);
            correlationId = event.getArgument(1, CorrelationId.class);
        } catch (Exception e) {
            if(correlationId != null){
                publishTokenConsumptionRejected(
                        new TokenConsumptionRejected(null, "Invalid payment request", now()), correlationId);
            }
            return;
        }

        consumeToken(safe(command.token()), correlationId);
    }

    private void consumeToken(String token, CorrelationId correlationId) {
        if (token.isEmpty()) {
            publishTokenConsumptionRejected(
                    new TokenConsumptionRejected(null, "Token is required", now()),
                    correlationId);
            return;
        }
        TokenRecord record = store.consumeToken(token, now());
        if (record == null) {
            publishTokenConsumptionRejected(new TokenConsumptionRejected(token,
                            "Token is invalid or already used", now()), correlationId);
            return;
        }
        publishTokenValidated(record.getCustomerId(), correlationId);
        publishTokenConsumed(new TokenConsumed(token, record.getCustomerId(), now()), correlationId);
    }

    private void removeTokenForUser(String userId) {
        store.invalidateTokens(userId);
    }

    private void handleUserDeregistrationRequested(Event event) {
        String userId = event.getArgument(0, String.class);
        removeTokenForUser(userId);
    }


    private void handleTokenRequestSubmitted(Event event) {
        TokenRequestSubmitted command;
        CorrelationId correlationId;
        try {
            command = event.getArgument(0, TokenRequestSubmitted.class);

        } catch (Exception e) {
            publishTokenRequestRejected(new TokenRequestRejected(UUID.randomUUID().toString(), null,
                    "Invalid token request", now()));
            return;
        }
        String commandId = ensureId(command.commandId());
        String customerId = safe(command.customerId());
        String reason = getErrorMessageIfAny(command);
        if (reason != null) { // reject the request
            publishTokenRequestRejected(new TokenRequestRejected(commandId, customerId, reason, now()));
            return;
        }
        int requestedCount = command.requestedCount();
        List<String> tokens = store.issueTokens(customerId, requestedCount, now());
        publishTokensIssued(new TokensIssued(commandId, customerId, tokens.size(), tokens, now()));
    }

    private String getErrorMessageIfAny(TokenRequestSubmitted command){
        String customerId = safe(command.customerId());
        int requestedCount = command.requestedCount();

        if (customerId.isEmpty()) {return"Customer id is required";}
        if (requestedCount < 1 || requestedCount > 5) {return "Requested count must be between 1 and 5";}
        int unusedCount = store.unusedCount(customerId);
        if (unusedCount > 1) {return "Customer has more than one unused token";}
        if (unusedCount + requestedCount > 6) {return "Requested tokens exceed unused token limit";}
        return null;
    }


    private void publishTokensIssued(TokensIssued issued) {
        mq.publish(new Event(TokenTopics.TOKENS_ISSUED, issued));
    }

    private void publishTokenRequestRejected(TokenRequestRejected rejected) {
        mq.publish(new Event(TokenTopics.TOKEN_REQUEST_REJECTED, rejected));
    }

    private void publishTokenConsumed(TokenConsumed consumed, CorrelationId correlationId) {
        mq.publish(new Event(TokenTopics.TOKEN_CONSUMED, consumed, correlationId));
    }

    private void publishTokenConsumptionRejected(TokenConsumptionRejected rejected, CorrelationId correlationId) {
        mq.publish(new Event(TokenTopics.TOKEN_CONSUMPTION_REJECTED, rejected, correlationId));
    }

    private void publishTokenValidated(String customerId, CorrelationId correlationId) {
        mq.publish(new Event(TOKEN_VALIDATED, customerId, correlationId));
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
