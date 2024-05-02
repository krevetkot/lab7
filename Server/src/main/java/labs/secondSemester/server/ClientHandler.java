package labs.secondSemester.server;

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
    private final int BUFFER_LENGTH = 1000;
    private final ExecutorService fixedPool = Executors.newFixedThreadPool(10);
    private static final Logger logger = LogManager.getLogger();


    {
        serializer = new Serializer();
        runtimeManager = new RuntimeManager();
    }

    public ClientHandler() throws SocketException {
        datagramSocket = new DatagramSocket(PORT);
    }

    @Override
    public void run() {
        while (true) {
            try {
                var request = receiveThenDeserialize(clientChannel);

                fixedPool.execute(() -> {
                    var command = Environment.getAvailableCommands().get(request.getCommandName());
                    var response = command.accept(commandInvoker, request);

                    forkJoinPool.execute(() -> sendResponse(response));
                });
            } catch (EOFException ignored) {
            } catch (SocketException e) {
                logger.info("Client disconnected");
                return;
            } catch (IOException | ClassNotFoundException e) {
                logger.error("thread: " + Thread.currentThread().getName() + ":" + e.getMessage());
                return;
            }
        }
    }

    public void sendResponce(Response response, SocketAddress address) throws IOException {
        byte[] array = serializer.serialize(response);
        DatagramPacket datagramPacket2 = new DatagramPacket(array, array.length, address);
        datagramSocket.send(datagramPacket2);
    }
    public <T> T readRequest(DatagramPacket datagramPacket, byte[] buffer) throws IOException {
        datagramSocket.receive(datagramPacket);
        return serializer.deserialize(buffer);
    }
}
