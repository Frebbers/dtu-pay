package dtu.token.messages;

public record TokenInvalidationRequested(String userId, long requestedAt) {}
