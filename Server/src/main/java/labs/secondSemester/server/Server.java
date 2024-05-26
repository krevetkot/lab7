package labs.secondSemester.server;

import labs.secondSemester.commons.exceptions.FailedBuildingException;
import labs.secondSemester.commons.managers.CollectionManager;
import labs.secondSemester.commons.managers.DatabaseManager;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.FileNotFoundException;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Scanner;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ForkJoinPool;

public class Server {
    private final DatagramSocket datagramSocket;
    private final int BUFFER_LENGTH = 1000;
    private DatabaseManager databaseManager;
    private final String fileWithCredentials;
    private final ExecutorService executorService;
    final int COUNT_OF_CLIENTS = 10;

    private static final Logger logger = LogManager.getLogger(Server.class);

    public Server(String fileWithCredentials) throws SocketException {
        this.fileWithCredentials = fileWithCredentials;
        int PORT = 2224;
        datagramSocket = new DatagramSocket(PORT);
        executorService = Executors.newFixedThreadPool(COUNT_OF_CLIENTS);
    }

    public void start() {
        logger.info("Запуск сервера.");

        connectToBD();

        CollectionManager.getCollection().clear();

        try {
            databaseManager.loadCollection();
            logger.info("Коллекция успешно загружена.");
        } catch (SQLException | FailedBuildingException e) {
            logger.error("Коллекция не загружена. Ошибка: " + e.getMessage());
            System.exit(-1);
        }


        ArrayList<ClientHandler> CHList = new ArrayList<>(COUNT_OF_CLIENTS);

        ExecutorService fixedPool = Executors.newFixedThreadPool(10);
        ForkJoinPool forkJoinPool = new ForkJoinPool();
        for (int i = 0; i < COUNT_OF_CLIENTS; i++) {
            CHList.add(new ClientHandler(datagramSocket, databaseManager, fileWithCredentials, fixedPool, forkJoinPool));
            executorService.submit(CHList.get(i));
        }


        Thread closingThreads = new Thread(() ->
        {
            for (ClientHandler client: CHList){
                client.shutDown();
            }
            System.out.println("Все потоки закрыты (сработал shutDownHook)");
        });

        Runtime.getRuntime().addShutdownHook(closingThreads);
    }

    public void connectToBD(){
        logger.info("Получение логина и пароля для входа в БД.");
        String login = null, password = null, URL = null;
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
        if (databaseManager.getConnection()==null){
            logger.info("Без базы данных мы работать не будем. Приходите позже.");
            System.exit(-1);
        }
    }
}
