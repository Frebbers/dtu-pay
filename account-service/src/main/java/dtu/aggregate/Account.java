package dtu.aggregate;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.stream.Stream;

import org.jmolecules.ddd.annotation.AggregateRoot;
import org.jmolecules.ddd.annotation.Entity;

import dtu.tokens.AccountCreated;
import dtu.tokens.AccountDeregistered;
import dtu.tokens.AccountEvent;
import dtu.Exceptions.AccountDoesNotExistsException;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;

@AggregateRoot
@Entity
@Getter

/// @author Christian Hyltoft - s215816

public class Account {
	private String firstname;
	private String lastname;
	private String cpr;
	private String bankAccountNum;
	private boolean active = false;

	@Setter(AccessLevel.NONE)
	private List<AccountEvent> appliedEvents = new ArrayList<AccountEvent>();

	private Map<Class<? extends AccountEvent>, Consumer<AccountEvent>> handlers = new HashMap<>();

	public static Account create(String firstName, String lastName, String cpr, String bankAccountNum) {
		AccountCreated event = new AccountCreated(firstName, lastName, cpr, bankAccountNum);
		var account = new Account();
		account.cpr = cpr;
		account.appliedEvents.add(event);
		return account;
	}

	public static Account createFromEvents(Stream<AccountEvent> events) throws AccountDoesNotExistsException {
		Account account = new Account();
		account.applyEvents(events);
		return account;
	}

	public void deregister() {
		AccountDeregistered event = new AccountDeregistered(cpr);
		this.appliedEvents.add(event);
	}

	public Account() {
		registerEventHandlers();
	}

	private void registerEventHandlers() {
		handlers.put(AccountCreated.class, e -> apply((AccountCreated) e));
		handlers.put(AccountDeregistered.class, e -> apply((AccountDeregistered) e));
	}

	/* Event Handling */

	private void applyEvents(Stream<AccountEvent> events) throws AccountDoesNotExistsException {
		events.forEachOrdered(e -> {
			this.applyEvent(e);
		});
	}

	private void applyEvent(AccountEvent e) {
		handlers.getOrDefault(e.getClass(), this::missingHandler).accept(e);
	}

	private void missingHandler(AccountEvent e) {
		throw new Error("handler for event " + e + " missing");
	}

	private void apply(AccountCreated event) {
		firstname = event.getFirstName();
		lastname = event.getLastName();
		cpr = event.getCpr();
		bankAccountNum = event.getBankAccountNum();
		active = true;
	}

	private void apply(AccountDeregistered event) {
		active = false;
	}

	public void clearAppliedEvents() {
		appliedEvents.clear();
	}

}
