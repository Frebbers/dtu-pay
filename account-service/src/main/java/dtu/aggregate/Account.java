package dtu.aggregate;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.jmolecules.ddd.annotation.AggregateRoot;
import org.jmolecules.ddd.annotation.Entity;

import dtu.Event.AccountCreated;
import dtu.Event.AccountDeregistered;
import dtu.Event.AccountEvent;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;

@AggregateRoot
@Entity
@Getter
public class Account {
	private UUID accountId;
	private String firstname;
	private String lastname;
	private String bankAccountNumber;
	private boolean active = false;

	@Setter(AccessLevel.NONE)
	private List<AccountEvent> appliedEvents = new ArrayList<AccountEvent>();

	private Map<Class<? extends AccountEvent>, Consumer<AccountEvent>> handlers = new HashMap<>();

	public static Account create(String firstName, String lastName, String bankAccountNumber) {
		UUID accountId = UUID.randomUUID();
		AccountCreated event = new AccountCreated(accountId, firstName, lastName, bankAccountNumber);
		var account = new Account();
		account.accountId = accountId;
		account.appliedEvents.add(event);
		return account;
	}

	public static Account createFromEvents(Stream<AccountEvent> events) {
		Account account = new Account();
		account.applyEvents(events);
		return account;
	}

	public void deregister() {
		AccountDeregistered event = new AccountDeregistered(accountId);
		this.appliedEvents.add(event);
	}


	public Account() {
		registerEventHandlers();
	}

	private void registerEventHandlers() {
		handlers.put(AccountCreated.class, e -> apply((AccountCreated) e));
		handlers.put(AccountDeregistered.class, e -> apply((AccountDeregistered) e));
	}

	public static Account rehydrate(UUID id, String firstName, String lastName, String bankAccountNumber) {
		AccountCreated created = new AccountCreated(id, firstName, lastName, bankAccountNumber);
		return Account.createFromEvents(Stream.of(created));
	}

	/* Event Handling */

	private void applyEvents(Stream<AccountEvent> events) throws Error {
		events.forEachOrdered(e -> {
			this.applyEvent(e);
		});
		if(this.accountId == null) {
			throw new Error("Account does not exist");
		}
	}

	private void applyEvent(AccountEvent e) {
		handlers.getOrDefault(e.getClass(), this::missingHandler).accept(e);
	}

	private void missingHandler(AccountEvent e) {
		throw new Error("handler for event " + e + " missing");
	}

	private void apply(AccountCreated event) {
		accountId = event.getAccountId();
		firstname = event.getFirstName();
		lastname = event.getLastName();
		bankAccountNumber = event.getBankAccountNumber();
		active = true;
	}

	private void apply(AccountDeregistered event) {
		active = false;
	}

	public void clearAppliedEvents() {
		appliedEvents.clear();
	}

}
