package dtu.services;

import java.util.UUID;
import java.util.logging.Logger;

import dtu.aggregate.Account;
import dtu.repositories.AccountRepository;
import dtu.repositories.ReadAccountRepository;

import messaging.Event;
import messaging.implementations.RabbitMqQueue;


public class AccountService {
  private static final Logger logger = Logger.getLogger(AccountService.class.getName());
  private final RabbitMqQueue mq;
  private final ReadAccountRepository readRepo;
  private final AccountRepository writeRepo;

  public AccountService(RabbitMqQueue mq, ReadAccountRepository readRepo, AccountRepository writeRepo) {
    this.mq = mq;
    this.readRepo = readRepo;
    this.writeRepo = writeRepo;

    // Subscribe to registration events
    mq.addHandler("UserRegistrationRequested", this::handleUserRegistration);
    mq.addHandler("UserDerigisterRequest", this::handleUserDerigisteration);
  }

  public UUID createAccount(String firstName, String lastName, String accountNumber) {
    if(readRepo.existsByBankAccountNumber(accountNumber)) throw new IllegalArgumentException("Account already exists");
    Account account = Account.create(firstName, lastName, accountNumber);
    writeRepo.save(account);
    return account.getAccountId();
  }

  private void handleUserRegistration(Event e) {
    logger.info("Received user registration event:" + e.getTopic());
    var account = e.getArgument(0, Account.class);
    UUID id = createAccount(account.getFirstname(), account.getLastname(), account.getBankAccountNumber()); 
    Event responseEvent = new Event("UserRegistered", id);
    Event savingToRepoEvent = new Event("UserAccountCreated",
      id.toString(),
      account.getFirstname(),
      account.getLastname(),
      account.getBankAccountNumber()
  );
    mq.publish(responseEvent);
    mq.publish(savingToRepoEvent);  
  } 

  public void handleUserDerigisteration(Event e) {
    logger.info("Received user derigistration event:" + e.getTopic());
    UUID id = e.getArgument(0, UUID.class);
    //writeRepo.deleteById(id);
  }
  
}
