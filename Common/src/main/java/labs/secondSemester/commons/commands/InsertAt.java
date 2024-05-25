package labs.secondSemester.commons.commands;

import labs.secondSemester.commons.exceptions.ConnectionException;
import labs.secondSemester.commons.exceptions.IllegalValueException;
import labs.secondSemester.commons.managers.CollectionManager;
import labs.secondSemester.commons.managers.DatabaseManager;
import labs.secondSemester.commons.network.Response;
import labs.secondSemester.commons.objects.Dragon;

import java.nio.file.AccessDeniedException;
import java.sql.SQLException;
import java.util.NoSuchElementException;
import java.util.Scanner;

/**
 * Команда insert_at index {element}: добавляет новый элемент в заданную позицию.
 *
 * @author Kseniya
 */
public class InsertAt extends Command {
    public InsertAt() {
        super("insert_at", "index {element}: добавить новый элемент в заданную позицию", true);
    }

    @Override
    public Response execute(String argument, boolean fileMode, Scanner scanner, DatabaseManager dbmanager)
            throws NoSuchElementException, NumberFormatException, labs.secondSemester.commons.exceptions.AccessDeniedException {

        if (CollectionManager.getCollectionForReading().isEmpty()) {
            throw new NoSuchElementException("Коллекция пока что пуста");
        }

        int index = Integer.parseInt(argument);

        if (index > CollectionManager.getCollectionForReading().size()) {
            throw new NoSuchElementException("Индекс должен быть меньше или равен размеру коллекции. Размер = "
                    + CollectionManager.getCollectionForReading().size());
        }

        if (index < 0) {
            throw new NoSuchElementException("Агрумент index должен быть больше или равен нулю.");
        }

        try {
            Dragon buildedDragon = getObjectArgument();
            if (!CollectionManager.getCollectionForReading().contains(buildedDragon)) {
                int dragonID = dbmanager.updateOrAddDragon(buildedDragon, getClientID(), false, -1);
                buildedDragon.setId(dragonID);
                CollectionManager.getCollectionForWriting().add(index, buildedDragon);
                return new Response("Спасибо, ваши данные приняты!");
            } else {
                return new Response("Такой дракон уже есть в коллекции.");
            }
        } catch (SQLException | AccessDeniedException e) {
            return new Response(e.getMessage());
        } catch (ConnectionException e) {
            Response response1 = dbmanager.reconnect(new Response(), 1);
            if (response1 == null) {
                return execute(argument, fileMode, scanner, dbmanager);
            } else {
                return response1;
            }
        }
    }

}
