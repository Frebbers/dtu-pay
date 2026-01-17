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
import dtu.Event.AccountDeregistered;

import messaging.Event;
import messaging.MessageQueue;

@Repository
public class ReadAccountRepository {

  private final Map<UUID, User> accounts = new ConcurrentHashMap<>();

  public ReadAccountRepository(MessageQueue eventQueue) {
    eventQueue.addHandler("AccountCreated", this::handleAccountCreatedEvent);
    eventQueue.addHandler("AccountDeregistered", this::handleAccountDeregisteredEvent);
  }

  public void handleAccountCreatedEvent(Event e) {
    AccountCreated event = e.getArgument(0, AccountCreated.class);

    accounts.put(event.getAccountId(), new User(
        event.getFirstName(),
        event.getLastName(),
        event.getBankAccountNum()));
  }

  public void handleAccountDeregisteredEvent(Event e) {
    AccountDeregistered event = e.getArgument(0, AccountDeregistered.class);
    accounts.remove(event.getAccountId());
    System.out.println(accounts.size());
  }

  public UUID getAccountIdByBankAccountNumber(String bankAccountNum) {
    System.out.println("Accounts in read repo: " + accounts);
    System.out.println("Account size: " + accounts.size());
    return accounts.entrySet().stream()
        .filter(e -> e.getValue().bankAccountNum().equals(bankAccountNum))
        .map(Map.Entry::getKey)
        .findFirst()
        .orElse(null);
  }

  public boolean existsByBankAccountNumber(String bankAccountNum) {
    return accounts.values().stream().anyMatch(account -> account.bankAccountNum().equals(bankAccountNum));
  }

  public void deleteAll() {
    // TODO Auto-generated method stub
    throw new UnsupportedOperationException("Unimplemented method 'deleteAll'");
  }

}
