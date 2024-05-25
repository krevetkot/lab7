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
 * Команда update id {element}: обновляет значение элемента коллекции, id которого равен заданному.
 *
 * @author Kseniya
 */
public class Update extends Command {
    public Update() {
        super("update", "id {element}: обновить значение элемента коллекции, id которого равен заданному", true);
    }

    @Override
    public Response execute(String argument, boolean fileMode, Scanner scanner, DatabaseManager dbmanager) throws NumberFormatException, NoSuchElementException, SQLException, labs.secondSemester.commons.exceptions.AccessDeniedException {
        Response response = new Response();
        if (CollectionManager.getCollectionForReading().isEmpty()) {
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

        int index = CollectionManager.getCollectionForReading().indexOf(oldDragon);
        int oldID = oldDragon.getId();

        Dragon changedDragon = getObjectArgument();

        try {
            dbmanager.updateOrAddDragon(changedDragon, getClientID(), true, oldID);
        } catch (AccessDeniedException e) {
            return new Response(e.getMessage());
        } catch (ConnectionException e) {
            Response response1 = dbmanager.reconnect(new Response(), 1);
            if (response1==null){
                return execute(argument, fileMode, scanner, dbmanager);
            } else {
                return response1;
            }
        }
        changedDragon.setId(oldID);
        CollectionManager.getCollectionForWriting().set(index, changedDragon);
        response.add("Элемент с ID " + id + " обновлен");

        return response;
    }
}
