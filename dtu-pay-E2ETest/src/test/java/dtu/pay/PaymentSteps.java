package dtu.pay;

import dtu.ws.fastmoney.BankService;
import dtu.ws.fastmoney.BankServiceException_Exception;
import dtu.ws.fastmoney.BankService_Service;
import io.cucumber.java.PendingException;
import io.cucumber.java.en.And;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.junit.Assert;

import java.math.BigDecimal;

public class PaymentSteps {
    private DtuPayClient dtupay = new DtuPayClient();
    private final ScenarioContext context;
    private BankService bank = new BankService_Service().getBankServicePort();
    private String bankApiKey = "amber2460";

    public PaymentSteps(ScenarioContext context) {
        this.context = context;
    }

    @When("the merchant initiates a payment for {int} kr by the customer")
    public void theMerchantInitiatesAPaymentForKrByTheCustomer(int cost) {
        try {
            dtupay.pay(context.tokens.getFirst(), context.merchant.cprNumber(), new BigDecimal(cost));
        }
        catch(Exception e){
                context.latestError = e;
            }
        }

    @Then("the payment is successful")
    public void thePaymentIsSuccessful() {
        String latestErrorMessage = null;
        try {
            latestErrorMessage = context.latestError.getMessage();
        } catch (Exception ignored) {}
                    Assert.assertNull("An error occurred during payment: "
                            + latestErrorMessage, latestErrorMessage);
        }


    @And("the balance of the customer at the bank is {int} kr")
    public void theBalanceOfTheCustomerAtTheBankIsKr(int expectedCustomerBalance) {
        dtu.ws.fastmoney.Account account;
        try {account = bank.getAccountByCprNumber(context.customer.cprNumber());
        } catch (BankServiceException_Exception e) {
            throw new RuntimeException(e);
        }
        Assert.assertEquals(new BigDecimal(expectedCustomerBalance),account.getBalance());
    }

    @And("the balance of the merchant at the bank is {int} kr")
    public void theBalanceOfTheMerchantAtTheBankIsKr(int expectedMerchantBalance) {
        dtu.ws.fastmoney.Account account;
        try {account = bank.getAccountByCprNumber(context.merchant.cprNumber());
        } catch (BankServiceException_Exception e) {
            throw new RuntimeException(e);
        }
        Assert.assertEquals(account.getBalance(), new BigDecimal(expectedMerchantBalance));
    }
}
