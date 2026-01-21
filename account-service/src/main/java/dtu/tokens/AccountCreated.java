package dtu.tokens;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Value;

import java.io.Serial;

@Value
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class AccountCreated extends AccountEvent {

    @Serial
    private static final long serialVersionUID = -1599019626118724482L; //TODO find out what this is for and why this exact value @fadl
    private String firstName;
    private String lastName;
    private String cpr;
    private String bankAccountNum;
}
