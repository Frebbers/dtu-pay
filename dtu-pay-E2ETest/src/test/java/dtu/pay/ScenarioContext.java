package dtu.pay;

import java.util.List;

public class ScenarioContext {
    public dtu.pay.User customer;
    public String bankAccountId;
    public String DTUPayAccountId;
    public List<String> tokens;
    public Throwable latestError;
}
