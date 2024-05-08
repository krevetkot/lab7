package labs.secondSemester.server;

import jakarta.xml.bind.JAXBException;
import labs.secondSemester.commons.exceptions.FailedBuildingException;
import labs.secondSemester.commons.managers.CollectionManager;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Scanner;

public class Main {
    public static void main(String[] args) {

        try {
            Class.forName("org.postgresql.Driver");
        } catch (ClassNotFoundException e) {
            System.out.println("Проблемы с драйвером. Ошибка: " + e.getMessage());
            System.out.println("Завершение работы.");
            System.exit(-1);
        }

        try {
            Server server = new Server();
            server.start();
        }
        catch (Exception e) {
            System.out.println(e.getMessage());
            System.exit(-1);
        }
    }
}