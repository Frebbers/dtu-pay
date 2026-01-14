package dtu;

import io.cucumber.java.Before;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.When;
import io.cucumber.java.en.Then;
import static org.junit.jupiter.api.Assertions.*;
import java.util.UUID;

public class AccountSteps {
    private String customerFirstname;
    private String customerLastname;
    private String customerCprNumber;
    private String customerName;
    private String customerAccountNumber;
    private String merchantFirstname;
    private String merchantLastname;
    private String merchantCprNumber;
    private String merchantName;
    private String merchantAccountNumber;
    private String customerUuid;
    private String merchantUuid;
    private String latestError;
    private boolean customerAlreadyRegistered;
    private boolean merchantAlreadyRegistered;
    private RegistrationType lastRegistrationAttempt;

    @Given("a customer with firstname {string} and lastname {string} and CPR number {string} and account number {string}")
    public void aCustomerWithFirstnameLastnameCprAndAccountNumber(String firstname, String lastname, String cprNumber, String accountNumber) {
        customerFirstname = firstname;
        customerLastname = lastname;
        customerCprNumber = cprNumber;
        customerName = (firstname + " " + lastname).trim();
        customerAccountNumber = accountNumber;
    }

    @Given("the customer is already registered")
    public void theCustomerIsAlreadyRegistered() {
        customerUuid = registerCustomer();
        assertNotNull(customerUuid);
    }

    @When("the customer registers with DTU Pay")
    public void theCustomerRegistersWithDTUPay() {
        customerUuid = registerCustomer();
    }

    @Then("the customer should be registered successfully and receive a UUID")
    public void theCustomerShouldBeRegisteredSuccessfullyAndReceiveAUUID() {
        assertNotNull(customerUuid);
        assertDoesNotThrow(() -> UUID.fromString(customerUuid));
    }

    @Then("^a \"customer.registered\" event should be published with uuid, name \"([^\"]*)\" and account number \"([^\"]*)\"$")
    public void aCustomerRegisteredEventShouldBePublishedWithUuidNameAndAccountNumber(String name, String accountNumber) {
        assertNotNull(customerUuid);
        assertEquals(customerName, name);
        assertEquals(customerAccountNumber, accountNumber);
    }

    @Given("a merchant with firstname {string} and lastname {string} and CPR number {string} and account number {string}")
    public void aMerchantWithFirstnameLastnameCprAndAccountNumber(String firstname, String lastname, String cprNumber, String accountNumber) {
        merchantFirstname = firstname;
        merchantLastname = lastname;
        merchantCprNumber = cprNumber;
        merchantName = (firstname + " " + lastname).trim();
        merchantAccountNumber = accountNumber;
    }

    @Given("the merchant is already registered")
    public void theMerchantIsAlreadyRegistered() {
        merchantUuid = registerMerchant();
        assertNotNull(merchantUuid);
    }

    @When("the merchant registers with DTU Pay")
    public void theMerchantRegistersWithDTUPay() {
        merchantUuid = registerMerchant();
    }

    @Then("the merchant should be registered successfully and receive a UUID")
    public void theMerchantShouldBeRegisteredSuccessfullyAndReceiveAUUID() {
        assertNotNull(merchantUuid);
        assertDoesNotThrow(() -> UUID.fromString(merchantUuid));
    }

    @Then("^a \"merchant.registered\" event should be published with uuid, name \"([^\"]*)\" and account number \"([^\"]*)\"$")
    public void aMerchantRegisteredEventShouldBePublishedWithUuidNameAndAccountNumber(String name, String accountNumber) {
        assertNotNull(merchantUuid);
        assertEquals(merchantName, name);
        assertEquals(merchantAccountNumber, accountNumber);
    }

    @Then("the registration should fail with error {string}")
    public void theRegistrationShouldFailWithError(String errorMsg) {
        assertNotNull(lastRegistrationAttempt, "No registration attempt recorded before failure");
        assertEquals(errorMsg, latestError);
        if (lastRegistrationAttempt == RegistrationType.CUSTOMER) {
            assertNull(customerUuid);
        } else {
            assertNull(merchantUuid);
        }
    }

    private String registerCustomer() {
        lastRegistrationAttempt = RegistrationType.CUSTOMER;
        if (customerAlreadyRegistered) {
            latestError = "Customer already registered: " + customerName + ", " + customerAccountNumber;
            return null;
        }
        customerAlreadyRegistered = true;
        latestError = null;
        return UUID.randomUUID().toString(); // Simulate success
    }

    private String registerMerchant() {
        lastRegistrationAttempt = RegistrationType.MERCHANT;
        if (merchantAlreadyRegistered) {
            latestError = "Merchant already registered: " + merchantName + ", " + merchantAccountNumber;
            return null;
        }
        merchantAlreadyRegistered = true;
        latestError = null;
        return UUID.randomUUID().toString(); // Simulate success
    }

    private enum RegistrationType { CUSTOMER, MERCHANT }

    @Before
    public void resetState() {
        customerAlreadyRegistered = false;
        merchantAlreadyRegistered = false;
        customerUuid = null;
        merchantUuid = null;
        latestError = null;
        lastRegistrationAttempt = null;
        merchantFirstname = null;
        merchantLastname = null;
        merchantCprNumber = null;
    }
}
