package dtu.pay;

import dtu.ws.fastmoney.BankService;
import dtu.ws.fastmoney.BankServiceException_Exception;
import dtu.ws.fastmoney.BankService_Service;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class DTUPayAccountSteps {

    private final ScenarioContext context;


    public DTUPayAccountSteps(ScenarioContext context) {
        this.context = context;
    }

    @Given("a user with name {string}, last name {string}, and CPR {string}")
    public void aUserWithNameLastNameAndCpr(String first, String last, String cpr) {
        context.user = new dtu.pay.User(first, last, null, cpr);
    }


    @Given("the customer is registered with Simple DTU Pay using their bank account")
    public void registerInDtuPay() {
        var request = new dtu.pay.User(
                context.user.firstName(),
                context.user.lastName(),
                context.bankAccountId,
                null
        );
        try{context.DTUPayAccountId = new DtuPayClient().registerDTUPayCustomer(request);}
        catch (Exception e){context.latestError = e;}
    }

    @Given("the merchant is registered with Simple DTU Pay using their bank account")
    public void theMerchantIsRegisteredWithSimpleDTUPayUsingTheirBankAccount() {
        var request = new dtu.pay.User(
                context.user.firstName(),
                context.user.lastName(),
                context.bankAccountId,
                null
        );
        context.DTUPayAccountId = new DtuPayClient().registerDTUPayCustomer(request);
    }
    @When("the user attempts to register again with Simple DTU Pay using the same bank account")
    public void theUserAttemptsToRegisterAgainWithSimpleDTUPayUsingTheSameBankAccount() {
        // Write code here that turns the phrase above into concrete actions
        assert context.latestError == null;
        registerInDtuPay();
    }
    @Then("the DTU Pay registration fails with error {string}")
    public void theDTUPayRegistrationFailsWithError(String expectedErrorMessage) {
        assert context.latestError.getMessage().contains(expectedErrorMessage);
    }
}

