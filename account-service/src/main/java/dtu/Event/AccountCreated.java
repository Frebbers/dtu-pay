package dtu.Event;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Value;

@Value
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class AccountCreated extends AccountEvent {

    private static final long serialVersionUID = -1599019626118724482L;
    private String firstName;
    private String lastName;
    private String cpr;
    private String bankAccountNum;
}
