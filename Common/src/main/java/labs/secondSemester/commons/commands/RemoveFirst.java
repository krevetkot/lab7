package labs.secondSemester.commons.commands;

import labs.secondSemester.commons.exceptions.AccessDeniedException;
import labs.secondSemester.commons.exceptions.IllegalValueException;
import labs.secondSemester.commons.managers.CollectionManager;
import labs.secondSemester.commons.managers.DatabaseManager;
import labs.secondSemester.commons.network.Response;

import java.sql.SQLException;
import java.util.Scanner;

/**
 * Команда remove_first: удаляет удалить первый элемент из коллекции.
 *
 * @author Kseniya
 */
public class RemoveFirst extends Command {
    public RemoveFirst() {
        super("remove_first", "удалить первый элемент из коллекции", false);
    }

    @Override
    public Response execute(String argument, boolean fileMode, Scanner scanner, DatabaseManager dbmanager) throws IllegalValueException, SQLException, AccessDeniedException {
        if (CollectionManager.getCollectionForReading().isEmpty()) {
            return new Response("Коллекция пуста.");
        }
        if (!getClientID().getLogin().equals(CollectionManager.getCollectionForReading().get(0).getOwner())){
            return new Response("Отказано в доступе: Вы не владелец элемента.");
        }
        else {
            dbmanager.removeByID(CollectionManager.getCollectionForReading().get(0).getId());
            CollectionManager.getCollectionForWriting().remove(0);
            return new Response("Первый элемент в коллекции удален.");
        }
    }
}
