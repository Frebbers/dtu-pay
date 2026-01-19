package dtu.pay;

import io.cucumber.java.PendingException;
import io.cucumber.java.en.And;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;

import java.math.BigDecimal;

public class PaymentSteps {
    private DtuPayClient dtupay = new DtuPayClient();
    private final ScenarioContext context;

    public PaymentSteps(ScenarioContext context) {
        this.context = context;
    }

    @When("the merchant initiates a payment for {int} kr by the customer")
    public void theMerchantInitiatesAPaymentForKrByTheCustomer(int cost) {
        var token = context.tokens.getFirst();
        dtupay.pay(new BigDecimal(cost), context.customer.cprNumber(), context.merchant.cprNumber(), token);

    }

    @Then("the payment is successful")
    public void thePaymentIsSuccessful() {
        // Write code here that turns the phrase above into concrete actions
        throw new PendingException();
    }

    @And("the balance of the customer at the bank is {int} kr")
    public void theBalanceOfTheCustomerAtTheBankIsKr(int expectedCustomerBalance) {
        // Write code here that turns the phrase above into concrete actions
        throw new PendingException();
    }

    @And("the balance of the merchant at the bank is {int} kr")
    public void theBalanceOfTheMerchantAtTheBankIsKr(int expectedMerchantBalance) {
        // Write code here that turns the phrase above into concrete actions
        throw new PendingException();
    }
}
