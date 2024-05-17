package labs.secondSemester.server;

import jakarta.xml.bind.JAXBException;
import labs.secondSemester.commons.exceptions.FailedBuildingException;
import labs.secondSemester.commons.managers.CollectionManager;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Scanner;

public class Main {
    private static final Logger logger = LogManager.getLogger(Main.class);
    public static void main(String[] args) {

        try {
            Class.forName("org.postgresql.Driver");
        } catch (ClassNotFoundException e) {
            System.out.println("Проблемы с драйвером. Ошибка: " + e.getMessage());
            System.out.println("Завершение работы.");
            System.exit(-1);
        }

        if (args.length == 0) {
            logger.error("Вы не указали имя файла с важнейшими данными. Запуск невозможен.");
            System.exit(1);
        }
        String filename = args[0];


        try {
            Server server = new Server(filename);
            server.start();
        }
        catch (Exception e) {
            System.out.println(e.getMessage());
            System.exit(-1);
        }
    }
}