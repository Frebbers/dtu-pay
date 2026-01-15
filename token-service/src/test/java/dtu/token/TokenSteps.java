package dtu.token;

import dtu.token.messages.ConsumeTokenRequested;
import dtu.token.messages.TokenConsumed;
import dtu.token.messages.TokenRequestRejected;
import dtu.token.messages.TokenRequestSubmitted;
import dtu.token.messages.TokensIssued;
import io.cucumber.java.Before;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.function.Consumer;
import messaging.Event;
import messaging.MessageQueue;
import org.junit.jupiter.api.Assertions;

public class TokenSteps {
    private TestMessageQueue mq;
    private TokenService service;
    private String customerId;
    private List<String> issuedTokens;
    private Event lastPublished;

    @Before
    public void setup() {
        mq = new TestMessageQueue();
        service = new TokenService(mq, new TokenStore());
        customerId = null;
        issuedTokens = new ArrayList<>();
        lastPublished = null;
    }

    @Given("a customer id {string}")
    public void aCustomerId(String customerId) {
        this.customerId = customerId;
    }

    @Given("the customer already has {int} unused tokens")
    public void theCustomerAlreadyHasUnusedTokens(int count) {
        requestTokens(count);
    }

    @When("the customer requests {int} tokens")
    public void theCustomerRequestsTokens(int count) {
        requestTokens(count);
    }

    @When("the customer consumes a token")
    public void theCustomerConsumesAToken() {
        Assertions.assertFalse(issuedTokens.isEmpty(), "No tokens available to consume");
        String token = issuedTokens.get(0);
        ConsumeTokenRequested command = new ConsumeTokenRequested(UUID.randomUUID().toString(), token, null, null,
                System.currentTimeMillis());
        mq.send(new Event(TokenTopics.CONSUME_TOKEN_REQUESTED, command));
        lastPublished = mq.lastPublished();
    }

    @Then("tokens are issued with count {int} for customer {string}")
    public void tokensAreIssuedWithCountForCustomer(int count, String customerId) {
        Assertions.assertNotNull(lastPublished, "No event published");
        Assertions.assertEquals(TokenTopics.TOKENS_ISSUED, lastPublished.getTopic());
        TokensIssued issued = lastPublished.getArgument(0, TokensIssued.class);
        Assertions.assertEquals(count, issued.issuedCount());
        Assertions.assertEquals(customerId, issued.customerId());
        Assertions.assertEquals(count, issued.tokens().size());
        Set<String> unique = new HashSet<>(issued.tokens());
        Assertions.assertEquals(count, unique.size());
    }

    @Then("the token request is rejected")
    public void theTokenRequestIsRejected() {
        Assertions.assertNotNull(lastPublished, "No event published");
        Assertions.assertEquals(TokenTopics.TOKEN_REQUEST_REJECTED, lastPublished.getTopic());
        lastPublished.getArgument(0, TokenRequestRejected.class);
    }

    @Then("the token is consumed for customer {string}")
    public void theTokenIsConsumedForCustomer(String customerId) {
        Assertions.assertNotNull(lastPublished, "No event published");
        Assertions.assertEquals(TokenTopics.TOKEN_CONSUMED, lastPublished.getTopic());
        TokenConsumed consumed = lastPublished.getArgument(0, TokenConsumed.class);
        Assertions.assertEquals(customerId, consumed.customerId());
    }

    private void requestTokens(int count) {
        TokenRequestSubmitted command = new TokenRequestSubmitted(UUID.randomUUID().toString(), customerId, count,
                System.currentTimeMillis());
        mq.send(new Event(TokenTopics.TOKEN_REQUEST_SUBMITTED, command));
        lastPublished = mq.lastPublished();
        if (TokenTopics.TOKENS_ISSUED.equals(lastPublished.getTopic())) {
            TokensIssued issued = lastPublished.getArgument(0, TokensIssued.class);
            issuedTokens = new ArrayList<>(issued.tokens());
        }
    }

    private static class TestMessageQueue implements MessageQueue {
        private final Map<String, Consumer<Event>> handlers = new HashMap<>();
        private final List<Event> published = new ArrayList<>();

        @Override
        public void publish(Event event) {
            published.add(event);
        }

        @Override
        public void addHandler(String topic, Consumer<Event> handler) {
            handlers.put(topic, handler);
        }

        public void send(Event event) {
            Consumer<Event> handler = handlers.get(event.getTopic());
            if (handler != null) {
                handler.accept(event);
            }
        }

        public Event lastPublished() {
            if (published.isEmpty()) {
                return null;
            }
            return published.get(published.size() - 1);
        }
    }
}
