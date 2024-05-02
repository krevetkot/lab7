package labs.secondSemester.server;

import labs.secondSemester.commons.commands.Command;
import labs.secondSemester.commons.exceptions.IllegalValueException;
import labs.secondSemester.commons.network.Header;
import labs.secondSemester.commons.network.Packet;
import labs.secondSemester.commons.network.Response;
import labs.secondSemester.commons.network.Serializer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.ByteArrayOutputStream;
import java.io.EOFException;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketAddress;
import java.net.SocketException;
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
    private static final Logger logger = LogManager.getLogger();


    {
        serializer = new Serializer();
        runtimeManager = new RuntimeManager();
    }

    public ClientHandler(DatagramSocket socket) throws SocketException {
//        datagramSocket = new DatagramSocket(PORT);
        datagramSocket = socket;
    }

    @Override
    public void run() {
        while (true) {
            try {
                byte[] buffer = new byte[BUFFER_LENGTH];
                DatagramPacket datagramPacket = new DatagramPacket(buffer, buffer.length, datagramSocket.getInetAddress(), PORT);
                Command command = readRequest(datagramPacket, buffer);


//                fixedPool.execute(() -> {
                    Response response = null;
                    try {
                        response = runtimeManager.commandProcessing(command, false, null);
                    } catch (IllegalValueException e) {
                        System.out.println(e.getMessage());
                    }

                    Response finalResponse = response;
//                    forkJoinPool.execute(() -> {
                        try {
                            sendResponse(finalResponse, datagramPacket.getSocketAddress());
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
//                    });
//                });
            } catch (Exception e) {
                logger.error("thread: " + Thread.currentThread().getName() + ":" + e.getMessage());
                return;
            }
        }
    }

    public void sendResponse(Response response, SocketAddress address) throws IOException {
        byte[] array = serializer.serialize(response);
        DatagramPacket datagramPacket2 = new DatagramPacket(array, array.length, address);
        datagramSocket.send(datagramPacket2);
    }

    public <T> T readRequest(DatagramPacket datagramPacket, byte[] buffer) throws IOException {
        datagramSocket.receive(datagramPacket);
        return serializer.deserialize(buffer);
    }
}
