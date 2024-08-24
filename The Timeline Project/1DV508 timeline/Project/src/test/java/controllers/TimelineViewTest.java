package controllers;

import database.DBM;
import database.Timeline;
import org.junit.jupiter.api.*;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class TimelineViewTest {

    static final private String SCHEMA = "test";
    static private int testCount;
    static private PreparedStatement stmt;
    static private ResultSet rs;

    TimelineView sut = new TimelineView();

    @BeforeAll
    static void begin() throws SQLException, ClassNotFoundException {
        new DBM(SCHEMA);
        //DBM dbm = new DBM("jdbc:mysql://localhost", "root", "AJnuHA^8VKHht=uB", "project");
    }

    @AfterAll
    static void end() throws SQLException {
        DBM.conn.createStatement().execute("DROP DATABASE IF EXISTS test");
        DBM.close();
    }

    @BeforeEach
    void setUp() {
        DBM.setupSchema();
        System.out.println("Test " + ++testCount);
    }

    @AfterEach

    @Test
    void setActiveTimelineIDTest() throws SQLException {
        stmt = DBM.conn.prepareStatement("SELECT COUNT(*) FROM timelines");
        rs = stmt.executeQuery();
        rs.next();
        int totalTimelines = rs.getInt(1);

        int actual;
        int expected;

        for (int i = 1; i < totalTimelines; i++) {
            //assertTrue(sut.setActiveTimeline(i));   //Tests that only one timeline is in the pulled list
            actual = sut.activeTimeline.getID();
            expected = i;
            assertEquals(expected, actual);         //Tests that the timeline was properly changed.
        }
    }

    @Test
    void setActiveTimelineObjectTest() throws SQLException {
        List<Timeline> list = DBM.getFromDB(DBM.conn.prepareStatement("SELECT * FROM timelines"), new Timeline());

        String actual;
        String expected;

        for (Timeline t : list) {
            sut.setActiveTimeline(t);

            actual = sut.activeTimeline.getName();
            expected = t.getName();

            assertEquals(expected, actual);         //Tests that the timeline was properly changed.
        }
    }

}
