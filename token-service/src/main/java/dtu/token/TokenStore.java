package dtu.token;

import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Base64;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
/// @author Fadl Matar - s195846

public class TokenStore {
    private final SecureRandom random = new SecureRandom();
    private final Map<String, TokenRecord> tokensByValue = new ConcurrentHashMap<>();
    private final Map<String, Set<String>> tokensByCustomer = new ConcurrentHashMap<>();

    public synchronized int unusedCount(String customerId) {
        Set<String> tokens = tokensByCustomer.get(customerId);
        if (tokens == null) {
            return 0;
        }
        int count = 0;
        for (String token : tokens) {
            TokenRecord record = tokensByValue.get(token);
            if (record != null && record.getStatus() == TokenStatus.UNUSED) {
                count++;
            }
        }
        return count;
    }

    public synchronized List<String> issueTokens(String customerId, int count, long issuedAt) {
        List<String> issued = new ArrayList<>(count);
        Set<String> customerTokens = tokensByCustomer.computeIfAbsent(customerId, id -> new HashSet<>());
        for (int i = 0; i < count; i++) {
            String token = generateUniqueToken();
            TokenRecord record = new TokenRecord(token, customerId, issuedAt);
            tokensByValue.put(token, record);
            customerTokens.add(token);
            issued.add(token);
        }
        return issued;
    }

    public synchronized TokenRecord consumeToken(String token, long usedAt) {
        TokenRecord record = tokensByValue.get(token);
        if (record == null || record.getStatus() == TokenStatus.USED) {
            return null;
        }
        record.markUsed(usedAt);
        return record;
    }

    private String generateUniqueToken() {
        String token;
        do {
            token = generateToken();
        } while (tokensByValue.containsKey(token));
        return token;
    }

    private String generateToken() {
        byte[] bytes = new byte[16];
        random.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    public void invalidateTokens(String userId) {
        try {
            Set<String> tokens = tokensByCustomer.get(userId);
            if (!tokens.isEmpty()) {
                for (String token : tokens) {
                    tokensByValue.remove(token);
                }
                tokensByCustomer.remove(userId);
            }
        } catch (Exception ignored) {
        }
    }
}
