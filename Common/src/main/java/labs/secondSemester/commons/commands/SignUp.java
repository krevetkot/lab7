package labs.secondSemester.commons.commands;

import labs.secondSemester.commons.exceptions.IllegalValueException;
import labs.secondSemester.commons.managers.DatabaseManager;
import labs.secondSemester.commons.network.Response;

import java.sql.SQLException;
import java.util.Scanner;

public class SignUp extends Command{
    public SignUp() {
        super("sign_up", "зарегистрироваться", false);
    }

    @Override
    public Response execute(String argument, boolean fileMode, Scanner scanner, DatabaseManager dbmanager) throws IllegalValueException, SQLException {
        if (dbmanager.findUser(getClientID().getLogin())!=-1){
            return new Response("Такой пользователь уже существует. Воспользуйтесь командой login.");
        }
        dbmanager.addUser(getClientID());
        Response response = new Response("Создан новый аккаунт.");
        response.add("Выполнен вход в аккаунт.");
        return response;
    }

}
