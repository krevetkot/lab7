package labs.secondSemester.server;

import labs.secondSemester.commons.commands.Command;
import labs.secondSemester.commons.exceptions.IllegalValueException;
import labs.secondSemester.commons.managers.DatabaseManager;
import labs.secondSemester.commons.network.Response;

import java.util.Scanner;

/**
 * Класс, реализующий запуск приложения.
 *
 * @author Kseniya
 */
public class RuntimeManager {


    /**
     * Обрабатывает полученную команду, чтобы вызвать ее выполнение.
     *
     * @throws IllegalValueException - ошибка недопустимых данных, команду невозможно выполнить
     */

    public Response commandProcessing(Command command, boolean fileMode, Scanner scanner, DatabaseManager dbmanager) throws IllegalValueException, ArrayIndexOutOfBoundsException, NumberFormatException {
        String argument = command.getStringArgument();
        if (command.isArgs()) {
            try {
                return command.execute(argument, fileMode, scanner, dbmanager);
            } catch (Exception e) {
                return new Response(e.getMessage());
            }
        } else {
            try {
                return command.execute(null, fileMode, scanner, dbmanager);
            } catch (Exception e) {
                return new Response(e.getMessage());
            }
        }
    }
}
