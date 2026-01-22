package dtu.tokens;

public class AccountServiceTopics {
  public static final String USER_REGISTRATION_REQUESTED = "UserRegistrationRequested";
  public static final String USER_DEREGISTERED_REQUESTED = "UserDeregistrationRequested";
  public static final String USER_REGISTERED = "UserRegistered";
  public static final String USER_DEREGISTERED= "UserDeregistered";
  public static final String TOKEN_VALIDATED = "TokenValidated";
  public static final String PAYMENT_REQUESTED = "PaymentRequested";
  public static final String BANK_ACCOUNT_RETRIEVED = "BankAccountRetrieved";
  public static final String BANK_ACCOUNT_RETRIEVAL_FAILED = "BankAccountRetrievalFailed";
  public static final String USER_DEREGISTRATION_FAILED = "UserDeregistrationFailed";
  public static final String USER_DOES_NOT_EXIST = "UserDoesNotExist"; 
  public static final String USER_ALREADY_REGISTERED = "UserAlreadyRegistered";
  public static final String USER_REGISTRATION_FAILED = "UserRegistrationFailed";
}
