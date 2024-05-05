package labs.secondSemester.server;

import labs.secondSemester.commons.managers.DatabaseManager;
import labs.secondSemester.commons.network.Serializer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.util.Scanner;

public class Server {
    private final int PORT = 2224;
    private final DatagramSocket datagramSocket;
    private final Serializer serializer;
    private final RuntimeManager runtimeManager;
    private final int BUFFER_LENGTH = 1000;
    private DatabaseManager databaseManager;

    private static final Logger logger = LogManager.getLogger(Server.class);

    {
        serializer = new Serializer();
        runtimeManager = new RuntimeManager();
    }

    public Server() throws SocketException {
        datagramSocket = new DatagramSocket(PORT);
    }

    public void start() throws IOException {
        logger.info("Запуск сервера.");

        try {
            Class.forName("org.postgresql.Driver");
        } catch (ClassNotFoundException e) {
            logger.error("Проблемы с драйвером. Ошибка: " + e.getMessage());
            logger.error("Завершение работы.");
            System.exit(-1);
        }

        BDConnect();



        while (true) {
            new Thread(new ClientHandler(datagramSocket, databaseManager)).start();
        }

    }

    public void BDConnect(){
        logger.info("Получение логина и пароля для входа в БД.");
        String login = null, password = null;
        try {
            Scanner signInScanner = new Scanner(new File("BDCredentials.txt"));
            login = signInScanner.nextLine().trim();
            password = signInScanner.nextLine().trim();
        } catch (FileNotFoundException e) {
            logger.error("Проблема с входными данными для подключения к БД. Ошибка: " + e.getMessage());
            logger.error("Завершение работы.");
            System.exit(-1);
        }

        logger.info("Создание менеджера базы данных.");
        databaseManager = new DatabaseManager(login, password);
        databaseManager.connect();
    }

//    public void sendResponse(Response response, SocketAddress address){
//        try {
//            Header header = new Header(0, 0, null);
//            int headerLength = serializer.serialize(header).length + 200;
//
//            byte[] buffer = serializer.serialize(response);
//            int bufferLength = buffer.length;
//            int countOfPieces = bufferLength/(BUFFER_LENGTH-headerLength);
//            if (countOfPieces*(BUFFER_LENGTH-headerLength) < bufferLength){
//                countOfPieces += 1;
//            }
//            for (int i=0; i<countOfPieces; i++){
//                header = new Header(countOfPieces, i, null);
//                headerLength = serializer.serialize(header).length + 200;
//                Packet packet = new Packet(header, Arrays.copyOfRange(buffer, i*(BUFFER_LENGTH-headerLength), Math.min(bufferLength, (i+1)*(BUFFER_LENGTH-headerLength)) ));
//
//                byte[] array = serializer.serialize(packet);
//                DatagramPacket datagramPacket2 = new DatagramPacket(array, array.length, address);
//                datagramSocket.send(datagramPacket2);
//                Thread.sleep(100);
//            }
//
//        }
//        catch (IOException | InterruptedException e){
//            logger.error(e.getMessage());
//        }
//    }
//
//
//    public <T> T readRequest(DatagramPacket datagramPacket, byte[] buffer) throws IOException {
//        datagramSocket.receive(datagramPacket);
//        Packet packet = serializer.deserialize(buffer);
//        Header header = packet.getHeader();
//        int countOfPieces = header.getCount();
//        ArrayList<Packet> list = new ArrayList<>(countOfPieces);
//
//        for (int i = 0; i < countOfPieces; i++) {
//            list.add(null);
//        }
//
//        list.add(header.getNumber(), packet);
//        int k = 1;
//
//        while (k<countOfPieces){
//            datagramSocket.receive(datagramPacket);
//            Packet newPacket = serializer.deserialize(buffer);
//            Header newHeader = newPacket.getHeader();
//            list.add(newHeader.getNumber(), newPacket);
//            k += 1;
//        }
//
//        int buffLength = 0;
//        for (int i = 0; i < countOfPieces; i++) {
//            buffLength += list.get(i).getPieceOfBuffer().length;
//        }
//        try (ByteArrayOutputStream byteStream = new ByteArrayOutputStream(buffLength)) {
//            for (int i = 0; i < countOfPieces; i++) {
//                byteStream.write(list.get(i).getPieceOfBuffer());
//            }
//            return serializer.deserialize(byteStream.toByteArray());
//        } catch (Exception e){
//            logger.error(e.getMessage());
//            return null;
//        }
//    }
}
