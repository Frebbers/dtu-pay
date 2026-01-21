package dtu.token;

public final class TokenTopics {
    public static final String TOKEN_REQUEST_SUBMITTED = "token.commands.TokenRequestSubmitted";
    public static final String CONSUME_TOKEN_REQUESTED = "token.commands.ConsumeTokenRequested";
    public static final String TOKENS_ISSUED = "token.events.TokensIssued";
    public static final String TOKEN_REQUEST_REJECTED = "token.events.TokenRequestRejected";
    public static final String TOKEN_CONSUMED = "token.events.TokenConsumed";
    public static final String TOKEN_CONSUMPTION_REJECTED = "token.events.TokenConsumptionRejected";
    public static final String TOKEN_INVALIDATION_REQUESTED = "token.events.TokenInvalidationRequested";
    public static final String USER_DEREGISTERED_REQUESTED = "UserDeregistrationRequested";

    private TokenTopics() {
    }
}
