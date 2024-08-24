package database;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.FileNotFoundException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

public class DBMIntegrationTest {
    static final private String SCHEMA = "test";
    static private int testCount = 0;
    static private PreparedStatement stmt;
    static private ResultSet rs;


    @BeforeAll
    static void init() throws SQLException, ClassNotFoundException {
        new DBM(SCHEMA);
    }

    @AfterAll
    static void finish() throws SQLException {
        DBM.conn.createStatement().execute("DROP DATABASE IF EXISTS " + SCHEMA);
        DBM.conn.close();
    }

    @BeforeEach
    void setUp() throws FileNotFoundException, SQLException {
        testCount++;
        System.out.println("Test " + testCount);

        DBM.setupSchema();
        DBM.createTestData();
    }

    @Test
    void insertMultiple() throws SQLException {
        int expected = 8;

        Event event1 = new Event();
        Event event2 = new Event();
        Event event3 = new Event();
        DBM.insertIntoDB(event1, event2, event3);

        stmt = DBM.conn.prepareStatement("SELECT COUNT(*) FROM events");
        rs = stmt.executeQuery();
        rs.next();
        int actual = rs.getInt(1);

        assertEquals(expected, actual);
    }

    @Test
    void insertList() throws SQLException {
        int expected = 9;

        Event[] events = new Event[4];
        for (int i = 0; i < 4; i++) {
            events[i] = new Event();
        }
        DBM.insertIntoDB(events);

        stmt = DBM.conn.prepareStatement("SELECT COUNT(*) FROM events");
        rs = stmt.executeQuery();
        rs.next();
        int actual = rs.getInt(1);

        assertEquals(expected, actual);
    }

    @Test
    void insertNulls() throws SQLException {
        int expected = 1 + DBM.getFromDB(DBM.conn.prepareStatement("SELECT COUNT(*) FROM events"), rs -> rs.getInt(1)).get(0);

        Event[] events = new Event[3];
        events[1] = new Event();
        DBM.insertIntoDB(events);

        stmt = DBM.conn.prepareStatement("SELECT COUNT(*) FROM events");
        rs = stmt.executeQuery();
        rs.next();
        int actual = rs.getInt(1);

        assertEquals(expected, actual);
    }

    @Test
    void insertDuplicateThrowsException() {
        //Event event = new Event(1, 2020, 4, 9);
        //DBM.insertIntoDB(event);

        //assertThrows(SQLIntegrityConstraintViolationException.class, () -> DBM.insertIntoDB(event));
    }

    @Test
    void updatebyArray() throws SQLException {
        String expected;

        List<User> userList = DBM.getFromDB(DBM.conn.prepareStatement("SELECT * FROM users"), new User());
        for (int i = 0; i < 5; i++) {
            userList.get(i).setUserEmail("test"+i+"@newdomain.biz");
        }
        DBM.updateInDB(userList);
        List<User> updatedList = DBM.getFromDB(DBM.conn.prepareStatement("SELECT * FROM users"), new User());

        String actual;
        for (int i = 0; i < 5; i++) {
            expected = "test"+i+"@newdomain.biz";
            actual = updatedList.get(i).getUserEmail();
            assertEquals(expected, actual);
        }
    }

    @Test
    void updateNulls() throws SQLException {
        User[] users = new User[3];
        DBM.updateInDB(users);
    }

    @Test
    void deleteByList() throws SQLException {
        int expected = 2;

        List<Event> eventList = DBM.getFromDB(DBM.conn.prepareStatement("SELECT * FROM events"), new Event());
        eventList.remove(1);
        eventList.remove(3);
        DBM.deleteFromDB(eventList);

        int actual = DBM.getFromDB(DBM.conn.prepareStatement("SELECT count(*) FROM events"), rs -> rs.getInt(1)).get(0);    //first row of count column

        assertEquals(expected, actual);
    }

    @Test
    void sanitizeSQLInjection() throws SQLException {
        User injection = new User("TestName', 'TestEmail', 'TestPass', 'TestSalt', '1'); -- ", "email@domain.com", "Passw0rd!");    //1 in last slot would ordinarily mean user is admin
        DBM.insertIntoDB(injection);

        List<User> userList = DBM.getFromDB(DBM.conn.prepareStatement("SELECT * FROM users"), new User());

        assertFalse(userList.get(0).getAdmin());
    }
}
