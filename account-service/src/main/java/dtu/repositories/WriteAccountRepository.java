package dtu.repositories;

import dtu.aggregate.Account;
import messaging.MessageQueue;

public class WriteAccountRepository {

	private EventStore eventStore;

	public WriteAccountRepository(MessageQueue bus) {
		eventStore = new EventStore(bus);
	}

	public Account getById(String cpr) {
		return Account.createFromEvents(eventStore.getEventsFor(cpr));
	}

	public void save(Account account) {
		eventStore.addEvents(account.getCpr(), account.getAppliedEvents());
		account.clearAppliedEvents();
	}

}
