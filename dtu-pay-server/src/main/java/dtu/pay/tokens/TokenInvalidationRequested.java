package dtu.pay.tokens;

public record TokenInvalidationRequested (String userId, long requestedAt) {}
