package dtu.pay;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ScenarioContext {
    public dtu.pay.User user;
    public dtu.pay.User customer;
    public dtu.pay.User merchant;
    public String bankAccountId;
    public String DTUPayAccountId;
    public String customerId;
    public String merchantId;
    public List<String> tokens;
    public Map<String, List<String>> tokensMap = new HashMap<>();
    public Throwable latestError;
}
