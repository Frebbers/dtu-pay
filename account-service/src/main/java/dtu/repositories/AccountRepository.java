package dtu.repositories;

import dtu.aggregate.Account;
import messaging.MessageQueue;

public class AccountRepository {
  
  private EventStore eventStore;

	public AccountRepository(MessageQueue bus) {
		eventStore = new EventStore(bus);
	}

	public Account getById(java.util.UUID accountId) {
		return Account.createFromEvents(eventStore.getEventsFor(accountId));
	}
	
	public void save(Account account) {
		eventStore.addEvents(account.getAccountId(), account.getAppliedEvents());
		account.clearAppliedEvents();
	}
  
}
