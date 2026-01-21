package dtu.repositories;

import java.util.stream.Collectors;

import dtu.Exceptions.AccountDoesNotExistsException;
import dtu.aggregate.Account;
import messaging.MessageQueue;

public class WriteAccountRepository {

	private EventStore eventStore;

	public WriteAccountRepository(MessageQueue bus) {
		eventStore = new EventStore(bus);
	}

	public Account getById(String cpr) throws AccountDoesNotExistsException {
		var events = eventStore.getEventsFor(cpr).collect(Collectors.toList());
		if (events.isEmpty()) {
			throw new AccountDoesNotExistsException("Account with CPR number " + cpr + " does not exist");
		}
		return Account.createFromEvents(events.stream());
	}

	public void save(Account account) {
		eventStore.addEvents(account.getCpr(), account.getAppliedEvents());
		account.clearAppliedEvents();
	}

}
