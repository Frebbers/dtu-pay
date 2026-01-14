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
    eventQueue.addHandler("AccountCreated", this::handleAccountCreatedEvent);
  }

  public void handleAccountCreatedEvent(Event event) {
    UUID id = UUID.fromString(event.getArgument(0, String.class));
    String firstName = event.getArgument(1, String.class);
    String lastName = event.getArgument(2, String.class);
    String cpr = event.getArgument(3, String.class);
    String bankAcc = event.getArgument(4, String.class);

    Account account = Account.rehydrate(id, firstName, lastName, bankAcc);
   
    accounts.put(id, account); 
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
  // AccountCreated accountCreatedEvent = event.getArgument(0,
  // AccountCreated.class);
  // bankAccountNumbers.put(accountCreatedEvent.getAccountId(),
  // accountCreatedEvent.getBankAccountNumber());
  // }
}
