package dtu.repositories;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import org.jmolecules.ddd.annotation.Repository;

import dtu.Event.AccountCreated;
import dtu.aggregate.Account;
import messaging.Event;
import messaging.MessageQueue;


@Repository
public class ReadAccountRepository {

  private final Map<UUID, String> bankAccountNumbers = new HashMap<>();

  private final Map<UUID, Account> accounts = new HashMap<>();
  private final Map<UUID, Account> customers = new ConcurrentHashMap<>(); // key: uuid, value: Customer
  private final Map<UUID, Account> merchants = new ConcurrentHashMap<>(); // key: uuid, value: Merchant

	public ReadAccountRepository(MessageQueue eventQueue) {
		eventQueue.addHandler("CustomerRegistered", this::handleCustomerCreatedEvent);
    eventQueue.addHandler("MerchantRegistered", this::handleMerchantCreatedEvent);
	}

  public void handleCustomerCreatedEvent(Event event) {
    AccountCreated accountCreatedEvent = event.getArgument(0, AccountCreated.class);
    Account account = new Account();
    customers.put(accountCreatedEvent.getAccountId(), account);
  }

  public void handleMerchantCreatedEvent(Event event) {
    AccountCreated accountCreatedEvent = event.getArgument(0, AccountCreated.class);
    Account account = Account.rehydrate(accountCreatedEvent.getAccountId(), 
      accountCreatedEvent.getFirstName(), 
      accountCreatedEvent.getLastName(), 
      accountCreatedEvent.getCpr(), 
      accountCreatedEvent.getBankAccountNumber());
    merchants.put(accountCreatedEvent.getAccountId(), account);
  }

  public Account getCustomerById(UUID id) {
    return customers.get(id);
  }

  public Account getMerchantById(UUID id) {
    return merchants.get(id);
  }

  public boolean existsByBankAccountNumber(String bankAccountNumber) {
    return bankAccountNumbers.values().contains(bankAccountNumber);
  }

  // public void handleAccountCreatedEvent(messaging.Event event) {
  //   AccountCreated accountCreatedEvent = event.getArgument(0, AccountCreated.class);
  //   bankAccountNumbers.put(accountCreatedEvent.getAccountId(), accountCreatedEvent.getBankAccountNumber());
  // }
}
