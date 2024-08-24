package controllers;

import database.DBM;
import database.Timeline;
import database.User;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.testfx.api.FxRobot;
import org.testfx.framework.junit5.ApplicationExtension;
import org.testfx.framework.junit5.Start;

import java.io.FileNotFoundException;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.SQLIntegrityConstraintViolationException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Semaphore;

import static org.junit.jupiter.api.Assertions.*;

//focusing on rating testing for now, probably lacking a ton of testing
@ExtendWith(ApplicationExtension.class)
class TimelineCellTest {
    static private int testCount = 0;
    Dashboard dash;
    TimelineCell tc;
    FxRobot robot = new FxRobot();
    int loginUserID;

    @BeforeAll
    public static void beforeAll() {
        new DBM("test");
    }

    @Start
    public void start(Stage stage) throws Exception {
        System.out.println("===========================================================================");
        // Makes each test easier to distinguish in console view
        System.out.println("Test " + ++testCount);
        DBM.setupSchema();
        DBM.createTestData();

        try {
            PreparedStatement stat = DBM.conn.prepareStatement("SELECT * FROM Users WHERE UserID=?");
            stat.setInt(1, 14);
            GUIManager.loggedInUser = DBM.getFromDB(stat, new User()).get(0);
            loginUserID = GUIManager.loggedInUser.getID();
        } catch (SQLException e) {
            System.out.println("Could not get test user from database");
        }

        FXMLLoader loader = new FXMLLoader(getClass().getResource("../../classes/FXML/Dashboard.fxml"));
        GUIManager.mainStage = stage;
        stage.setScene(new Scene(loader.load()));
        dash = loader.getController();
        GUIManager.mainStage = stage;
        stage.show();
    }

    @BeforeEach
    void setUp() throws FileNotFoundException, SQLException {
        DBM.setupSchema();
        DBM.createTestData();
        System.out.println("Test " + ++testCount);
    }

    @AfterAll
    static void end() throws SQLException {
        DBM.conn.createStatement().execute("DROP DATABASE IF EXISTS test");
        DBM.conn.close();
    }

    @Test
    void noOwnRatingTest() throws SQLException {
        GUIManager.loggedInUser.setID(2);
        Timeline user2Timeline = new Timeline();
        user2Timeline.setOwner(GUIManager.loggedInUser);
        double ratingBefore = user2Timeline.getRating();
        assertThrows(SQLIntegrityConstraintViolationException.class, () -> {
            user2Timeline.addRating(5, 2);
        });
        double ratingAfter = user2Timeline.getRating();
        assertEquals(ratingBefore, ratingAfter);

    }

    @Test
    void EmptyRatingInDBTest() throws SQLException {
        GUIManager.mainPane = new BorderPane(); // Avoids a null pointer?
        ArrayList<Timeline> timelinesList = new ArrayList<>(dash.list.getItems());
        int listSize = timelinesList.size();
        for (int i = 0; i < listSize - 1; i++) {
            dash.list.getSelectionModel().select(i);
            Timeline timelineSelected = dash.list.getSelectionModel().getSelectedItem();
            double actual = timelineSelected.getRating();
            double expected = 0;
            assertEquals(actual, expected);
        }
    }

    @Test
    void RatingTest() throws SQLException {
        // Rating 1-5 for each timeline
        GUIManager.mainPane = new BorderPane(); // Avoids a null pointer?
        ArrayList<Timeline> timelinesList = new ArrayList<>(dash.list.getItems());
        int listSize = timelinesList.size();
        for (int i = 0; i < listSize - 1; i++) {
            for (int j = 1; j < 5; j++) {
                dash.list.getSelectionModel().clearAndSelect(i);
                Timeline timelineSelected = dash.list.getSelectionModel().getSelectedItem();
                timelineSelected.addRating(j, 1);
                double actual = timelineSelected.getRating();
                double expected = 0;
                assertEquals(actual, expected);
            }
        }
    }

    @Test
    void ratingInDB() throws SQLException {
        int expectedDB = DBM.getFromDB(DBM.conn.prepareStatement("SELECT COUNT(*) FROM ratings "), rs -> rs.getInt(1))
                .get(0) + 1;
        dash.list.getSelectionModel().clearAndSelect(3);
        Timeline timelineSelected = dash.list.getSelectionModel().getSelectedItem();
        timelineSelected.addRating(3, 1);
        int actualDB = DBM.getFromDB(DBM.conn.prepareStatement("SELECT COUNT(*) FROM ratings "), rs -> rs.getInt(1))
                .get(0);
        assertEquals(expectedDB, actualDB);
    }

    @Test
    void updateTest() {
        // I dont think this is working the way it is supposed to
        // Should not be able to have a bg image of 0 width
        for (int i = 0; i < 500; i++) {
            TimelineCell testingCell = new TimelineCell();
            testingCell.update();
        }
    }

    @Test
    void getTimelineTest() throws InterruptedException, SQLException {
        // need to find a way to iterate over all timelinecells
        // ArrayList<Timeline> timelinesList = new ArrayList<>(dash.list.getItems());
        int listSize = dash.list.getItems().size();
        for (int i = 0; i < listSize - 1; i++) {
            dash.list.getSelectionModel().select(i);
            Timeline timelineSelected = dash.list.getSelectionModel().getSelectedItem();
            String actual = timelineSelected.getName();
            String expected = dash.list.getSelectionModel().getSelectedItem().getName();
            assertTrue(expected.contains(actual));
        }
    }


    /* ********** Helper Methods ****************/
    // Helper methods to make changing GUI elements possible
    void testingCellsSetup() throws SQLException {
        // not functional at all
        TimelineCell tc1 = new TimelineCell();
        List<Timeline> timelineList = DBM.getFromDB(
                DBM.conn.prepareStatement("SELECT * FROM timelines WHERE TimelineID = " + 1), new Timeline());
        tc1.setTimeline(timelineList.get(0));
    }

    void changeSortBy(int selection) throws InterruptedException {
        Platform.runLater(() -> dash.sortBy.getSelectionModel().clearAndSelect(selection));
        waitForRunLater();
    }

    void waitForRunLater() throws InterruptedException {
        Semaphore semaphore = new Semaphore(0);
        Platform.runLater(semaphore::release);
        semaphore.acquire();
    }

    // Helper methods to change who is logged in
    static void setAdminLoggedIn(boolean admin) {
        GUIManager.loggedInUser.setAdmin(admin);
    }

    void reinitializeDashboard() throws InterruptedException {
        Platform.runLater(() -> dash.initialize());
        waitForRunLater();
    }

}
