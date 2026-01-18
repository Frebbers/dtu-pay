package dtu.Event;

import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Value;

@Value
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)

public class AccountDeregistered extends AccountEvent {
    private static final long serialVersionUID = 1596683920706802940L;

    private UUID accountId;
    
}
