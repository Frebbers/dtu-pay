package dtu;

import static org.mockito.Mockito.mock;

import dtu.repositories.ReadAccountRepository;
import dtu.repositories.WriteAccountRepository;
import dtu.repositories.User;
import dtu.services.AccountService;
import messaging.Event;
import messaging.MessageQueue;
import messaging.implementations.MessageQueueSync;

public class SharedContext {
  public MessageQueue queue;
  public MessageQueue queueExternal;
  public WriteAccountRepository writeRepo;
  public ReadAccountRepository readRepo;
  public AccountService accountService;
  public User account;
  public String createdCpr;
  public String bankAccount;
  public CorrelationId correlationId;
  public Event deregistrationRequestedEvent;
  public Event bankAccountRequestedEvent;
  public Event bankAccountRetrievalFailedEvent;
  public Event deregistrationFailedEvent;

  public SharedContext() {
   
    queue = new MessageQueueSync();
    queueExternal = mock(MessageQueue.class);

    writeRepo = new WriteAccountRepository(queue);
    readRepo = new ReadAccountRepository(queue);

    accountService = new AccountService(queueExternal, readRepo, writeRepo);
  
  }
}
