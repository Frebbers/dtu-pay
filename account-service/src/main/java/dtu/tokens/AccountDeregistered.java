package dtu.tokens;


import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Value;

import java.io.Serial;

@Value
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)

public class AccountDeregistered extends AccountEvent {
    @Serial
    private static final long serialVersionUID = 1596683920706802940L; //what is this for?

    private String cpr;
}
