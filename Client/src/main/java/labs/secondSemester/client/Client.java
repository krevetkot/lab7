package labs.secondSemester.client;

import labs.secondSemester.commons.commands.*;
import labs.secondSemester.commons.exceptions.FailedBuildingException;
import labs.secondSemester.commons.exceptions.IllegalValueException;
import labs.secondSemester.commons.managers.Console;
import labs.secondSemester.commons.network.*;
import labs.secondSemester.commons.objects.Dragon;
import labs.secondSemester.commons.objects.forms.DragonForm;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.NoSuchElementException;
import java.util.Scanner;


public class Client {
    private final DatagramChannel datagramChannel;
    private InetSocketAddress serverAddress;
    private Selector selector;
    private final Serializer serializer;
    private final FileManager fileManager;
    private final String ip;
    private final int BUFFER_LENGTH = 10000;
    private ClientIdentification clientID;

    {
        selector = Selector.open();
        datagramChannel = DatagramChannel.open();
        serializer = new Serializer();
        selector = Selector.open();
    }

    public Client(String ip) throws IOException {

        fileManager = new FileManager(this);
        this.ip = ip;
    }


    public void start() {

        connectServer(1);
        ByteBuffer buffer = ByteBuffer.allocate(BUFFER_LENGTH);
        String request = null;
        Scanner scanner = new Scanner(System.in);
        clientID = new ClientIdentification("Kseniya", "111");
        CommandFactory commandFactory = new CommandFactory(clientID);
//            singIn(scanner);
        //нужна какая-то проверка, что такой чел есть, но ее позже
        System.out.println(clientID.getLogin() +
                ", приветствуем Вас в приложении по управлению коллекцией! Введите 'help' для вывода доступных команд.");

        while (true) {
            try {
                request = scanner.nextLine();
            } catch (NoSuchElementException e1) {
                System.out.println("До свидания! Приходите еще :)");
                System.exit(0);
            } catch (Exception e) {
                System.out.println(e.getMessage());
            }

            try {
                Command command = commandFactory.buildCommand(request);
                if (command instanceof Add || command instanceof InsertAt || command instanceof Update) {
                    DragonForm newDragon = new DragonForm();
                    try {
                        Dragon buildedDragon = newDragon.build(scanner, false);
                        buildedDragon.setOwner(clientID.getLogin());
                        command.setObjectArgument(buildedDragon);
                    } catch (FailedBuildingException | IllegalValueException e) {
                        Console.print(e.getMessage(), false);
                    }
                }

                if (command instanceof Exit) {
                    command.execute(null, false, null, null);

                }
                if (command instanceof ExecuteFile) {
                    assert request != null;
                    fileManager.executeFile(request.trim().split(" ")[1], clientID);
                    continue;
                } else {
                    send(command);
                }

            } catch (IllegalValueException | ArrayIndexOutOfBoundsException | NumberFormatException | SQLException e) {
                System.out.println(e.getMessage());
                continue;
            }

            Response response = receive(buffer);
            for (String element : response.getResponse()) {
                System.out.println(element);
            }
        }
    }

    public void singIn(Scanner scanner){
        System.out.println("Введите имя пользователя: ");
        String login;
        while (true) {
            login = scanner.nextLine().trim();
            if (login.isBlank()) {
                System.out.println("Имя пользователя не может быть пустым! Попробуйте еще раз.");
            } else break;
        }
        System.out.println("Введите пароль: ");
        String password;
        while (true) {
            password = scanner.nextLine().trim();
            if (password.isBlank()) {
                System.out.println("Пароль не может быть пустым! Попробуйте еще раз.");
            } else break;
        }
        clientID = new ClientIdentification(login, password);
    }


    public void send(Command command) {
        try {
            Header header = new Header(0, 0);
            int headerLength = serializer.serialize(header).length + 200;

            byte[] buffer = serializer.serialize(command);
            int bufferLength = buffer.length;
            int countOfPieces = bufferLength / (BUFFER_LENGTH - headerLength);
            if (countOfPieces * (BUFFER_LENGTH - headerLength) < bufferLength) {
                countOfPieces += 1;
            }
            for (int i = 0; i < countOfPieces; i++) {
                header = new Header(countOfPieces, i);
                headerLength = serializer.serialize(header).length + 200;
                Packet packet = new Packet(header, Arrays.copyOfRange(buffer, i * (BUFFER_LENGTH - headerLength), Math.min(bufferLength, (i + 1) * (BUFFER_LENGTH - headerLength))));
                datagramChannel.send(ByteBuffer.wrap(serializer.serialize(packet)), serverAddress);
            }

        } catch (IOException e) {
            System.out.println(e.getMessage());
        }
    }


    public Response receive(ByteBuffer buffer) {
        buffer.clear();
        try {
            SocketAddress address = null;
            int time = 1;
            int tries = 1;
            while (!serverAddress.equals(address)) {
                if (time % 500000 == 0) {
                    connectServer(tries);
                    tries += 1;
                }
                buffer.clear();
                address = datagramChannel.receive(buffer);
                time += 1;
            }
            Packet packet = serializer.deserialize(buffer.array());
            Header header = packet.getHeader();
            int countOfPieces = header.getCount();
            ArrayList<Packet> list = new ArrayList<>(3);
            list.add(header.getNumber(), packet);
            list.add(1, null);
            int k = 1;

            while (k < countOfPieces) {
                buffer.clear();
                if (selector.select() < 0) continue;

                datagramChannel.receive(buffer);
                Packet newPacket = serializer.deserialize(buffer.array());
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
            } catch (Exception e) {
                System.out.println(e.getMessage());
                return null;
            }
        } catch (Exception e) {
            System.out.println(e.getMessage());
            return null;
        }
    }

    public void connectServer(int connectionTries) {
        try {
            serverAddress = new InetSocketAddress(ip, 2224);
            datagramChannel.configureBlocking(false);
            datagramChannel.register(selector, SelectionKey.OP_READ);
            System.out.println("Попытка подключения к серверу: " + connectionTries);
            if (connectionTries > 2) {
                System.out.println("Кажется, барахлит подключение к серверу. Попробуйте позже.");
                System.exit(0);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
