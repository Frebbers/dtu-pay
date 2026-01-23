package dtu.services;

import java.util.logging.Logger;

import dtu.tokens.AccountServiceTopics;
import dtu.tokens.CorrelationId;
import dtu.tokens.User;
import dtu.Exceptions.AccountAlreadyExistsException;
import dtu.Exceptions.AccountDoesNotExistsException;
import dtu.aggregate.Account;
import dtu.repositories.WriteAccountRepository;

import dtu.repositories.PaymentRequest;
import dtu.repositories.ReadAccountRepository;
import messaging.Event;
import messaging.MessageQueue;

/// @author Fadl Matar - s195846

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
    mq.addHandler(AccountServiceTopics.PAYMENT_REQUESTED, this::handleMerchantBankAccount);
    mq.addHandler(AccountServiceTopics.TOKEN_VALIDATED, this::handleCustomerBankAccount);
  }

  public String createAccount(String firstName, String lastName, String cpr, String bankAccountNumber)
      throws AccountAlreadyExistsException {
    if (readRepo.existsByCpr(cpr))
      throw new AccountAlreadyExistsException("Account with CPR number " + cpr + " already exists");
    Account account = Account.create(firstName, lastName, cpr, bankAccountNumber);
    writeRepo.save(account);
    return account.getCpr();
  }

  public void deregisterAccount(String cpr) throws AccountDoesNotExistsException {
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
      mq.publish(new Event(AccountServiceTopics.USER_REGISTERED, id, correlationId));
    } catch (AccountAlreadyExistsException ex) {
      logger.warning("Account registration failed: " + ex.getMessage());
      mq.publish(new Event(AccountServiceTopics.USER_ALREADY_REGISTERED,
          ex.getMessage(), correlationId));
    } catch (Exception exe) {
      logger.severe("Registration crashed: " + exe);
      mq.publish(new Event(AccountServiceTopics.USER_REGISTRATION_FAILED,
          exe.getMessage(), correlationId));
    }
  }

  public void handleUserDeregistration(Event e) {
    logger.info("Received user deregistration event:" + e.getTopic());
    String id = e.getArgument(0, String.class);
    CorrelationId correlationId = e.getArgument(1, CorrelationId.class);
    try {
      deregisterAccount(id);
      mq.publish(new Event(AccountServiceTopics.USER_DEREGISTERED, id, correlationId));
    } catch (Exception exe) {
      logger.warning("Deregistration failed: " + exe);
      mq.publish(new Event(AccountServiceTopics.USER_DEREGISTRATION_FAILED,
          exe.getMessage(), correlationId));
    }
  }

  public void handleMerchantBankAccount(Event e) {
    PaymentRequest payment = e.getArgument(0, PaymentRequest.class);
    CorrelationId correlationId = e.getArgument(1, CorrelationId.class);
    try {
      String bankAccountNumber = getBankAccountNumber(payment.merchantId());

      mq.publish(new Event(AccountServiceTopics.BANK_ACCOUNT_RETRIEVED,
          payment.merchantId(), bankAccountNumber, correlationId));
    } catch (AccountDoesNotExistsException ex) {
      logger.warning("Account with " + payment.merchantId() + " does not exist");
      mq.publish(new Event(AccountServiceTopics.BANK_ACCOUNT_RETRIEVAL_FAILED,
          ex.getMessage(), correlationId));
    } catch (Exception exe) {
      logger.severe("Registration crashed: " + exe);
      mq.publish(new Event(AccountServiceTopics.BANK_ACCOUNT_RETRIEVAL_FAILED,
          exe.getMessage(), correlationId));
    }
  }

  public void handleCustomerBankAccount(Event e) {
    logger.info("Received bank account number request event:" + e.getTopic());
    String cpr = e.getArgument(0, String.class);
    CorrelationId correlationId = e.getArgument(1, CorrelationId.class);
    try {
      String bankAccountNumber = getBankAccountNumber(cpr);
      mq.publish(new Event(AccountServiceTopics.BANK_ACCOUNT_RETRIEVED,
          cpr, bankAccountNumber, correlationId));
    } catch (AccountDoesNotExistsException ex) {
      logger.warning("Account with " + cpr + " does not exist");
      mq.publish(new Event(AccountServiceTopics.BANK_ACCOUNT_RETRIEVAL_FAILED,
          ex.getMessage(), correlationId));
    } catch (Exception exe) {
      logger.severe("Registration crashed: " + exe);
      mq.publish(new Event(AccountServiceTopics.BANK_ACCOUNT_RETRIEVAL_FAILED,
          exe.getMessage(), correlationId));
    }
  }
}
