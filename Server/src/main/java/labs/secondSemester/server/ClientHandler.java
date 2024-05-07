package labs.secondSemester.server;

import labs.secondSemester.commons.commands.Command;
import labs.secondSemester.commons.exceptions.IllegalValueException;
import labs.secondSemester.commons.managers.DatabaseManager;
import labs.secondSemester.commons.network.Header;
import labs.secondSemester.commons.network.Packet;
import labs.secondSemester.commons.network.Response;
import labs.secondSemester.commons.network.Serializer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketAddress;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ForkJoinPool;

public class ClientHandler implements Runnable{
    private final ForkJoinPool forkJoinPool = new ForkJoinPool();
    private final int PORT = 2224;
    private final DatagramSocket datagramSocket;
    private final Serializer serializer;
    private final RuntimeManager runtimeManager;
    private final int BUFFER_LENGTH = 10240;
    private final ExecutorService fixedPool = Executors.newFixedThreadPool(10);
    private DatabaseManager databaseManager;
    private static final Logger logger = LogManager.getLogger();


    {
        serializer = new Serializer();
        runtimeManager = new RuntimeManager();
    }

    public ClientHandler(DatagramSocket socket, DatabaseManager dbmanager){
        datagramSocket = socket;
        databaseManager = dbmanager;
    }

    @Override
    public void run() {
        while (true) {
            try {
                byte[] buffer = new byte[BUFFER_LENGTH];
                DatagramPacket datagramPacket = new DatagramPacket(buffer, buffer.length, datagramSocket.getInetAddress(), PORT);

                Command command = readRequest(datagramPacket, buffer);

                fixedPool.execute(() -> {
                    Response response = new Response();
                    try {
                        logger.info("Выполнение запроса.");
                        response = runtimeManager.commandProcessing(command, false, null, databaseManager);
                    } catch (IllegalValueException e) {
                        response.add(e.getMessage());
                    }

                    Response finalResponse = response;
                    forkJoinPool.execute(() -> {
                        try {
                            logger.info("Отправка ответа.");
                            sendResponse(finalResponse, datagramPacket.getSocketAddress());
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                    });
                });
            } catch (Exception e) {
                logger.error("thread: " + Thread.currentThread().getName() + ":" + e.getMessage());
                return;
            }
        }
    }

    public void sendResponse(Response response, SocketAddress address) throws IOException {
        try {
            Header header = new Header(0, 0);
            int headerLength = serializer.serialize(header).length + 200;

            byte[] buffer = serializer.serialize(response);
            int bufferLength = buffer.length;
            int countOfPieces = bufferLength/(BUFFER_LENGTH-headerLength);
            if (countOfPieces*(BUFFER_LENGTH-headerLength) < bufferLength){
                countOfPieces += 1;
            }
            for (int i=0; i<countOfPieces; i++){
                header = new Header(countOfPieces, i);
                headerLength = serializer.serialize(header).length + 200;
                Packet packet = new Packet(header, Arrays.copyOfRange(buffer, i*(BUFFER_LENGTH-headerLength), Math.min(bufferLength, (i+1)*(BUFFER_LENGTH-headerLength)) ));

                byte[] array = serializer.serialize(packet);
                DatagramPacket datagramPacket2 = new DatagramPacket(array, array.length, address);
                datagramSocket.send(datagramPacket2);
                Thread.sleep(100);
            }

        }
        catch (IOException | InterruptedException e){
            logger.error(e.getMessage());
        }
    }

    public <T> T readRequest(DatagramPacket datagramPacket, byte[] buffer) throws IOException {
//        logger.info("Чтение запроса.");
        datagramSocket.receive(datagramPacket);
        Packet packet = serializer.deserialize(buffer);
        Header header = packet.getHeader();
        int countOfPieces = header.getCount();
        ArrayList<Packet> list = new ArrayList<>(countOfPieces);

        for (int i = 0; i < countOfPieces; i++) {
            list.add(null);
        }

        list.add(header.getNumber(), packet);
        int k = 1;

        while (k<countOfPieces){
            datagramSocket.receive(datagramPacket);
            Packet newPacket = serializer.deserialize(buffer);
            Header newHeader = newPacket.getHeader();
            list.add(newHeader.getNumber(), newPacket);
            k += 1;
        }

        int buffLength = 0;
        for (int i = 0; i < countOfPieces; i++) {
            buffLength += list.get(i).getPieceOfBuffer().length;
        }
        try (ByteArrayOutputStream byteStream = new ByteArrayOutputStream(buffLength)) {
            for (int i = 0; i < countOfPieces; i++) {
                byteStream.write(list.get(i).getPieceOfBuffer());
            }
            return serializer.deserialize(byteStream.toByteArray());
        } catch (Exception e){
            logger.error(e.getMessage());
            return null;
        }
    }
}
