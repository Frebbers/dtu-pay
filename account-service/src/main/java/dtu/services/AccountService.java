package dtu.services;


import java.util.UUID;
import java.util.logging.Logger;

import dtu.CorrelationId;
import dtu.Exceptions.AccountAlreadyExistsException;
import dtu.Exceptions.AccountDoesNotExistException;
import dtu.aggregate.Account;
import dtu.repositories.WriteAccountRepository;
import dtu.repositories.User;
import dtu.repositories.ReadAccountRepository;
import messaging.Event;
import messaging.MessageQueue;

public class AccountService {
  private static final Logger logger = Logger.getLogger(AccountService.class.getName());
  private final MessageQueue mq;
  private final ReadAccountRepository readRepo;
  private final WriteAccountRepository writeRepo;

  public AccountService(MessageQueue mq, ReadAccountRepository readRepo, WriteAccountRepository writeRepo) {
    this.mq = mq;
    this.readRepo = readRepo;
    this.writeRepo = writeRepo;

    // Subscribe to registration events
    mq.addHandler("UserRegistrationRequested", this::handleUserRegistration);
    mq.addHandler("UserDeregisterRequested", this::handleUserDeregistration);
  }

  public UUID createAccount(String firstName, String lastName, String bankAccountNumber)
      throws AccountAlreadyExistsException {
    if (readRepo.existsByBankAccountNumber(bankAccountNumber))
      throw new AccountAlreadyExistsException("Account with bank number " + bankAccountNumber + " already exists");
    Account account = Account.create(firstName, lastName, bankAccountNumber);
    writeRepo.save(account);
    return account.getAccountId();
  }

  public void deregisterAccount(UUID accountId) throws AccountDoesNotExistException {
    if (writeRepo.getById(accountId) == null)
      throw new AccountDoesNotExistException("Account with id " + accountId + " does not exist");
    Account account = writeRepo.getById(accountId);
    account.deregister();
    writeRepo.save(account);
  }

  public void handleUserRegistration(Event e) {
    logger.info("Received user registration event:" + e.getTopic());
    var account = e.getArgument(0, User.class);
    CorrelationId correlationId = e.getArgument(1, CorrelationId.class);
    try {
      UUID id = createAccount(account.firstName(), account.lastName(), account.bankAccountNum());
      Event responseEvent = new Event("UserRegistered", new Object[] { id.toString(), correlationId });
      mq.publish(responseEvent);
    } catch (AccountAlreadyExistsException ex) {
      logger.warning("Account registration failed: " + ex.getMessage());
      Event responseEvent = new Event("UserNotRegistered", new Object[] { ex.getMessage(), correlationId });
      mq.publish(responseEvent);
    } catch (Exception exe) {
      logger.severe("Registration crashed: " + exe);
      mq.publish(new Event("UserNotRegistered", new Object[] { exe.getMessage(), correlationId }));
    }
  }

  public void handleUserDeregistration(Event e) {
    logger.info("Received user deregistration event:" + e.getTopic());
    UUID id = e.getArgument(0, UUID.class);
    CorrelationId correlationId = e.getArgument(1, CorrelationId.class);
    try {
      deregisterAccount(id);
      mq.publish(new Event("UserDeregistered", new Object[] { id.toString(), correlationId }));
    } catch (AccountDoesNotExistException ex) {
      logger.warning("Account deregistration failed: " + ex.getMessage());
      Event responseEvent = new Event("UserDeregistrationFailed", ex.getMessage(), correlationId);
      mq.publish(responseEvent);
    }
  }
}
