package dtu.pay;

import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;

/*
	Replace the class with your own step definition
   	classes.
 */
public class ExampleSteps {

	@Given("that the test is setup")
	public void thatTheTestIsSetup() {
		// Write code here that turns the phrase above into concrete actions
		// A given step executes commands such that the system gets into a state
		// that is described the step definition.
		// E.g. "Given a user is registered with the bank with balance 1000"
		// Would imply to create a user and then register the user, so that after
		// executing the code the statement becomes true.
	}

	@When("an action is performed")
	public void anActionIsPerformed() {
		// Write code here that turns the phrase above into concrete actions
		// Here the actions to test are being performed.
		// E.g. "When the user withdraws 100 kr from the bank" would then perform the actions
		// that are needed to withdraw the amount of money from the bank.
	}

	@Then("this condition is true")
	public void thisConditionIsTrue() {
		// Write code here that turns the phrase above into concrete actions
		// Here we are using JUnits assert statements to check if the desired outcome
		// has happened.
		// E.g., "Then the balance for that user is 900 kr".
		// This would then use, e.g., assertEquals to check that the actual bank balance
		// of that user is the expected balance.
	}
}
