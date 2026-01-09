package dtu.pay;

import dtu.ws.fastmoney.*;
import io.cucumber.java.After;
import io.cucumber.java.Before;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class BankSteps {
    BankService bank = new BankService_Service().getBankServicePort();
    private SimpleDtuPayService dtupay = new SimpleDtuPayService();
    List<String> accounts = new ArrayList<>();
    String bankApiKey = "amber2460";
    Customer customer;
    Merchant merchant;
    String customerId;
    String merchantId;
    String customerBankAccNum;
    String merchantBankAccNum;
    boolean successful;

    @Before
    public void beforeScenario() {
        dtupay = new SimpleDtuPayService();
        accounts.clear();
        customer = null;
        merchant = null;
        customerId = null;
        merchantId = null;
        customerBankAccNum = null;
        merchantBankAccNum = null;
        successful = false;
    }

    @Given("a customer with name {string}, last name {string}, and CPR {string}")
    public void aCustomerWithNameLastNameAndCPR(String firstName, String lastName, String cpr) {
        customer = new Customer(firstName, lastName, cpr, null);
    }

    @Given("the customer is registered with the bank with an initial balance of {int} kr")
    public void theCustomerIsRegisteredWithTheBankWithAnInitialBalanceOfKr(Integer initialBalance) throws BankServiceException_Exception {
        User user = new User();
        user.setCprNumber(customer.cprNumber());
        user.setFirstName(customer.firstName());
        user.setLastName(customer.lastName());

        customerBankAccNum = bank.createAccountWithBalance(bankApiKey, user, new BigDecimal(initialBalance));
        accounts.add(customerBankAccNum);
    }

    @Given("the customer is registered with Simple DTU Pay using their bank account")
    public void theCustomerIsRegisteredWithSimpleDTUPayUsingTheirBankAccount() {
        customer = new Customer(customer.firstName(), customer.lastName(), customer.cprNumber(), customerBankAccNum);
        customerId = dtupay.register(customer);
    }

    @Given("a merchant with name {string}, last name {string}, and CPR {string}")
    public void aMerchantWithNameLastNameAndCPR(String firstName, String lastName, String cpr) {
        merchant = new Merchant(firstName, lastName, cpr, null);
    }

    @Given("the merchant is registered with the bank with an initial balance of {int} kr")
    public void theMerchantIsRegisteredWithTheBankWithAnInitialBalanceOfKr(Integer initialBalance) throws BankServiceException_Exception {
        User user = new User();
        user.setCprNumber(merchant.cprNumber());
        user.setFirstName(merchant.firstName());
        user.setLastName(merchant.lastName());

        merchantBankAccNum = bank.createAccountWithBalance(bankApiKey, user, new BigDecimal(initialBalance));
        accounts.add(merchantBankAccNum);
    }

    @Given("the merchant is registered with Simple DTU Pay using their bank account")
    public void theMerchantIsRegisteredWithSimpleDTUPayUsingTheirBankAccount() {
        merchant = new Merchant(merchant.firstName(), merchant.lastName(), merchant.cprNumber(), merchantBankAccNum);
        merchantId = dtupay.register(merchant);
    }

    @When("the merchant initiates a payment for {int} kr by the customer")
    public void theMerchantInitiatesAPaymentForKrByTheCustomerWithBank(Integer amount) {
        successful = dtupay.pay(amount, customerId, merchantId);
    }

    @Then("the payment is successful")
    public void thePaymentIsSuccessfulWithBank() {
        assertTrue(successful, "Payment should be successful. Error: " + dtupay.getLatestError());
    }

    @Then("the balance of the customer at the bank is {int} kr")
    public void theBalanceOfTheCustomerAtTheBankIsKr(Integer expectedBalance) throws BankServiceException_Exception {
        Account account = bank.getAccount(customerBankAccNum);
        assertEquals(expectedBalance, account.getBalance().intValue());
    }

    @Then("the balance of the merchant at the bank is {int} kr")
    public void theBalanceOfTheMerchantAtTheBankIsKr(Integer expectedBalance) throws BankServiceException_Exception {
        Account account = bank.getAccount(merchantBankAccNum);
        assertEquals(expectedBalance, account.getBalance().intValue());
    }

    @After
    public void afterScenario() throws BankServiceException_Exception {
        for (String account : accounts) {
            try {
                bank.retireAccount(bankApiKey, account);
            } catch (BankServiceException_Exception e) {
                System.err.println("Failed to retire account: " + account + ". " + e.getMessage());
            }
        }

        if (customerId != null) {
            dtupay.unregisterCustomerById(customerId);
        }
        if (merchantId != null) {
            dtupay.unregisterMerchantById(merchantId);
        }
    }
}

