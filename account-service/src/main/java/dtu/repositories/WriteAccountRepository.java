package dtu.repositories;

import dtu.aggregate.Account;
import messaging.MessageQueue;

public class WriteAccountRepository {
  
  private EventStore eventStore;

	public WriteAccountRepository(MessageQueue bus) {
		eventStore = new EventStore(bus);
	}

	public Account getById(String accountId) {
		return Account.createFromEvents(eventStore.getEventsFor(accountId));
	}
	
	public void save(Account account) {
		eventStore.addEvents(account.getAccountId(), account.getAppliedEvents());
		account.clearAppliedEvents();
	}
  
}
