package labs.secondSemester.server;

import labs.secondSemester.commons.exceptions.FailedBuildingException;
import labs.secondSemester.commons.managers.CollectionManager;
import labs.secondSemester.commons.managers.Validator;
import labs.secondSemester.commons.objects.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;

public class DatabaseManager {
    private String URL = "jdbc:postgresql://localhost:2225/studs";
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
                "join person on (dragon.killer = person.person_id) " +
                "join coordinates on (dragon.coordinates = coordinates.coordinates_id);";
        PreparedStatement joinStatement = connection.prepareStatement(join);
        ResultSet result = joinStatement.executeQuery();
        while (result.next()){
            Dragon newDragon = parse(result);
            collection.add(newDragon);
        }
        CollectionManager.getCollection().addAll(collection);
        Collections.sort(collection);
    }

    public void saveCollection(){

    }

    public int addCoordinates(Coordinates coords) throws SQLException {
        PreparedStatement addStatement = connection.prepareStatement("insert into coordinates(x, y) values (?, ?)");
        addStatement.setLong(1, coords.getX());
        addStatement.setFloat(2, coords.getY());
        addStatement.executeUpdate();
        ResultSet res = addStatement.executeQuery();
        res.next();
        return res.getInt("id");
    }

    public int addPerson(Person person) throws SQLException {
        PreparedStatement addStatement = connection.prepareStatement("insert into person (person_name, passport_id, eye_color, hair_color, nationality, countKilledDragons) values (?, ?, ?, ?, ?, ?)");
        addStatement.setString(1, person.getName());
        addStatement.setString(2, person.getPassportID());
        addStatement.setString(3, String.valueOf(person.getEyeColor()));
        addStatement.setString(4, String.valueOf(person.getHairColor()));
        addStatement.setString(5, String.valueOf(person.getNationality()));
        addStatement.setLong(3, person.getCountKilledDragons());
        addStatement.executeUpdate();
        ResultSet res = addStatement.executeQuery();
        res.next();
        return res.getInt("id");
    }

    public Dragon parse(ResultSet result) throws SQLException, FailedBuildingException {
        int dragonId = result.getInt("dragon_id");
        String dragonName = result.getString("dragon_name");
        LocalDate creationDate = result.getDate("creation_date").toLocalDate();
        long age = result.getLong("age");
        long weight = result.getLong("weight");
        boolean speaking = result.getBoolean("speaking");
        DragonType type = DragonType.valueOf(result.getString("type"));

        long x = result.getLong("x");
        float y = result.getFloat("y");

        //сделать адекватную обработку когда в персоне нул

        String personName = result.getString("person_name");
        String passportId = result.getString("passport_id");
        Color eyeColor = Color.valueOf(result.getString("eye_color"));
        Color hairColor = Color.valueOf(result.getString("hair_color"));
        Country nationality = Country.valueOf(result.getString("nationality"));
        Long countKilledDragons = result.getLong("countKilledDragons");

        Coordinates coords = new Coordinates(x, y);
        Person person = new Person(personName, passportId, eyeColor, hairColor, nationality, countKilledDragons);
        Dragon dragon = new Dragon(dragonId, dragonName, coords, creationDate, age, weight, speaking, type, person);
        if (!Validator.dragonValidation(dragon)) {
            throw new FailedBuildingException("Недопустимое значение в поле!", Dragon.class);
        }
        return dragon;
    }
}
