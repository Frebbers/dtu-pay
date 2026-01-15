package dtu.token.messages;

import java.util.List;

public record TokensIssued(String commandId, String customerId, int issuedCount, List<String> tokens, long issuedAt) {
}
