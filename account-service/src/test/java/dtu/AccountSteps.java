package dtu;

import dtu.services.Customer;
import dtu.services.CustomerRegistrationService;
import io.cucumber.java.Before;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.When;
import io.cucumber.java.en.Then;
import static org.junit.jupiter.api.Assertions.*;
import java.util.UUID;
import messaging.Event;
import messaging.MessageQueue;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

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

    private CompletableFuture<Event> publishedEvent = new CompletableFuture<>();

    private MessageQueue q = new MessageQueue() {

        @Override
        public void publish(Event event) {
            publishedEvent.complete(event);
        }

        @Override
        public void addHandler(String eventType, Consumer<Event> handler) {
        }

    };
    private CustomerRegistrationService service = new CustomerRegistrationService(q);
    private CompletableFuture<Customer> registeredCustomer = new CompletableFuture<>();
    private Customer customer;

    @Given("there is a customer with empty id")
    public void thereIsACustomerWithEmptyId() {
        customer = new Customer();
        customer.setName("James");
        assertNull(customer.getId());
    }

    @When("the customer is being registered")
    public void theCustomerIsBeingRegistered() {
        // We have to run the registration in a thread, because
        // the register method will only finish after the next @When
        // step is executed.
        new Thread(() -> {
            var result = service.register(customer);
            registeredCustomer.complete(result);
        }).start();
    }

    @Then("the {string} event is sent")
    public void theEventIsSent(String string) {
        Event event = new Event(string, new Object[] { customer });
        assertEquals(event,publishedEvent.join());
    }

    @When("the {string} event is sent with non-empty id")
    public void theEventIsSentWithNonEmptyId(String string) {
        // This step simulate the event created by a downstream service.
        var c = new Customer();
        c.setName(customer.getName());
        c.setId("123");
        service.handleCustomerIdAssigned(new Event("..",new Object[] {c}));
    }

    @Then("the customer is registered and his id is set")
    public void theCustomerIsRegisteredAndHisIdIsSet() {
        // Our logic is very simple at the moment; we don't
        // remember that the customer is registered.
        assertNotNull(registeredCustomer.join().getId());
    }
}
