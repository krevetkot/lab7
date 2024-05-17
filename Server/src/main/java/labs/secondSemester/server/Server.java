package labs.secondSemester.server;

import labs.secondSemester.commons.exceptions.FailedBuildingException;
import labs.secondSemester.commons.managers.CollectionManager;
import labs.secondSemester.commons.managers.DatabaseManager;
import labs.secondSemester.commons.network.Serializer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.FileNotFoundException;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.sql.SQLException;
import java.util.Scanner;

public class Server {
    private final int PORT = 2224;
    private final DatagramSocket datagramSocket;
    private final Serializer serializer;
    private final RuntimeManager runtimeManager;
    private final int BUFFER_LENGTH = 1000;
    private DatabaseManager databaseManager;
    private String fileWithCredentials;

    private static final Logger logger = LogManager.getLogger(Server.class);

    public Server(String fileWithCredentials) throws SocketException {
        this.fileWithCredentials = fileWithCredentials;
        datagramSocket = new DatagramSocket(PORT);
        serializer = new Serializer();
        runtimeManager = new RuntimeManager();
    }

    public void start() {
        logger.info("Запуск сервера.");

        connectToBD();

//        try {
//            databaseManager.saveCollection();
//        } catch (SQLException e) {
//            logger.error(e.getMessage());
//            System.exit(-1);
//        }

        CollectionManager.getCollection().clear();

        try {
            databaseManager.loadCollection();
            logger.info("Коллекция успешно загружена.");
        } catch (SQLException | FailedBuildingException e) {
            logger.error("Коллекция не загружена. Ошибка: " + e.getMessage());
            System.exit(-1);
        }

        new Thread(new ClientHandler(datagramSocket, databaseManager)).start();

//        databaseManager.closeConnection();
    }

    public void connectToBD(){
        logger.info("Получение логина и пароля для входа в БД.");
        String login = null, password = null, URL = null;
//        login = "s409577";
//        password = "7Tpx3iO5o2XLp7ja";
        try {
            Scanner signInScanner = new Scanner(new File(fileWithCredentials));
            login = signInScanner.nextLine().trim();
            password = signInScanner.nextLine().trim();
            URL = signInScanner.nextLine().trim();
        } catch (FileNotFoundException e) {
            logger.error("Проблема с входными данными для подключения к БД. Ошибка: " + e.getMessage());
            logger.error("Завершение работы.");
            System.exit(-1);
        }

        logger.info("Создание менеджера базы данных.");
        databaseManager = new DatabaseManager(login, password, URL);
        databaseManager.connect();
    }
}
