package dtu.pay.accountService.repositories;

import dtu.pay.accountService.EventStore;
import dtu.pay.accountService.aggregate.Account;
import messaging.MessageQueue;

public class WriteAccountRepository {
  
  private EventStore eventStore;

	public WriteAccountRepository(MessageQueue bus) {
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
