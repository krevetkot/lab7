package labs.secondSemester.commons.commands;

import labs.secondSemester.commons.exceptions.IllegalValueException;
import labs.secondSemester.commons.managers.CollectionManager;
import labs.secondSemester.commons.managers.DatabaseManager;
import labs.secondSemester.commons.network.Response;
import labs.secondSemester.commons.objects.Dragon;

import java.sql.SQLException;
import java.util.NoSuchElementException;
import java.util.Scanner;

/**
 * Команда remove_by_id id: удаляет элемент из коллекции по его id.
 *
 * @author Kseniya
 */
public class RemoveByID extends Command {
    public RemoveByID() {
        super("remove_by_id", "id: удалить элемент из коллекции по его id", true);
    }

    @Override
    public Response execute(String argument, boolean fileMode, Scanner scanner, DatabaseManager dbmanager) throws IllegalValueException, NoSuchElementException, NumberFormatException, SQLException {
        if (CollectionManager.getCollection().isEmpty()) {
            throw new NoSuchElementException("Коллекция пока что пуста");
        }

        long id = Long.parseLong(argument);
        Dragon oldDragon = CollectionManager.getById(id);

        if (oldDragon == null) {
            throw new NoSuchElementException("Нет элемента с таким ID.");
        }
        if (!getClientID().getLogin().equals(oldDragon.getOwner())){
            return new Response("Отказано в доступе: Вы не владелец элемента.");
        }
        else {
            dbmanager.removeByID((int) id);
            CollectionManager.getCollection().remove(oldDragon);
            return new Response("Элемент с ID " + id + " удален");
        }
    }
}
