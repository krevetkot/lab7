package labs.secondSemester.commons.commands;

import labs.secondSemester.commons.exceptions.IllegalValueException;
import labs.secondSemester.commons.managers.CollectionManager;
import labs.secondSemester.commons.managers.DatabaseManager;
import labs.secondSemester.commons.network.Response;
import labs.secondSemester.commons.objects.Dragon;

import java.nio.file.AccessDeniedException;
import java.sql.SQLException;
import java.util.Collections;
import java.util.Scanner;

/**
 * Команда add {element}: добавляет новый элемент в коллекцию.
 *
 * @author Kseniya
 */
public class Add extends Command {
    public Add() {
        super("add", " {element}: добавить новый элемент в коллекцию", false);
    }

    @Override
    public Response execute(String argument, boolean fileMode, Scanner scanner, DatabaseManager dbmanager) throws IllegalValueException, SQLException {
        Response response = new Response();
        Dragon buildedDragon = getObjectArgument();
        if (!CollectionManager.contains(buildedDragon)) {
            int dragonID;
            try {
                dragonID = dbmanager.updateOrAddDragon(buildedDragon, getClientID(), false, -1);
            } catch (AccessDeniedException e) {
                response.add(e.getMessage());
                return response;
            }
            buildedDragon.setId(dragonID);
            CollectionManager.getCollection().add(buildedDragon);
            Collections.sort(CollectionManager.getCollection());
            response.add("Спасибо, ваши данные приняты!");
        } else {
            response.add("Такой дракон уже есть в коллекции.");
        }
        return response;
    }
}
