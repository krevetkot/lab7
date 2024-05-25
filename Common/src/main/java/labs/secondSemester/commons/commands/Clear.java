package labs.secondSemester.commons.commands;

import labs.secondSemester.commons.exceptions.AccessDeniedException;
import labs.secondSemester.commons.managers.CollectionManager;
import labs.secondSemester.commons.managers.DatabaseManager;
import labs.secondSemester.commons.network.Response;

import java.util.Scanner;

/**
 * Команда clear: очищает коллекцию.
 *
 * @author Kseniya
 */
public class Clear extends Command {
    public Clear() {
        super("clear", "очистить коллекцию", false);
    }

    @Override
    public Response execute(String argument, boolean fileMode, Scanner scanner, DatabaseManager dbmanager) throws AccessDeniedException {
        if (CollectionManager.getCollectionForReading().isEmpty()) {
            return new Response("Коллекция и так пуста.");
        } else {
            for (int i=0; i<CollectionManager.getCollectionForReading().size(); i++){
                if (getClientID().getLogin().equals(CollectionManager.getCollectionForReading().get(i).getOwner())){
//                    try {
//                        dbmanager.removeByID(i);
//                    } catch (SQLException e) {
//                        throw new RuntimeException(e);
//                    } catch (ConnectionException e) {
//                        return dbmanager.reconnect();
//                    }
                    CollectionManager.getCollectionForWriting().remove(i);
                }
            }
            return new Response("Коллекция очищена. Удалены только элементы, принадлежащие Вам.");
        }
    }
}
