package dtu.tokens;

public record TokenInvalidationRequested (String userId, long requestedAt) {}
