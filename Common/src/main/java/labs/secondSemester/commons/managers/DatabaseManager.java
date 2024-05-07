package labs.secondSemester.commons.managers;

import labs.secondSemester.commons.exceptions.FailedBuildingException;
import labs.secondSemester.commons.network.ClientIdentification;
import labs.secondSemester.commons.objects.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.nio.file.AccessDeniedException;
import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;

import static java.sql.Types.INTEGER;

public class DatabaseManager {
    private String URL = "jdbc:postgresql://localhost:9876/studs";
    private String user;
    private String password;
    private Connection connection;
    private static final Logger logger = LogManager.getLogger();

    public DatabaseManager(String user, String password){
        this.user = user;
        this.password = password;
    }

    public void connect(){
        try {
            connection = DriverManager.getConnection(URL, user, password);
            logger.info("Соединение с базой данных установлено.");

        } catch (SQLException e) {
            logger.error("Соединение с базой данных не установлено. Ошибка: " + e.getMessage());
            System.exit(-1);
        }
    }

    public void loadCollection() throws SQLException, FailedBuildingException {
        ArrayList<Dragon> collection = new ArrayList<>();
        String join = "select * from dragon " +
                "left join person on (dragon.killer = person.person_id) " +
                "join coordinates on (dragon.coordinates = coordinates.coordinates_id)" +
                "join users on (dragon.owner = users.user_id);";
        PreparedStatement joinStatement = connection.prepareStatement(join);
        ResultSet result = joinStatement.executeQuery();
        while (result.next()){
            Dragon newDragon = parse(result);
            collection.add(newDragon);
        }
        CollectionManager.getCollection().addAll(collection);
        Collections.sort(collection);
    }

    public void saveCollection() throws SQLException, AccessDeniedException {
        for (Dragon dragon: CollectionManager.getCollection()){
            updateOrAddDragon(dragon, new ClientIdentification("Kseniya", "12345"), false, -1);
        }
    }

    public int updateOrAddCoordinates(Coordinates coords, boolean existence, int id) throws SQLException {
        PreparedStatement statement;
        if (existence){
            statement = connection.prepareStatement("update coordinates set(x, y)=(?, ?) where coordinates_id=? returning coordinates_id;");
            statement.setInt(3, id);
        }
        else {
            statement = connection.prepareStatement("insert into coordinates(x, y) values (?, ?) returning coordinates_id;");
        }

        statement.setLong(1, coords.getX());
        statement.setFloat(2, coords.getY());
        ResultSet res = statement.executeQuery();
        res.next();
        return res.getInt("coordinates_id");
    }

    public int updateOrAddPerson(Person person, boolean existence, int id) throws SQLException {
        PreparedStatement statement;
        if (existence){
            statement = connection.prepareStatement("update coordinates set(person_name, passport_id, eye_color, hair_color, nationality, countKilledDragons)=(?, ?, ?, ?, ?, ?) where person_id=? returning coordinates_id;");
            statement.setInt(7, id);
        }
        else {
            statement = connection.prepareStatement("insert into person (person_name, passport_id, eye_color, hair_color, nationality, countKilledDragons) values (?, ?, ?, ?, ?, ?) returning person_id;");
        }
        statement.setString(1, person.getName());
        statement.setString(2, person.getPassportID());
        statement.setString(3, String.valueOf(person.getEyeColor()));
        statement.setString(4, String.valueOf(person.getHairColor()));
        statement.setString(5, String.valueOf(person.getNationality()));
        statement.setLong(6, person.getCountKilledDragons());
        ResultSet res = statement.executeQuery();
        res.next();
        return res.getInt("person_id");
    }

