package dtu.tokens;



import lombok.Getter;
import messaging.Event;



public abstract class AccountEvent extends Event {

	private static final long serialVersionUID = -8571080289905090781L;

	private static long versionCount = 1;
	
	@Getter
	private final long version = versionCount++;
}
