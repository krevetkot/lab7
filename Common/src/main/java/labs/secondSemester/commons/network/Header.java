package labs.secondSemester.commons.network;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.io.Serializable;

@Getter
@AllArgsConstructor
public class Header implements Serializable {
    private final int count;
    private final int number;

}