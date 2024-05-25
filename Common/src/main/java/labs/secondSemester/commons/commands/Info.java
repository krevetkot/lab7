package labs.secondSemester.commons.commands;

import labs.secondSemester.commons.exceptions.AccessDeniedException;
import labs.secondSemester.commons.exceptions.IllegalValueException;
import labs.secondSemester.commons.managers.CollectionManager;
import labs.secondSemester.commons.managers.DatabaseManager;
import labs.secondSemester.commons.network.Response;

import java.util.Scanner;

/**
 * Команда info: выводит в стандартный поток вывода информацию о коллекции (тип, дата инициализации, количество элементов и т.д.).
 *
 * @author Kseniya
 */
public class Info extends Command {

    public Info() {
        super("info", "вывести в стандартный поток вывода информацию о коллекции (тип, дата инициализации, количество элементов и т.д.)", false);
    }

    @Override
    public Response execute(String argument, boolean fileMode, Scanner scanner, DatabaseManager dbmanager) throws AccessDeniedException {
        if (CollectionManager.getCollectionForReading().isEmpty()) {
            return new Response("Коллекция пока что пуста. Тип коллекции: " + CollectionManager.getCollectionForReading().getClass());
        } else {
            Response response = new Response();
            response.add("Тип коллекции: " + CollectionManager.getCollectionForReading().getClass());
            response.add("Количество элементов: " + CollectionManager.getCollectionForReading().size());
            response.add("Дата инициализации: " + CollectionManager.getCollectionForReading().get(0).getCreationDate());
            return response;
        }
    }
}
