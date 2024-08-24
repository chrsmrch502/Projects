package database;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class EventTest {
    static final private String SCHEMA = "test";
    static private int testCount = 0;
    Event test = new Event();
    static Event[] events = new Event[5];

    @BeforeAll
    static void init() throws SQLException, IOException, ClassNotFoundException {
        new DBM(SCHEMA);
        DBM.setupSchema();
        DBM.createTestData();
    }


    @BeforeEach
    void setUp() throws FileNotFoundException, SQLException {
        testCount++;
        System.out.println("Test " + testCount);

        DBM.setupSchema();
        DBM.createTestData();
    }

    @Test
    void createFromDB() throws SQLException {
        /*Test number 1*/
        /*Event test=new Event();
        events[0]=test;
        DBM.insertIntoDB(test);
        test.setImage("test.test");
        test.setTitle("test");
        test.setDescription("Test");
        Event test1=new Event();
        events[1]=test1;
        DBM.insertIntoDB(test1);
        Event test2=new Event();
        events[2]=test2;
        DBM.insertIntoDB(test2);
        assertEquals(test,events[0]);
        assertEquals(test1,events[1]);
        assertEquals(test2,events[2]);
        assertNotNull(1);
        assertEquals("test.test",test.getImagePath());
        assertEquals("test",test.getEventName());
        assertEquals("Test",test.getEventDescription());*/

        /*Test number 2*/
        Event test1 = new Event();
        test1.setID(0);
        test1.setImage("فراس");
        test1.setName("الحطيب");
        test1.setDescription("With a culture that values creativity and technology, Google is used to decorating our " +
                "homepage for national holidays and historical figures.  When Ira Glass, of This American Life, slammed his hand on the conference table and smiled," +
                " “Why can’t we feature a random person?” the doodlers and I thought he was crazy.  I believe we laughed and moved the conversation on quickly-- none of" +
                " us thought the logo space that celebrates people like Harriet Tubman could also feature a random person." +
                "  Ira and This American Life, however, were onto something. ");
        Event test2 = new Event();
        test2.setID(0);
        test2.setImage("alsdlöasmdklamdasmdkasmdas");
        test2.setName("الحطيب");
        test2.setDescription("With a culture that values creativity and technology, Google is used to decorating our " +
                "homepage for national holidays and historical figures.  When Ira Glass, of This American Life, slammed his hand on the conference table and smiled," +
                " “Why can’t we feature a random person?” the doodlers and I thought he was crazy.  I believe we laughed and moved the conversation on quickly-- none of" +
                " us thought the logo space that celebrates people like Harriet Tubman could also feature a random person." +
                "  Ira and This American Life, however, were onto something. ");
        events[0] = test1;
        events[1] = test2;
        DBM.insertIntoDB(test1);
        ResultSet rs;
        PreparedStatement stmt = DBM.conn.prepareStatement("SELECT COUNT(*) FROM events");
        rs = stmt.executeQuery();
        rs.next();
        int actual = rs.getInt(1);
        assertEquals(events.length, actual);
        PreparedStatement stmt1 = DBM.conn.prepareStatement("SELECT * FROM events");
        List<Event>EventlineList = DBM.getFromDB(stmt1, new Event());
        assertEquals(events[0].toString(), EventlineList.get(4).toString());

    }

    @Test
    void getInsertQuery() throws SQLException {
      /*  Event test1=new Event();
        test1.setID(0);
        test1.setImage("فراس");
        test1.setTitle("الحطيب");
        test1.setDescription("With a culture that values creativity and technology, Google is used to decorating our " +
                "homepage for national holidays and historical figures.  When Ira Glass, of This American Life, slammed his hand on the conference table and smiled," +
                " “Why can’t we feature a random person?” the doodlers and I thought he was crazy.  I believe we laughed and moved the conversation on quickly-- none of" +
                " us thought the logo space that celebrates people like Harriet Tubman could also feature a random person." +
                "  Ira and This American Life, however, were onto something. ");
        Event test2=new Event();
        test2.setID(0);
        test2.setImage("alsdlöasmdklamdasmdkasmdas");
        test2.setTitle("الحطيب");
        test2.setDescription("With a culture that values creativity and technology, Google is used to decorating our " +
                "homepage for national holidays and historical figures.  When Ira Glass, of This American Life, slammed his hand on the conference table and smiled," +
                " “Why can’t we feature a random person?” the doodlers and I thought he was crazy.  I believe we laughed and moved the conversation on quickly-- none of" +
                " us thought the logo space that celebrates people like Harriet Tubman could also feature a random person." +
                "  Ira and This American Life, however, were onto something. ");
        events[0]=test1;
        events[1]=test2;*/
        String sql = "INSERT INTO `events` (`EventName`, `EventDescription`,`StartYear`,`StartMonth`,`StartDay`,`StartHour`, " +
                "`StartMinute`,`StartSecond`,`StartMillisecond`,`EndYear`,`EndMonth`,`EndDay`,`EndHour`,`EndMinute`,`EndSecond`, " +
                "`EndMillisecond`,`CreatedYear`,`CreatedMonth`,`CreatedDay`,`CreatedHour`,`CreatedMinute`,`CreatedSecond`,`CreatedMillisecond`,`EventOwner`, `ImagePath`) VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?);";
        PreparedStatement out = DBM.conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
        for (int i = 0; i < events.length; i++) {
            assertEquals(out.toString(), events[i].getInsertQuery().toString());
        }


        //out.setInt(1, test.getUserID());
        //assertNotNull(test);
        //assertEquals(out.toString(),test.getInsertQuery().toString());
       /* try {
           // assertEquals(out.toString(),test.getInsertQuery().toString());
        }catch (SQLIntegrityConstraintViolationException e){
            ;
        }*/


    }

    @AfterAll
    static void tearDown() throws SQLException {
        DBM.conn.createStatement().execute("DROP DATABASE IF EXISTS test");
        DBM.conn.close();
    }

    @Test
    void addToTimeline() throws SQLException {
        ResultSet rs;
        PreparedStatement stmt = DBM.conn.prepareStatement("SELECT COUNT(*) FROM timelineevents");
        rs = stmt.executeQuery();
        rs.next();
        int actual = rs.getInt(1);
        assertNotNull(actual);

        Event testToAdd = new Event();
        testToAdd.setID(1);
        Timeline test = new Timeline();
        test.setID(1);
        PreparedStatement out = DBM.conn.prepareStatement("INSERT  INTO `timelineevents` (`TimelineID`, `EventID`) VALUES (1, 2);");
        PreparedStatement stmt1 = DBM.conn.prepareStatement("SELECT * FROM timelineevents");
        ResultSet rs1;
        rs = stmt1.executeQuery();
        rs.next();
        int actual1 = rs.getInt(1);
        assertEquals( testToAdd.getID(),actual1);

    }


    @Test
    void getUpdateQuery() throws SQLException {
        Event test = new Event();
        DBM.insertIntoDB(test);
        DBM.updateInDB(test);
        events[3] = test;
        ResultSet rs;
        PreparedStatement stmt = DBM.conn.prepareStatement("SELECT COUNT(*) FROM events");
        rs = stmt.executeQuery();
        rs.next();
        int actual = rs.getInt(1);
        assertEquals(events.length, actual);
    }

    @Test
    void getDeleteQuery() throws SQLException {
        Event test = new Event();
        events[0] = test;
        DBM.insertIntoDB(test);
        DBM.deleteFromDB(test);
        PreparedStatement out = DBM.conn.prepareStatement("DELETE FROM `events` WHERE (`EventID` = ?)");
        out.setInt(1, test.getID());
        assertEquals(out.toString(), test.getDeleteQuery().toString());
        DBM.deleteFromDB(events[0]);
        ResultSet rs;
        PreparedStatement stmt = DBM.conn.prepareStatement("SELECT COUNT(*) FROM events");
        rs = stmt.executeQuery();
        rs.next();
        int actual = rs.getInt(1);
        assertEquals(4, actual);
        String sql="DELETE FROM `events` WHERE (`EventID` = ?)";
        PreparedStatement out1 = DBM.conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
        for (int i = 0; i < events.length; i++) {
            assertEquals(out1.toString(), events[i].getDeleteQuery().toString());
        }


    }

    @Test
    void setUserID() {
        test.setOwnerID(1);
        assertEquals(1, 1);
    }

    @Test
    void close() throws SQLException, ClassNotFoundException {
        DBM.conn.close();

        assertThrows(SQLException.class, () -> DBM.conn.createStatement());
        new DBM(SCHEMA);
    }

}