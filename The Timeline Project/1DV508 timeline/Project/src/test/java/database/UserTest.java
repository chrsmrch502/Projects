package database;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.sql.*;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class UserTest {
    static User[] users = new User[4];
    static User[] user = new User[4];
    static private DBM sut;

    @BeforeAll
    static void init() throws SQLException, IOException, ClassNotFoundException {
        sut = new DBM();
        DBM.setupSchema();
        DBM.createTestData();
    }

    static void createTestDB() throws SQLException {
        User user1 = new User("John", "john@gmail.com", "somethingCool#1");
        users[0] = user1;
        DBM.insertIntoDB(user1);
        User user2 = new User("John", "john2@gmail.com", "somethingCool#2");
        users[1] = user2;
        DBM.insertIntoDB(user2);
        User user3 = new User("John", "john3@gmail.com", "somethingCool#3");
        users[2] = user3;
        DBM.insertIntoDB(user3);
        User user4 = new User("John", "john4@gmail.com", "somethingCool#4");
        users[3] = user4;
        DBM.insertIntoDB(user4);
    }

    @AfterAll
    static void tearDown() throws SQLException {
        DBM.conn.createStatement().execute("DROP DATABASE IF EXISTS test");
        DBM.conn.close();
    }

    @Test
    void validateUnique() throws SQLException {
        User user1 = new User("John", "john@gmail.com", "somethingCool#1");
        User user2 = new User("John", "johnny@gmail.com", "somethingCool#1");

        // Test if email returns false - exists
        assertFalse(User.validateUnique(user1.getUserEmail()));
        // Test if email returns true - does not exist
        assertTrue(User.validateUnique(user2.getUserEmail()));
        // check exception if not right format - missing @
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            user2.setUserEmail("jonny.gmail.com");
        });
        String expectedMessage = "Invalid email format";
        String actualMessage = exception.getMessage();
        assertTrue(actualMessage.contains(expectedMessage)); // Checks both that exception is thrown and correct message
        // printed
        // check exception if not right format - missing .
        Exception exception1 = assertThrows(IllegalArgumentException.class, () -> {
            user2.setUserEmail("jonny@gmailcom");
        });
        String expectedMessage1 = "Invalid email format";
        String actualMessage1 = exception.getMessage();
        assertTrue(actualMessage1.contains(expectedMessage1)); // Checks both that exception is thrown and correct
        // message printed
    }

    @Test
    void setPassword() {
        // IllegalArgumentException check
        // See if encryption is right??
        User user = new User("John", "johnny@gmail.com", "Th3Mind'5EyE!");
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            user.setPassword("secret");
        });
        String actualMessage = exception.getMessage();
        String expectedMessage = ("Invalid password, must include at least: one digit, one lower case, one upper case, one special character, no white space and be at least 8 character long");

        assertTrue(actualMessage.contains(expectedMessage));
    }

    @Test
    void createFromDB() throws SQLException {
        // Create objects from the DB and see if they are 4(cause I inserted 4)
        ResultSet rs;
        PreparedStatement stmt = DBM.conn.prepareStatement("SELECT COUNT(*) FROM users");
        rs = stmt.executeQuery();
        rs.next();
        int actual = rs.getInt(1);
        assertEquals(users.length, actual);

        // See if the database objects are the same as the ones I pushed
        PreparedStatement stmt1 = DBM.conn.prepareStatement("SELECT * FROM users");
        List<User> userList = DBM.getFromDB(stmt1, new User());
        for (int i = 0; i < users.length; i++) {
            assertEquals(users[i].getUserEmail(), userList.get(i).getUserEmail());
            assertEquals(users[i].getUserName(), userList.get(i).getUserName());
        }
    }

    @Test
        //Need to uncomment two methods in User class to let this test work:  getEncryptedForTest() and getSaltForTest()
        //Might want some integration testing but this is a unit test
    void getInsertQuery() throws SQLException {
        //Ended up comparing the strings from the PreparedStatement created with getInsertQuery and the one created manually by picking the fields from the same user
        User tester = new User("Halli", "halli@hotmail.com", "Th3Mind'5EyE!");
        String sql = "INSERT INTO `users` (`UserName`, `UserEmail`, `Password`, `Salt`, `Admin`) VALUES ('" + tester.getUserName() + "','" + tester.getUserEmail() + "','" + tester.getEncryptedForTest() + "','" + tester.getSaltForTest() + "',?)";
        PreparedStatement out = DBM.conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
        out.setBoolean(1, tester.getAdmin());
        assertEquals(out.toString(), tester.getInsertQuery().toString());
        //Exception testing
        tester.setID(1);
        Exception exception = assertThrows(SQLIntegrityConstraintViolationException.class, () -> {
            tester.getInsertQuery();
        });
        String actualMessage = exception.getMessage();
        String expectedMessage = ("User is already in DB.");

        assertTrue(actualMessage.contains(expectedMessage));

    }

    @Test
        //Need to uncomment two methods in User class to let this test work:  getEncryptedForTest() and getSaltForTest()
        //will do little integration test here to see if the user gets his email updated (using createFromDB())
    void getUpdateQuery() throws SQLException {
        User testerNotInDB = new User("Halli", "halli@hotmail.com", "Th3Mind'5EyE!");
        //Test exception, if user ID = 0 (not in DB)
        Exception exception = assertThrows(SQLDataException.class, () -> {
            testerNotInDB.getUpdateQuery();
        });
        String actualMessage = exception.getMessage();
        String expectedMessage = ("User not in database cannot be updated.");
        assertTrue(actualMessage.contains(expectedMessage));

        // Try updateQuery and see then if createFromDB() fits the changes - integration
        ResultSet rs;
        PreparedStatement stmt = DBM.conn.prepareStatement("SELECT * FROM users WHERE userEmail = ?");
        stmt.setString(1, users[1].getUserEmail());
        rs = stmt.executeQuery();
        rs.next();
        users[1].setUserEmail("lalli@hotmail.com");
        DBM.updateInDB(users[1]); //Now I have updated the email for tester to lalli@hotmail.com, now I will call him by his userID and check if it is updated
        //Now I recreate that user and see if he has the new email
        PreparedStatement stmt1 = DBM.conn.prepareStatement("SELECT * FROM users  WHERE userID = ?");
        stmt1.setInt(1, users[1].getID());
        rs = stmt1.executeQuery();
        rs.next();
        users[1] = users[1].createFromDB(rs);
        assertEquals("lalli@hotmail.com", users[1].getUserEmail());

    }

    @Test
    public void getDeleteQuery() throws SQLException {
        User testerNotInDB = new User("Halli", "halli@hotmail.com", "Th3Mind'5EyE!");
        //Test exception, if user ID = 0 (not in DB)
        Exception exception = assertThrows(SQLDataException.class, () -> {
            testerNotInDB.getDeleteQuery();
        });
        String actualMessage = exception.getMessage();
        String expectedMessage = ("User not in database cannot be updated.");
        assertTrue(actualMessage.contains(expectedMessage));

        //Ended up comparing the strings from the PreparedStatement created with getInsertQuery and the one created manually by picking the fields from the same user
        int userID = users[1].getID();
        String sql = "DELETE FROM `users` WHERE (`UserID` = " + userID + ")";
        PreparedStatement out = DBM.conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
        assertEquals(out.toString(), users[1].getDeleteQuery().toString());
    }

    @Test
    void testToString() {
    }

    @Test
    void isAdmin() throws SQLException { // test to ensure that admin toggles are being sent to database correctly
        // set previous users as admin (4 users)
        users[0].setAdmin(true);
        users[1].setAdmin(true);
        users[2].setAdmin(true);
        users[3].setAdmin(true);
        DBM.updateInDB(users);
        // create new users and set them all as admin (now total 8 users)
        User user1 = new User("John", "john5@gmail.com", "somethingCool#5");
        user[0] = user1;
        DBM.insertIntoDB(user1);
        User user2 = new User("John", "john6@gmail.com", "somethingCool#6");
        user[1] = user2;
        DBM.insertIntoDB(user2);
        User user3 = new User("John", "john7@gmail.com", "somethingCool#7");
        user[2] = user3;
        DBM.insertIntoDB(user3);
        User user4 = new User("John", "john8@gmail.com", "somethingCool#8");
        user[3] = user4;
        DBM.insertIntoDB(user4);
        // generate a list of users from database
        PreparedStatement stmt1 = DBM.conn.prepareStatement("SELECT * FROM users");
        List<User> userList = DBM.getFromDB(stmt1, new User());
        //loop through each user checking if the list from the database matches what their admin status was set to
        for (int i = 0; i < users.length; i++) {
            assertEquals(users[i].getAdmin(), userList.get(i).getAdmin());
        }
        // remove admin status from the last 4 users
        user[0].setAdmin(false);
        user[1].setAdmin(false);
        user[2].setAdmin(false);
        user[3].setAdmin(false);
        DBM.updateInDB(user);

        PreparedStatement stmt2 = DBM.conn.prepareStatement("SELECT * FROM users");
        List<User> userList1 = DBM.getFromDB(stmt2, new User());
        //loop through each user checking if the list from the database matches what their admin status was set to
        for (int i = 0; i < users.length; i++) {
            assertEquals(users[i].getAdmin(), userList1.get(i).getAdmin());
        }
    }
}