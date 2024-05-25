package labs.secondSemester.commons.commands;

import labs.secondSemester.commons.exceptions.IllegalValueException;
import labs.secondSemester.commons.managers.DatabaseManager;
import labs.secondSemester.commons.network.Response;

import java.util.Scanner;

/**
 * Команда save: сохраняет коллекцию в файл.
 *
 * @author Kseniya
 */
public class Save extends Command {
    public Save() {
        super("save", "сохранить коллекцию в файл", false);
    }

    @Override
    public Response execute(String argument, boolean fileMode, Scanner scanner, DatabaseManager dbmanager) {
        return null;
    }
}
