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
import dtu.Exceptions.AccountDoesNotExistsException;
import messaging.Event;
import messaging.MessageQueue;

@Repository
public class ReadAccountRepository {

  private final Map<String, User> accounts = new ConcurrentHashMap<>();

  public ReadAccountRepository(MessageQueue eventQueue) {
    eventQueue.addHandler("AccountCreated", this::handleAccountCreatedEvent);
    eventQueue.addHandler("AccountDeregistered", this::handleAccountDeregisteredEvent);
  }

  public void handleAccountCreatedEvent(Event e) {
    AccountCreated event = e.getArgument(0, AccountCreated.class);

    accounts.put(event.getCpr(), new User(
        event.getFirstName(),
        event.getLastName(),
        event.getCpr(),
        event.getBankAccountNum()));
    System.out.println("Accounts in read repo: " + accounts);
    System.out.println("Account size: " + accounts.size());
  }

  public void handleAccountDeregisteredEvent(Event e) {
    AccountDeregistered event = e.getArgument(0, AccountDeregistered.class);
    accounts.remove(event.getCpr());
    System.out.println("Accounts in read repo: " + accounts);
    System.out.println("Account size: " + accounts.size());
  }

  public String getCprByBankAccount(String bankAccountNum) {
    System.out.println("Accounts in read repo: " + accounts);
    System.out.println("Account size: " + accounts.size());
    return accounts.entrySet().stream()
        .filter(e -> e.getValue().bankAccountNum().equals(bankAccountNum))
        .map(Map.Entry::getKey)
        .findFirst()
        .orElse(null);
  }

  public String getBankAccount(String cpr) throws AccountDoesNotExistsException {
    User u = accounts.get(cpr);
    if(u == null) throw new AccountDoesNotExistsException("Account with CPR" + cpr + " does not exist");
    System.out.println("Accounts in read repo: " + accounts);
    System.out.println("Account size: " + accounts.size());
    return u.bankAccountNum();
  }

  public boolean existsByBankAccountNumber(String bankAccountNum) {
    return accounts.values().stream().anyMatch(account -> account.bankAccountNum().equals(bankAccountNum));
  }

  public boolean existsByCpr(String cpr){
    return accounts.containsKey(cpr);
  }
}
