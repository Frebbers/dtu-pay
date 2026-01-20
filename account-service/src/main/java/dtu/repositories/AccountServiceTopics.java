package dtu.repositories;

public class AccountServiceTopics {
  public static final String USER_REGISTRATION_REQUESTED = "UserRegistrationRequested";
  public static final String USER_DEREGISTERED_REQUESTED = "UserDeregistrationRequested";
  public static final String USER_REGISTERED = "UserRegistered";
  public static final String USER_DEREGISTERED= "UserDeregistered";
  public static final String BANK_ACCOUNT_REQUESTED = "accounts.commands.GetBankAccount";
  public static final String BANK_ACCOUNT_RETRIEVED = "accounts.events.BankAccountRetrieved";
  public static final String BANK_ACCOUNT_RETRIEVAL_FAILED = "BankAccountRetrievalFailed";
  public static final String ACCOUNT_CREATED = "AccountCreated";
  public static final String ACCOUNT_DEREGISTERED = "AccountDeregistered";
  public static final String USER_DEREGISTRATION_FAILED = "UserDeregistrationFailed";
  public static final String USER_DOES_NOT_EXIST = "UserDoesNotExist"; 
  public static final String USER_ALREADY_REGISTERED = "UserAlreadyRegistered";
  public static final String USER_REGISTRATION_FAILED = "UserRegistrationFailed";
}
