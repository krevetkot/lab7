package labs.secondSemester.commons.commands;

import labs.secondSemester.commons.exceptions.AccessDeniedException;
import labs.secondSemester.commons.exceptions.IllegalValueException;
import labs.secondSemester.commons.managers.CollectionManager;
import labs.secondSemester.commons.managers.DatabaseManager;
import labs.secondSemester.commons.network.Response;
import labs.secondSemester.commons.objects.Dragon;

import java.util.Scanner;

/**
 * Команда show: выводит в стандартный поток вывода все элементы коллекции в строковом представлении.
 *
 * @author Kseniya
 */
public class Show extends Command {
    public Show() {
        super("show", "вывести в стандартный поток вывода все элементы коллекции в строковом представлении", false);
    }

    @Override
    public Response execute(String argument, boolean fileMode, Scanner scanner, DatabaseManager dbmanager) throws AccessDeniedException {
        if (CollectionManager.getCollectionForReading().isEmpty()) {
            return new Response("Коллекция пуста.");
        }
        Response response = new Response();
        for (Dragon element : CollectionManager.getCollectionForReading()) {
            try {
                response.add(element.toString());
            } catch (Exception e) {
                return new Response(e.getMessage());
            }
        }
        return response;
    }
}
