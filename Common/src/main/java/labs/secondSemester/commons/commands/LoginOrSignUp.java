package labs.secondSemester.commons.commands;

import labs.secondSemester.commons.exceptions.IllegalValueException;
import labs.secondSemester.commons.managers.CommandManager;
import labs.secondSemester.commons.managers.DatabaseManager;
import labs.secondSemester.commons.network.Response;

import java.sql.SQLException;
import java.util.Scanner;

public class LoginOrSignUp extends Command{
    public LoginOrSignUp() {
        super("login_or_sign_up", "вспомогательная команда для входа в аккаунт или регистрации", false);
    }

    @Override
    public Response execute(String argument, boolean fileMode, Scanner scanner, DatabaseManager dbmanager) throws IllegalValueException, SQLException {
        Response response = new Response();
        if (dbmanager.findUser(getClientID().getLogin())==-1){
            dbmanager.addUser(getClientID());
            response.add("yes");
            response.add("Создан новый аккаунт и выполнен вход.");
            return response;
        }
        if (dbmanager.checkPassword(getClientID())){
            response.add("yes");
            response.add("Выполнен вход в аккаунт.");
            return response;
        }
        response.add("no");
        response.add("Пароль не соответствует паролю, указанному при регистрации.");
        return response;
    }
}
