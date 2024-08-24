package database;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.FileNotFoundException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import static org.junit.jupiter.api.Assertions.assertThrows;

class DBMUnitTest {
    static final private String SCHEMA = "test";
    static private int testCount = 0;
    static private PreparedStatement stmt;
    static private ResultSet rs;

    @BeforeAll
    static void init() throws FileNotFoundException, SQLException {
        new DBM(SCHEMA);
        DBM.setupSchema();
        DBM.createTestData();
    }

    @AfterAll
    static void finish() throws SQLException {
        DBM.conn.createStatement().execute("DROP DATABASE IF EXISTS test");
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
    void close() throws SQLException {
        DBM.conn.close();

        assertThrows(SQLException.class, () -> DBM.conn.createStatement());

        new DBM(SCHEMA);
    }
}