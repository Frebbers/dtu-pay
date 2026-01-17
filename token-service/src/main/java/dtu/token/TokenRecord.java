package dtu.token;

public class TokenRecord {
    private final String token;
    private final String customerId;
    private final long issuedAt;
    private TokenStatus status;
    private Long usedAt;

    public TokenRecord(String token, String customerId, long issuedAt) {
        this.token = token;
        this.customerId = customerId;
        this.issuedAt = issuedAt;
        this.status = TokenStatus.UNUSED;
    }

    public String getToken() {
        return token;
    }

    public String getCustomerId() {
        return customerId;
    }

    public long getIssuedAt() {
        return issuedAt;
    }

    public TokenStatus getStatus() {
        return status;
    }

    public Long getUsedAt() {
        return usedAt;
    }

    public void markUsed(long usedAt) {
        this.status = TokenStatus.USED;
        this.usedAt = usedAt;
    }
}
