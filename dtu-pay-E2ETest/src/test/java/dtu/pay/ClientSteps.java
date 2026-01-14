package dtu.pay;

import dtu.ws.fastmoney.Account;
import dtu.ws.fastmoney.BankService;
import dtu.ws.fastmoney.BankServiceException_Exception;
import dtu.ws.fastmoney.BankService_Service;
import dtu.ws.fastmoney.User;
import io.cucumber.java.After;
import io.cucumber.java.Before;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class ClientSteps {
    private dtu.pay.User customer;
    private dtu.pay.User merchant;
    private String customerId, merchantId;
    private SimpleDtuPayClient dtupay = new SimpleDtuPayClient();
    private boolean successful = false;
    private List<Payment> payments;

    // Bank Service integration
    private BankService bank = new BankService_Service().getBankServicePort();
    private String bankApiKey = "amber2460";
    private List<String> bankAccounts = new ArrayList<>();
    private String customerBankAccNum;
    private String merchantBankAccNum;

    @Before
    public void beforeScenario() {
        // Create a fresh instance of SimpleDtuPayClient for each scenario
        dtupay = new SimpleDtuPayClient();
        customer = null;
        customerId = null;
        merchantId = null;
        successful = false;
        payments = null;
        bankAccounts.clear();
        customerBankAccNum = null;
        merchantBankAccNum = null;
    }

    @Given("a customer with name {string}, last name {string}, and CPR {string}")
    public void a_customer_with_name_last_name_and_cpr_client(String firstName, String lastName, String cpr) {
        customer = new dtu.pay.User(firstName, lastName, null, cpr);
    }

    @Given("the customer is registered with the bank with an initial balance of {int} kr")
    public void the_customer_is_registered_with_the_bank_with_an_initial_balance_of_kr_client(Integer initialBalance) throws BankServiceException_Exception {
        //convert customer to bank user
        User user = new User();
//        user.setCprNumber(user.getCprNumber());
//        user.setFirstName(user.getFirstName());
//        user.setLastName(user.getLastName());
        user.setCprNumber(customer.cprNumber());
        user.setFirstName(customer.firstName());
        user.setLastName(customer.lastName());

        customerBankAccNum = bank.createAccountWithBalance(bankApiKey, user, new BigDecimal(initialBalance));
        bankAccounts.add(customerBankAccNum);
    }

    @Given("the customer is registered with DTU Pay using their bank account")
    public void the_customer_is_registered_with_simple_dtu_pay_using_their_bank_account_client() {
        customer = new dtu.pay.User("John", "Doe", customerBankAccNum, null);
        customerId = dtupay.registerDTUPayAccount(customer);
    }

    @Given("a merchant with name {string}, last name {string}, and CPR {string}")
    public void a_merchant_with_name_last_name_and_cpr_client(String firstName, String lastName, String cpr) {
        merchant = new dtu.pay.User(firstName, lastName, merchantBankAccNum, null);
    }

    @Given("the merchant is registered with the bank with an initial balance of {bigdecimal} kr")
    public void the_merchant_is_registered_with_the_bank_with_an_initial_balance_of_kr_client(BigDecimal initialBalance) throws BankServiceException_Exception {
        User user = new User();
        user.setCprNumber(merchant.cprNumber());
        user.setFirstName(merchant.firstName());
        user.setLastName(merchant.lastName());

        merchantBankAccNum = bank.createAccountWithBalance(bankApiKey, user, initialBalance);
        bankAccounts.add(merchantBankAccNum);
    }

    @Given("the merchant is registered with Simple DTU Pay using their bank account")
    public void the_merchant_is_registered_with_simple_dtu_pay_using_their_bank_account_client() {
        merchant = new dtu.pay.User(merchant.firstName(), merchant.lastName(), merchant.cprNumber(), merchantBankAccNum);
        merchantId = dtupay.registerDTUPayAccount(merchant);
    }

    @When("the merchant initiates a payment for {bigdecimal} kr by the customer")
    public void the_merchant_initiates_a_payment_for_kr_by_the_customer_client(BigDecimal amount) {
        successful = dtupay.pay(amount, customerId, merchantId);
    }

    @Then("the payment is successful")
    public void the_payment_is_successful_client() {
        assertTrue(successful);
    }

    @Then("the balance of the customer at the bank is {int} kr")
    public void the_balance_of_the_customer_at_the_bank_is_kr_client(Integer expectedBalance) throws BankServiceException_Exception {
        Account account = bank.getAccount(customerBankAccNum);
        assertEquals(expectedBalance, account.getBalance().intValue());
    }

    @Then("the balance of the merchant at the bank is {int} kr")
    public void the_balance_of_the_merchant_at_the_bank_is_kr_client(Integer expectedBalance) throws BankServiceException_Exception {
        Account account = bank.getAccount(merchantBankAccNum);
        assertEquals(expectedBalance, account.getBalance().intValue());
    }

    @After
    public void afterScenario() {
        if (customerId != null) {
            dtupay.unregisterCustomer(customerId);
        }
        if (merchantId != null) {
            dtupay.unregisterMerchant(merchantId);
        }

        for (String account : bankAccounts) {
            try {
                bank.retireAccount(bankApiKey, account);
            } catch (BankServiceException_Exception e) {
                System.out.println("Could not retire account: " + account);
            }
        }
    }

    @Then("the account is created successfully")
    public void theAccountIsCreatedSuccessfully() {
        assertTrue(customerId != null || merchantId != null);
    }
}

