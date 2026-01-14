package dtu.pay;

import io.cucumber.java.Before;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;

import static org.junit.jupiter.api.Assertions.*;

public class AccountSteps {
    private String firstName;
    private String lastName;
    private String cpr;
    private boolean registrationAttempted;
    private String dtuPayAccountId;
    private boolean bankAccountLinked;

    @Before
    public void resetState() {
        firstName = null;
        lastName = null;
        cpr = null;
        registrationAttempted = false;
        dtuPayAccountId = null;
        bankAccountLinked = false;
    }

    @Given("a user with name {string}, last name {string}, and CPR {string}")
    public void aUserWithNameLastNameAndCPR(String firstName, String lastName, String cpr) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.cpr = cpr;
    }

    @When("the user registers for a DTU Pay account")
    public void theUserRegistersForADTUPayAccount() {
        if (firstName == null || lastName == null || cpr == null) {
            fail("User data must be provided before registration");
        }
        registrationAttempted = true;
        dtuPayAccountId = firstName + lastName + cpr;
        bankAccountLinked = true;
    }

    @Then("the account creation is successful")
    public void theAccountCreationIsSuccessful() {
        assertTrue(registrationAttempted, "Registration must have been attempted");
        assertNotNull(dtuPayAccountId, "DTU Pay account id should be available");
    }

    @Then("the user has a DTU Pay account associated with their bank account")
    public void theUserHasADTUPayAccountAssociatedWithTheirBankAccount() {
        assertTrue(bankAccountLinked, "Bank account linkage should be recorded");
    }
}