    public int updateOrAddDragon(Dragon dragon, ClientIdentification clientID, boolean existence, int id) throws SQLException, AccessDeniedException {
        //если existence == true -> update; false -> add
        //реальное id указывается только если existence=true; иначе -1

        int userID = findUser(clientID.getLogin());
        if (userID==-1){
            throw new AccessDeniedException("Отказано в доступе.");
        }

        PreparedStatement statement;
        int coordsID;
        int personID;
        if (existence){

            statement = connection.prepareStatement("update dragon set(dragon_name, coordinates, creation_date, age, weight, speaking, type, killer, owner)=(?, ?, ?, ?, ?, ?, ?, ?, ?) where dragon_id=? returning dragon_id;");
            statement.setInt(10, id);

            PreparedStatement ps = connection.prepareStatement("select coordinates, killer from dragon where dragon_id=?;");
            ps.setInt(1, id);
            ResultSet res = ps.executeQuery();
            res.next();
            coordsID = updateOrAddCoordinates(dragon.getCoordinates(), true, res.getInt("coordinates"));

            if (dragon.getKiller()!=null){
                personID = updateOrAddPerson(dragon.getKiller(), true, res.getInt("killer"));
                statement.setInt(8, personID);
            } else {
                statement.setNull(8, INTEGER);
            }
        }
        else {
            statement = connection.prepareStatement("insert into dragon (dragon_name, coordinates, creation_date, age, weight, speaking, type, killer, owner) values (?, ?, ?, ?, ?, ?, ?, ?, ?) returning dragon_id;");

            coordsID = updateOrAddCoordinates(dragon.getCoordinates(), false, -1);

            if (dragon.getKiller()!=null){
                personID = updateOrAddPerson(dragon.getKiller(), false, -1);
                statement.setInt(8, personID);
            } else {
                statement.setNull(8, INTEGER);
            }
        }

        statement.setString(1, dragon.getName());
        statement.setInt(2, coordsID);
        statement.setDate(3, Date.valueOf(dragon.getCreationDate()));
        statement.setLong(4, dragon.getAge());
        statement.setLong(5, dragon.getWeight());
        statement.setBoolean(6, dragon.getSpeaking());
        statement.setString(7, String.valueOf(dragon.getType()));
        statement.setInt(9, userID);

        ResultSet res = statement.executeQuery();
        res.next();
        if (existence){
            logger.info("Элемент c id = " + id + " обновлен.");
        } else {
            logger.info("Элемент успешно добавлен в базу данных.");
        }
        return res.getInt("dragon_id");
    }

    public int addUser(ClientIdentification clientID) throws SQLException {
        PreparedStatement addStatement = connection.prepareStatement("insert into users(login, password) values (?, ?) returning user_id;");
        addStatement.setString(1, clientID.getLogin());
        addStatement.setString(2, clientID.getPassword());
        ResultSet res = addStatement.executeQuery();
        res.next();
        return res.getInt("user_id");
    }

    public int findUser(String login){
        try {
            PreparedStatement ps = connection.prepareStatement("select * from users where (users.login =?);");
            ps.setString(1, login);
            ResultSet res = ps.executeQuery();
            if (res.next()){
                return res.getInt("user_id");
            }
        } catch (SQLException e) {
            logger.error(e.getMessage());
        }
        return -1;
    }

    public boolean checkPassword(ClientIdentification clientID){
        try {
        PreparedStatement ps = connection.prepareStatement("select password from users where (users.login =?);");
        ps.setString(1, clientID.getLogin());
        ResultSet res = ps.executeQuery();
        res.next();
        return clientID.getPassword().equals(res.getString("password"));
        } catch (SQLException e) {
            logger.error(e.getMessage());
            return false;
        }
    }

    public Dragon parse(ResultSet result) throws SQLException, FailedBuildingException {
        int dragonId = result.getInt("dragon_id");
        String dragonName = result.getString("dragon_name");
        LocalDate creationDate = result.getDate("creation_date").toLocalDate();
        long age = result.getLong("age");
        long weight = result.getLong("weight");
        boolean speaking = result.getBoolean("speaking");
        DragonType type = null;
        if (!result.getString("type").equals("null")){
            type = DragonType.valueOf(result.getString("type"));
        }

        long x = result.getLong("x");
        float y = result.getFloat("y");

        Person person = null;
        if (!(result.getString("killer")==null)){
            String personName = result.getString("person_name");
            String passportId = result.getString("passport_id");
            Color eyeColor = Color.valueOf(result.getString("eye_color"));
            Color hairColor = Color.valueOf(result.getString("hair_color"));
            Country nationality = Country.valueOf(result.getString("nationality"));
            Long countKilledDragons = result.getLong("countKilledDragons");
            person = new Person(personName, passportId, eyeColor, hairColor, nationality, countKilledDragons);
        }

        String ownerName = result.getString("login");

        Coordinates coords = new Coordinates(x, y);
        Dragon dragon = new Dragon(dragonId, dragonName, coords, creationDate, age, weight, speaking, type, person, ownerName);
        if (!Validator.dragonValidation(dragon)) {
            throw new FailedBuildingException("Недопустимое значение в поле!", Dragon.class);
        }
        return dragon;
    }

    public void removeByID(int id) throws SQLException {
            PreparedStatement deleteStatement = connection.prepareStatement("delete from dragon where (dragon_id=?);");
            deleteStatement.setInt(1, id);
            deleteStatement.executeUpdate();
            logger.info("Объект с id = " + id + " успешно удален.");
    }
}
