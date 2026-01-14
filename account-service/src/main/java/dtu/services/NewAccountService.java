package dtu.services;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;

import dtu.aggregate.Account;
import dtu.repositories.AccountRepository;
import dtu.repositories.ReadAccountRepository;
import dtu.services.AccountService.Customer;
import dtu.services.AccountService.Merchant;
import messaging.Event;
import messaging.implementations.RabbitMqQueue;


public class NewAccountService {
  private static final Logger logger = Logger.getLogger(AccountService.class.getName());
  private final RabbitMqQueue mq;
  private final ReadAccountRepository readRepo;
  private final AccountRepository writeRepo;

  public NewAccountService(RabbitMqQueue mq, ReadAccountRepository readRepo, AccountRepository writeRepo) {
    this.mq = mq;
    this.readRepo = readRepo;
    this.writeRepo = writeRepo;

    // Subscribe to registration events
    mq.addHandler("CustomerRegistrationRequested", this::handleCustomerRegistration);
    mq.addHandler("MerchantRegistrationRequested", this::handleMerchantRegistration);
  }

  public UUID createAccount(String firstName, String lastName, String cpr, String accountNumber) {
    if(readRepo.existsByBankAccountNumber(accountNumber)) throw new IllegalArgumentException("Account already exists");
    Account account = Account.create(firstName, lastName, cpr, accountNumber);
    writeRepo.save(account);
    return account.getAccountId();
  }

  private void handleCustomerRegistration(Event e) {
    logger.info("Received customer registration event:" + e.getTopic());
    var account = e.getArgument(0, Account.class);
    UUID id = createAccount(account.getFirstname(),account.getLastname(), account.getCpr(), account.getBankAccountNumber()); 
    Event event = new Event("MerchantRegistered", id);
    mq.publish(event);  
  } 

  private void handleMerchantRegistration(Event e) {
    logger.info("Received merchant registration event:" + e.getTopic());
    // Implementation goes here
    var account = e.getArgument(0, Account.class);
    UUID id = createAccount(account.getFirstname(),account.getLastname(), account.getCpr(), account.getBankAccountNumber()); 
    Event event = new Event("CustomerRegistered", id);
    mq.publish(event); 
  }


}
