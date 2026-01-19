package dtu.services;

import java.util.logging.Logger;

import dtu.CorrelationId;
import dtu.Exceptions.AccountAlreadyExistsException;
import dtu.Exceptions.AccountDoesNotExistsException;
import dtu.aggregate.Account;
import dtu.repositories.WriteAccountRepository;
import dtu.repositories.User;
import dtu.repositories.AccountServiceTopics;
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
    mq.addHandler(AccountServiceTopics.USER_REGISTRATION_REQUESTED, this::handleUserRegistration);
    mq.addHandler(AccountServiceTopics.USER_DEREGISTERED_REQUESTED, this::handleUserDeregistration);
    mq.addHandler(AccountServiceTopics.BANK_ACCOUNT_REQUESTED, this::handleBankAccountNumberRequested);
  }

  public String createAccount(String firstName, String lastName, String cpr, String bankAccountNumber)
      throws AccountAlreadyExistsException {
    if (readRepo.existsByBankAccountNumber(bankAccountNumber))
      throw new AccountAlreadyExistsException("Account with bank number " + bankAccountNumber + " already exists");
    Account account = Account.create(firstName, lastName, cpr, bankAccountNumber);
    writeRepo.save(account);
    return account.getCpr();
  }

  public void deregisterAccount(String cpr) throws AccountDoesNotExistsException {
    if (writeRepo.getById(cpr) == null)
      throw new AccountDoesNotExistsException("Account with cpr " + cpr + " does not exist");
    Account account = writeRepo.getById(cpr);
    account.deregister();
    writeRepo.save(account);
  }

  public String getBankAccountNumber(String cpr) throws AccountDoesNotExistsException {
    return readRepo.getBankAccount(cpr);
  }

  public void handleUserRegistration(Event e) {
    logger.info("Received user registration event:" + e.getTopic());
    var account = e.getArgument(0, User.class);
    CorrelationId correlationId = e.getArgument(1, CorrelationId.class);
    try {
      String id = createAccount(account.firstName(), account.lastName(), account.cprNumber(), account.bankAccountNum());
      Event responseEvent = new Event(AccountServiceTopics.USER_REGISTERED, new Object[] { id, correlationId });
      mq.publish(responseEvent);
    } catch (AccountAlreadyExistsException ex) {
      logger.warning("Account registration failed: " + ex.getMessage());
      Event responseEvent = new Event(AccountServiceTopics.USER_REGISTRATION_FAILED,
          new Object[] { ex.getMessage(), correlationId });
      mq.publish(responseEvent);
    } catch (Exception exe) {
      logger.severe("Registration crashed: " + exe);
      mq.publish(
          new Event(AccountServiceTopics.USER_REGISTRATION_FAILED, new Object[] { exe.getMessage(), correlationId }));
    }
  }

  public void handleUserDeregistration(Event e) {
    logger.info("Received user deregistration event:" + e.getTopic());
    String id = e.getArgument(0, String.class);
    CorrelationId correlationId = e.getArgument(1, CorrelationId.class);
    try {
      deregisterAccount(id);
      mq.publish(new Event(AccountServiceTopics.USER_DEREGISTERED, new Object[] { id, correlationId }));
    } catch (AccountDoesNotExistsException ex) {
      logger.warning("Account deregistration failed: " + ex.getMessage());
      Event responseEvent = new Event(AccountServiceTopics.USER_DEREGISTRATION_FAILED, ex.getMessage(), correlationId);
      mq.publish(responseEvent);
    }
  }

  public void handleBankAccountNumberRequested(Event e) {
    logger.info("Recieved bank account number request event:" + e.getTopic());
    String cpr = e.getArgument(0, String.class);
    CorrelationId correlationId = e.getArgument(1, CorrelationId.class);
    try {
      String bankAccountNumber = getBankAccountNumber(cpr);
      mq.publish(new Event(AccountServiceTopics.BANK_ACCOUNT_RETRIEVED, bankAccountNumber, correlationId));
    } catch (AccountDoesNotExistsException ex) {
      logger.warning("Account with " + cpr + " does not exist");
      Event responseEvent = new Event(AccountServiceTopics.BANK_ACCOUNT_RETRIEVAL_FAILED, ex.getMessage(),
          correlationId);
      mq.publish(responseEvent);
    }
  }
}
