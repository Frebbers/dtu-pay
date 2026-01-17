package dtu.pay;

import dtu.ws.fastmoney.BankService;
import dtu.ws.fastmoney.BankServiceException_Exception;
import dtu.ws.fastmoney.BankService_Service;
import io.cucumber.java.en.Given;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class CommonCustomerSteps {

    private String firstName;
    private String lastName;
    private String cpr;
    private final ScenarioContext context;
    private BankService bank = new BankService_Service().getBankServicePort();
    private String bankApiKey = "amber2460";
    private DtuPayClient dtupay = new DtuPayClient();
    private String createdUserBankAccNumber;
    private List<String> bankAccounts = new ArrayList<>();
    private dtu.pay.User customer;
    private String DTUPayAccountId;
    private List<String> tokens;
    private Throwable latestError;

    @Given("User with CPR {string} is not registered in the bank")
    public void userWithCPRIsCleanedUp(String cpr) {

        try {
            var acc = bank.getAccountByCprNumber(cpr);
            bank.retireAccount(bankApiKey, acc.getId());
        }
        catch (BankServiceException_Exception ignored) {}
    }

    public CommonCustomerSteps(ScenarioContext context) {
        this.context = context;
    }

    @Given("a user with name {string}, last name {string}, and CPR {string}")
    public void aUserWithNameLastNameAndCpr(String first, String last, String cpr) {
        context.customer = new dtu.pay.User(first, last, null, cpr);
    }

    @Given("the user is registered with the bank with an initial balance of {int} kr")
    public void registerInBank(int balance) throws BankServiceException_Exception {
        var user = new dtu.ws.fastmoney.User();
        user.setFirstName(context.customer.firstName());
        user.setLastName(context.customer.lastName());
        user.setCprNumber(context.customer.cprNumber());

        context.bankAccountId =
                bank.createAccountWithBalance(bankApiKey, user, new BigDecimal(balance));
    }

    @Given("the customer is registered with Simple DTU Pay using their bank account")
    public void registerInDtuPay() {
        var request = new dtu.pay.User(
                context.customer.firstName(),
                context.customer.lastName(),
                context.bankAccountId,
                null
        );
        context.DTUPayAccountId = new DtuPayClient().registerDTUPayCustomer(request);
    }
}

