package labs.secondSemester.commons.network;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
@Getter
public class ClientIdentification implements Serializable {
    private final String login;
    private final String password;
    @Setter
    private boolean isAuthorized;


    public ClientIdentification(String login, String password){
        this.login = login;
        this.password = password;
        isAuthorized = false;
    }
}