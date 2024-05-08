package labs.secondSemester.commons.commands;

import labs.secondSemester.commons.exceptions.IllegalValueException;
import labs.secondSemester.commons.managers.DatabaseManager;
import labs.secondSemester.commons.network.Response;

import java.sql.SQLException;
import java.util.Scanner;

public class Login extends Command{
    public Login() {
        super("login", "выполнить вход в аккаунт", false);
    }

    @Override
    public Response execute(String argument, boolean fileMode, Scanner scanner, DatabaseManager dbmanager) throws IllegalValueException, SQLException {
        Response response = new Response();
        if (dbmanager.checkPassword(getClientID())){
            response.add("yes");
            response.add("Выполнен вход в аккаунт.");
            return response;
        }
        response.add("Пароль не соответствует паролю, указанному при регистрации.");
        return response;
    }
}
