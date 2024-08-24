package controllers;

import database.DBM;
import database.Event;
import database.Timeline;
import database.User;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.ButtonType;
import javafx.scene.control.DialogPane;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;
import javafx.stage.Window;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.testfx.api.FxRobot;
import org.testfx.framework.junit5.ApplicationExtension;
import org.testfx.framework.junit5.Start;

import java.io.File;
import java.io.IOException;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Semaphore;
import org.apache.commons.io.FileUtils;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(ApplicationExtension.class)
public class DashboardTest {
    static private int testCount = 0;
    Dashboard sut;
    FxRobot robot = new FxRobot();
    User loginUser;
    static File source = new File("src/main/resources/images/timeline");
    static File dest = new File("src/test/testImages");

    @BeforeAll
    public static void beforeAll() {
        new DBM("test");

        try {
            FileUtils.copyDirectory(source, dest);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @AfterEach
    public void resetTest() {
        try {
            FileUtils.copyDirectory(dest, source);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    @Start
    public void start(Stage stage) throws Exception {
        System.out.println("===========================================================================");  //Makes each test easier to distinguish in console view
        System.out.println("Test " + ++testCount);
        DBM.setupSchema();
        DBM.createTestData();

        try {
            PreparedStatement stat = DBM.conn.prepareStatement("SELECT * FROM Users WHERE UserID=?");
            stat.setInt(1, 14);
            GUIManager.loggedInUser =  DBM.getFromDB(stat, new User()).get(0);
            loginUser = GUIManager.loggedInUser;
        } catch (SQLException e) { System.out.println("Could not get test user from database"); }

        GUIManager.mainStage = stage;
        FXMLLoader loader = new FXMLLoader(getClass().getResource("../../classes/FXML/Dashboard.fxml"));
        GUIManager.mainStage = stage;
        stage.setScene(new Scene(loader.load()));
        sut = loader.getController();
        stage.show();
    }

    @AfterAll
    static void end() throws SQLException {
        DBM.conn.createStatement().execute("DROP DATABASE IF EXISTS test");
        DBM.conn.close();
    }


    @Test
    void testSortTimelinesAlphabetically() throws InterruptedException {
        Timeline higherTimelineOnList;
        Timeline lowerTimelineOnList;

        changeSortBy(0);    //Select sort alphabetically
        ArrayList<Timeline> timelinesList = new ArrayList<>(sut.list.getItems());

        for (int i = 0; i < timelinesList.size() - 1; i++) {  //For each timeline on the list except the last one,
            higherTimelineOnList = timelinesList.get(i);
            lowerTimelineOnList = timelinesList.get(i + 1);

            assertTrue(higherTimelineOnList.getName().compareTo(lowerTimelineOnList.getName()) <= 0);   //assert that the one below it comes after alphabetically by name, or is the same
        }

        for (Timeline t : timelinesList)
            System.out.println(t.getName());
    }

    @Test
    void testSortTimelinesAlphabeticallyAfterAddingNewTimelines() throws InterruptedException {
        Timeline higherTimelineOnList;
        Timeline lowerTimelineOnList;

        int initialListSize = sut.list.getItems().size();

        addNewTimelineToDBByName("", "abcd", "ABCD", "1234", "!@#$", "åöäå§");

        reinitializeDashboard();

        int finalListSize = sut.list.getItems().size();

        changeSortBy(0);    //Select sort alphabetically
        ArrayList<Timeline> timelinesList = new ArrayList<>(sut.list.getItems());

        for (int i = 0; i < timelinesList.size() - 1; i++) {  //For each timeline on the list except the last one,
            higherTimelineOnList = timelinesList.get(i);
            lowerTimelineOnList = timelinesList.get(i + 1);

            assertTrue(higherTimelineOnList.getName().compareToIgnoreCase(lowerTimelineOnList.getName()) <= 0);   //assert that the one below it comes after alphabetically by name, or is the same
        }

        int expected = 6;
        int actual = finalListSize - initialListSize;
        assertEquals(expected, actual); //Checks to make sure that the Timelines were actually added to the list.

        for (Timeline t : timelinesList)
            System.out.println(t.getName());
    }

    @Test
    void testSortTimelinesAlphabeticallyAfterRemovingSomeTimelines() throws InterruptedException {
        Timeline higherTimelineOnList;
        Timeline lowerTimelineOnList;

        int initialListSize = sut.list.getItems().size();

        try {
            DBM.deleteFromDB(sut.list.getItems().get(0));
            DBM.deleteFromDB(sut.list.getItems().get(3));
            DBM.deleteFromDB(sut.list.getItems().get(1));
            DBM.deleteFromDB(sut.list.getItems().get(4));
        }
        catch (SQLException e) {e.printStackTrace();}

        reinitializeDashboard();

        int finalListSize = sut.list.getItems().size();

        changeSortBy(0);    //Select sort alphabetically
        ArrayList<Timeline> timelinesList = new ArrayList<>(sut.list.getItems());

        for (int i = 0; i < timelinesList.size() - 1; i++) {  //For each timeline on the list except the last one,
            higherTimelineOnList = timelinesList.get(i);
            lowerTimelineOnList = timelinesList.get(i + 1);

            assertTrue(higherTimelineOnList.getName().compareTo(lowerTimelineOnList.getName()) <= 0);   //assert that the one below it comes after alphabetically by name, or is the same
        }

        int expected = -4;
        int actual = finalListSize - initialListSize;
        assertEquals(expected, actual);         //Makes sure that the Timelines were removed

        for (Timeline t : timelinesList)
            System.out.println(t.getName());
    }

    @Test
    void testSortTimelinesAlphabeticallyAfterRemovingAllTimelines() throws InterruptedException {
        Timeline higherTimelineOnList;
        Timeline lowerTimelineOnList;

        //int initialListSize = sut.list.getItems().size();

        try {
            for (Timeline t : sut.list.getItems())
                DBM.deleteFromDB(t);
        } catch (SQLException e) {e.printStackTrace();}

        reinitializeDashboard();

        int finalListSize = sut.list.getItems().size();

        changeSortBy(0);    //Select sort alphabetically
        ArrayList<Timeline> timelinesList = new ArrayList<>(sut.list.getItems());

        for (int i = 0; i < timelinesList.size() - 1; i++) {  //For each timeline on the list except the last one,
            higherTimelineOnList = timelinesList.get(i);
            lowerTimelineOnList = timelinesList.get(i + 1);

            assertTrue(higherTimelineOnList.getName().compareTo(lowerTimelineOnList.getName()) <= 0);   //assert that the one below it comes after alphabetically by name, or is the same
        }

        int expected = 0;
        int actual = finalListSize;
        assertEquals(expected, actual);         //Makes sure that the Timelines were removed
    }

    @Test
    void testSortTimelinesAlphabeticallyAfterAddingAndRemovingTimelines() throws InterruptedException {
        Timeline higherTimelineOnList;
        Timeline lowerTimelineOnList;

        int initialListSize = sut.list.getItems().size();

        addNewTimelineToDBByName("", "abcd", "ABCD", "1234", "!@#$", "åöäå§");

        try {
            DBM.deleteFromDB(sut.list.getItems().get(0));
            DBM.deleteFromDB(sut.list.getItems().get(3));
            DBM.deleteFromDB(sut.list.getItems().get(1));
            DBM.deleteFromDB(sut.list.getItems().get(4));
        }
        catch (SQLException e) {e.printStackTrace();}

        addNewTimelineToDBByName("", "1234", "!@#$", "☺☻♥♦♣♠", "ÖÄÅåöäå§");

        try {
            DBM.deleteFromDB(sut.list.getItems().get(5));
            DBM.deleteFromDB(sut.list.getItems().get(6));
        }
        catch (SQLException e) {e.printStackTrace();}

        reinitializeDashboard();

        int finalListSize = sut.list.getItems().size();

        changeSortBy(0);    //Select sort alphabetically
        ArrayList<Timeline> timelinesList = new ArrayList<>(sut.list.getItems());

        for (int i = 0; i < timelinesList.size() - 1; i++) {  //For each timeline on the list except the last one,
            higherTimelineOnList = timelinesList.get(i);
            lowerTimelineOnList = timelinesList.get(i + 1);

            assertTrue(higherTimelineOnList.getName().compareToIgnoreCase(lowerTimelineOnList.getName()) <= 0);   //assert that the one below it comes after alphabetically by name, or is the same
        }

        int expected = 5;
        int actual = finalListSize - initialListSize;
        assertEquals(expected, actual);         //Makes sure that the Timelines were added and removed

        for (Timeline t : timelinesList)
            System.out.println(t.getName());
    }

    @Test
    void testSortTimelinesReverseAlphabetically() throws InterruptedException {
        Timeline higherTimelineOnList;
        Timeline lowerTimelineOnList;

        changeSortBy(1);    //Select sort reverse alphabetically
        ArrayList<Timeline> timelinesList = new ArrayList<>(sut.list.getItems());

        for (int i = 0; i < timelinesList.size() - 1; i++) {  //For each timeline on the list except the last one,
            higherTimelineOnList = timelinesList.get(i);
            lowerTimelineOnList = timelinesList.get(i + 1);

            assertTrue(higherTimelineOnList.getName().compareTo(lowerTimelineOnList.getName()) >= 0);   //assert that the one below it comes before alphabetically by name, or is the same
        }

        for (Timeline t : timelinesList)
            System.out.println(t.getName());
    }

    @Test
    void testSortTimelinesDateCreatedNewestFirst() throws InterruptedException {
        Timeline higherTimelineOnList;
        Timeline lowerTimelineOnList;

        changeSortBy(2);    //Select sort date created, newest first
        ArrayList<Timeline> timelinesList = new ArrayList<>(sut.list.getItems());

        for (int i = 0; i < timelinesList.size() - 1; i++) {  //For each timeline on the list except the last one,
            higherTimelineOnList = timelinesList.get(i);
            lowerTimelineOnList = timelinesList.get(i + 1);

            assertTrue(higherTimelineOnList.getCreationDate().compareTo(lowerTimelineOnList.getCreationDate()) >= 0);   //assert that the one below it was created later, or at the same time
        }

        for (Timeline t : timelinesList)
            System.out.println(t.getName());
    }

    @Test
    void testSortTimelinesDateCreatedOldestFirst() throws InterruptedException {
        Timeline higherTimelineOnList;
        Timeline lowerTimelineOnList;

        changeSortBy(3);    //Select sort date created, oldest first
        ArrayList<Timeline> timelinesList = new ArrayList<>(sut.list.getItems());

        for (int i = 0; i < timelinesList.size() - 1; i++) {  //For each timeline on the list except the last one,
            higherTimelineOnList = timelinesList.get(i);
            lowerTimelineOnList = timelinesList.get(i + 1);

            assertTrue(higherTimelineOnList.getCreationDate().compareTo(lowerTimelineOnList.getCreationDate()) <= 0);   //assert that the one below it was created sooner, or at the same time
        }

        for (Timeline t : timelinesList)
            System.out.println(t.getName());
    }

    @Test
    void testOnlyViewPersonalTimelines() throws InterruptedException {
        addNewTimelineToDBByOwnerId(loginUser, loginUser, loginUser);

        Platform.runLater(() -> {
            sut.initialize();
            sut.checkboxOnlyViewPersonalLines.setSelected(true);
        });
        waitForRunLater();

        ArrayList<Timeline> timelinesList = new ArrayList<>(sut.list.getItems());

        int actual;
        int expected;
        for (Timeline timeline : timelinesList) {
            expected = GUIManager.loggedInUser.getID();
            actual = timeline.getOwnerID();

            assertEquals(expected, actual);
        }

        expected = 3;
        actual = sut.list.getItems().size();

        assertEquals(expected, actual);    //Makes sure that the timelines are on the list
    }

    @Test
    void testViewPersonalChangeSortMethodThenViewAllTimelines() throws InterruptedException {
        addNewTimelineToDBByOwnerId(loginUser, loginUser, loginUser);    //Add some new timelines to the list

        Platform.runLater(() -> {       //View only personal timelines
            sut.initialize();
            sut.checkboxOnlyViewPersonalLines.setSelected(true);
        });
        waitForRunLater();

        changeSortBy(1);    //Change the sorting method

        ArrayList<Timeline> timelinesList = new ArrayList<>(sut.list.getItems());
        int initialListSize = timelinesList.size(); //Keep track of how many timelines are currently in the list

        int actual;
        int expected;
        Timeline higherTimelineOnList;

        for (int i = 0; i < timelinesList.size(); i++) {
            higherTimelineOnList = timelinesList.get(i);

            expected = GUIManager.loggedInUser.getID();
            actual = higherTimelineOnList.getOwnerID();
            assertEquals(expected, actual);     //Check that the timelines are owned by the user

            if (i != timelinesList.size() - 1)  //Don't compare the last one to avoid null pointer
                assertTrue(higherTimelineOnList.getName().compareTo(timelinesList.get(i + 1).getName()) >= 0);   //assert that the one below it comes before alphabetically by name, or is the same
        }

        expected = 3;
        actual = sut.list.getItems().size();
        assertEquals(expected, actual);    //Make sure that only the User's timelines are on the list

        Platform.runLater(() -> {       //View all timelines again
            sut.checkboxOnlyViewPersonalLines.setSelected(false);
        });
        waitForRunLater();

        Timeline lowerTimelineOnList;
        for (int i = 0; i < timelinesList.size() - 1; i++) {
            higherTimelineOnList = timelinesList.get(i);
            lowerTimelineOnList = timelinesList.get(i + 1);

            assertTrue(higherTimelineOnList.getName().compareTo(lowerTimelineOnList.getName()) >= 0);   //assert that the one below it comes before alphabetically by name, or is the same
        }

        expected = initialListSize;
        actual = timelinesList.size();
        assertEquals(expected, actual);     //Check that all the timelines are being shown
    }

    @Test
    //Using a robot to click on buttons is the best way I found to test alerts and popups, but sometimes the tests give a false negative.
    //If any test using a robot fails, rerun it, it almost always works the second time.
    void testDeleteTimelineConfirm() throws InterruptedException {
        GUIManager.loggedInUser.setAdmin(true);
        addNewTimelineToDBByOwnerId(loginUser);
        reinitializeDashboard();

        //Select the first timeline in the list that has an owner ID of the logged in user
        sut.list.getSelectionModel().select(sut.list.getItems().stream().filter(t -> t.getOwnerID() == loginUser.getID()).findFirst().get());
        int initialListSize = sut.list.getItems().size();

        Platform.runLater(() -> robot.clickOn("#btnDelete"));
        waitForRunLater();
        Platform.runLater(() -> {
            DialogPane popup = getDialogPane();
            robot.clickOn(popup.lookupButton(ButtonType.OK));
        });
        waitForRunLater();

        reinitializeDashboard();
        int expected = initialListSize - 1; //Check that it was actually deleted
        int actual = sut.list.getItems().size();
        assertEquals(expected, actual);
    }

    @Test
    //Using a robot to click on buttons is the best way I found to test alerts and popups, but sometimes the tests give a false negative.
    //If any test using a robot fails, rerun it, it almost always works the second time.
    void testDeleteTimelineClose() throws InterruptedException {
        GUIManager.loggedInUser.setAdmin(true);
        addNewTimelineToDBByOwnerId(loginUser);
        reinitializeDashboard();

        //Select the first timeline in the list that has an owner ID of the logged in user
        Platform.runLater(() -> sut.list.getSelectionModel().select(sut.list.getItems().stream().filter(t -> t.getOwnerID() == loginUser.getID()).findFirst().get()));
        int initialListSize = sut.list.getItems().size();

        Platform.runLater(() -> robot.clickOn("#btnDelete"));
        waitForRunLater();
        Platform.runLater(() -> {
            DialogPane popup = getDialogPane();
            robot.clickOn(popup.lookupButton(ButtonType.CANCEL));
        });
        waitForRunLater();

        reinitializeDashboard();
        int expected = initialListSize; //Check that the timeline is still in the list
        int actual = sut.list.getItems().size();
        assertEquals(expected, actual);
    }

    @Test
    void testCreateTimelineButton() throws InterruptedException {
        GUIManager.mainPane = new BorderPane(); //Avoids a null pointer
        GUIManager.loggedInUser.setAdmin(true);
        addNewTimelineToDBByOwnerId(loginUser);
        reinitializeDashboard();
        //Select the first timeline in the list that has an owner ID of the logged in user to ensure that the new timeline doesn't get overridden in edit mode
        sut.list.getSelectionModel().select(sut.list.getItems().stream().filter(t -> t.getOwnerID() == loginUser.getID()).findFirst().get());


        Platform.runLater(() -> {
            TimelineView testView = sut.createTimeline();
            assertTrue(testView.timelineEditorController.editable); //Makes sure that the create timeline screen starts in edit mode.


            //Check all timeline attributes to make sure that it is a blank timeline
            String actualString = testView.activeTimeline.getName();
            String expectedString = "New Timeline";
            assertEquals(expectedString, actualString);

            actualString = testView.activeTimeline.getDescription();
            expectedString = "";
            assertEquals(expectedString, actualString);

            int actualInt = testView.activeTimeline.getOwnerID();
            int expectedInt = loginUser.getID();
            assertEquals(expectedInt, actualInt);

            actualInt = testView.activeTimeline.getKeywords().size();
            expectedInt = 0;
            assertEquals(expectedInt, actualInt);

            actualInt = testView.activeTimeline.getScale();
            expectedInt = 0;
            assertEquals(expectedInt, actualInt);

            actualInt = testView.activeTimeline.getEventList().size();
            expectedInt = 0;
            assertEquals(expectedInt, actualInt);

        });
        waitForRunLater();
    }

    @Test
    void testEditTimelineButton() throws InterruptedException {
        GUIManager.mainPane = new BorderPane(); //Avoids a null pointer
        GUIManager.loggedInUser.setAdmin(true);

        Timeline newTimeline = new Timeline();
        newTimeline.setOwner(loginUser);
        newTimeline.setName("Name");
        newTimeline.setDescription("Description");
        newTimeline.getKeywords().add("Keyword");
        newTimeline.setScale(3);
        try {DBM.insertIntoDB(newTimeline);} catch (SQLException e) {e.printStackTrace();}

        Event testEvent = new Event();
        testEvent.setOwnerID(loginUser.getID());
        try {DBM.insertIntoDB(testEvent);} catch (SQLException e) {e.printStackTrace();}
        try {testEvent.addToTimeline(newTimeline.getID());} catch (SQLException e) {e.printStackTrace();}

        reinitializeDashboard();

        Platform.runLater(() -> {
            //Select the first timeline in the list that has an owner ID of the logged in user
            sut.list.getSelectionModel().select(sut.list.getItems().stream().filter(t -> t.getOwnerID() == loginUser.getID()).findFirst().get());

//            TimelineView testView = sut.editTimeline();
//            assertTrue(testView.timelineEditorController.editable); //Makes sure that the edit timeline screen starts in edit mode.
//
//            //Check all timeline attributes to make sure that it is the proper timeline
//            String actualString = testView.activeTimeline.getName();
//            String expectedString = "Name";
//            assertEquals(expectedString, actualString);
//
//            actualString = testView.activeTimeline.getDescription();
//            expectedString = "Description";
//            assertEquals(expectedString, actualString);
//
//            int actualInt = testView.activeTimeline.getOwnerID();
//            int expectedInt = loginUser.getID();
//            assertEquals(expectedInt, actualInt);
//
//            actualInt = testView.activeTimeline.getKeywords().size();
//            expectedInt = 1;
//            assertEquals(expectedInt, actualInt);
//
//            actualInt = testView.activeTimeline.getScale();
//            expectedInt = 3;
//            assertEquals(expectedInt, actualInt);
//
//            actualInt = testView.activeTimeline.getEventList().size();
//            expectedInt = 1;
//            assertEquals(expectedInt, actualInt);
        });

        waitForRunLater();
    }

    @Test
    void testViewTimelineButton() throws InterruptedException {
        GUIManager.mainPane = new BorderPane(); //Avoids a null pointer
        GUIManager.loggedInUser.setAdmin(false);    //Makes sure that non admins can still view timelines

        //Select the first timeline in the list
        sut.list.getSelectionModel().select(0);
        Timeline timelineSelected = sut.list.getSelectionModel().getSelectedItem();

        Platform.runLater(() -> {
           // TimelineView testView = sut.openTimeline();
           // assertFalse(testView.timelineEditorController.editable); //Makes sure that the view timeline screen doesn't start in edit mode.
//
           // //Check all timeline attributes to make sure that it is a blank timeline
           // String actualString = testView.activeTimeline.getName();
           // String expectedString = timelineSelected.getName();
           // assertEquals(expectedString, actualString);
//
           // actualString = testView.activeTimeline.getDescription();
           // expectedString = timelineSelected.getDescription();
           // assertEquals(expectedString, actualString);
//
           // int actualInt = testView.activeTimeline.getOwnerID();
           // int expectedInt = timelineSelected.getOwnerID();
           // assertEquals(expectedInt, actualInt);
//
           // actualInt = testView.activeTimeline.getKeywords().size();
           // expectedInt = timelineSelected.getKeywords().size();
           // assertEquals(expectedInt, actualInt);
//
           // actualInt = testView.activeTimeline.getScale();
           // expectedInt = timelineSelected.getScale();
           // assertEquals(expectedInt, actualInt);
//
           // actualInt = testView.activeTimeline.getEventList().size();
           // expectedInt = timelineSelected.getEventList().size();
           // assertEquals(expectedInt, actualInt);
        });
        waitForRunLater();
    }

    @Test
    void testSimpleSearchByTimelineName() throws InterruptedException {
        addNewTimelineToDBByName("Please don't make a timeline with this name it will ruin my tests");
        reinitializeDashboard();

        Platform.runLater(() -> sut.searchInput.setText("me")); //Checks that searching for something in the middle of the name still counts
        waitForRunLater();

        assertTrue(sut.list.getItems().stream().allMatch(timeline -> timeline.getName().toLowerCase().contains(sut.searchInput.getText().toLowerCase())));

        int actual = sut.list.getItems().size();
        int expected = 0;
        assertNotEquals(expected, actual); //If list is empty, then previous assert defaults to true

    }

    @Test
    void testSimpleSearchByTimelineNameCaseInsensitive() throws InterruptedException {
        addNewTimelineToDBByName("Please don't make a timeline with this name it will ruin my tests");
        reinitializeDashboard();

        Platform.runLater(() -> sut.searchInput.setText("ME"));
        waitForRunLater();

        assertTrue(sut.list.getItems().stream().allMatch(timeline -> timeline.getName().toLowerCase().contains(sut.searchInput.getText().toLowerCase())));
        int actual = sut.list.getItems().size();
        int expected = 0;
        assertNotEquals(expected, actual); //If list is empty, then previous assert defaults to true
    }

    @Test
    void testSimpleSearchByWeirdTimelineName() throws InterruptedException {
        addNewTimelineToDBByName("☺☻♥♦♣♠");
        reinitializeDashboard();

        Platform.runLater(() -> sut.searchInput.setText("♦"));
        waitForRunLater();

        assertTrue(sut.list.getItems().stream().allMatch(timeline -> timeline.getName().toLowerCase().contains(sut.searchInput.getText().toLowerCase())));
        int actual = sut.list.getItems().size();
        int expected = 0;
        assertNotEquals(expected, actual); //If list is empty, then previous assert defaults to true
    }

    @Test
    void testSimpleSearchByTimelineNameFailure() throws InterruptedException {
        addNewTimelineToDBByName("Please don't make a timeline with this name it will ruin my tests");
        reinitializeDashboard();

        Platform.runLater(() -> sut.searchInput.setText("Please x")); //Checks that searching for something not in the name actually removes it from the list
        waitForRunLater();

        assertFalse(sut.list.getItems().stream().allMatch(timeline -> timeline.getName().toLowerCase().contains(sut.searchInput.getText().toLowerCase()))
                && sut.list.getItems().size() > 0); //If list is empty, then previous assert defaults to true, put in same assert to avoid false positive);
    }

    @Test
    void testSimpleSearchByKeyWord() throws InterruptedException {
        addNewTimelineToDBByKeyWords("Please don't make a timeline with this keyword it will ruin my tests");
        reinitializeDashboard();

        Platform.runLater(() -> sut.searchInput.setText("t mak")); //Checks that searching for something in the middle of the key word still counts
        waitForRunLater();

        assertTrue(sut.list.getItems().stream().allMatch(timeline -> timeline.getKeywords().stream().anyMatch(keyWord->keyWord.contains(sut.searchInput.getText().toLowerCase()))));
        int actual = sut.list.getItems().size();
        int expected = 0;
        assertNotEquals(expected, actual); //If list is empty, then previous assert defaults to true
    }

    @Test
    void testSimpleSearchByTimelineNamePersonalOnly() throws InterruptedException {
        addNewTimelineToDBByOwnerIdAndName("Please don't make a timeline with this name it will ruin my tests", loginUser);
        Platform.runLater(() -> {
            sut.checkboxOnlyViewPersonalLines.setSelected(true);
            sut.initialize();
            sut.searchInput.setText("me"); //Checks that searching for something in the middle of the name still counts
        });
        waitForRunLater();

        assertTrue(sut.list.getItems().stream().allMatch(timeline -> timeline.getName().toLowerCase().contains(sut.searchInput.getText().toLowerCase())));
        int actual = sut.list.getItems().size();
        int expected = 0;
        assertNotEquals(expected, actual); //If list is empty, then previous assert defaults to true
    }

    @Test
    void testSimpleSearchByTimelineNameCaseInsensitivePersonalOnly() throws InterruptedException {
        addNewTimelineToDBByOwnerIdAndName("Please don't make a timeline with this name it will ruin my tests", loginUser);
        Platform.runLater(() -> {
            sut.checkboxOnlyViewPersonalLines.setSelected(true);
            sut.initialize();
            sut.searchInput.setText("ME");
        });
        waitForRunLater();

        assertTrue(sut.list.getItems().stream().allMatch(timeline -> timeline.getName().toLowerCase().contains(sut.searchInput.getText().toLowerCase())));
        int actual = sut.list.getItems().size();
        int expected = 0;
        assertNotEquals(expected, actual); //If list is empty, then previous assert defaults to true
    }

    @Test
    void testSimpleSearchByWeirdTimelineNamePersonalOnly() throws InterruptedException {
        addNewTimelineToDBByOwnerIdAndName("☺☻♥♦♣♠", loginUser);
        Platform.runLater(() -> {
            sut.checkboxOnlyViewPersonalLines.setSelected(true);
            sut.initialize();
            sut.searchInput.setText("♦");
        });
        waitForRunLater();

        assertTrue(sut.list.getItems().stream().allMatch(timeline -> timeline.getName().toLowerCase().contains(sut.searchInput.getText().toLowerCase())));
        int actual = sut.list.getItems().size();
        int expected = 0;
        assertNotEquals(expected, actual); //If list is empty, then previous assert defaults to true
    }

    @Test
    void testSimpleSearchByTimelineNamePersonalOnlyFailure() throws InterruptedException {
        addNewTimelineToDBByOwnerIdAndName("Please don't make a timeline with this name it will ruin my tests", loginUser);
        Platform.runLater(() -> {
            sut.checkboxOnlyViewPersonalLines.setSelected(true);
            sut.initialize();
            sut.searchInput.setText("Please x"); //Checks that searching for something not in the name actually removes it from the list
        });
        waitForRunLater();

        assertFalse(sut.list.getItems().stream().allMatch(timeline -> timeline.getName().toLowerCase().contains(sut.searchInput.getText().toLowerCase()))
                    && sut.list.getItems().size() > 0); //If list is empty, then previous assert defaults to true, put in same assert to avoid false positive
    }

    @Test
    void testSimpleSearchByTimelineNamePersonalOnlyIDFailure() throws InterruptedException {
        addNewTimelineToDBByOwnerIdAndName("Please don't make a timeline with this name it will ruin my tests", new User());
        Platform.runLater(() -> {
            sut.checkboxOnlyViewPersonalLines.setSelected(true);
            sut.initialize();
            sut.searchInput.setText("Please"); //Checks that searching for something not in the name actually removes it from the list
        });
        waitForRunLater();

        assertFalse(sut.list.getItems().stream().allMatch(timeline -> timeline.getName().toLowerCase().contains(sut.searchInput.getText().toLowerCase()))
                && sut.list.getItems().size() > 0); //If list is empty, then previous assert defaults to true, put in same assert to avoid false positive);
    }

    @Test
    void testSimpleSearchByKeyWordPersonalOnly() throws InterruptedException {
        addNewTimelineToDBByOwnerIdAndKeyword(loginUser,"Please don't make a timeline with this keyword it will ruin my tests");
        Platform.runLater(() -> {
            sut.checkboxOnlyViewPersonalLines.setSelected(true);
            sut.initialize();
            sut.searchInput.setText("t mak"); //Checks that searching for something in the middle of the key word still counts
        });
        waitForRunLater();

        assertTrue(sut.list.getItems().stream().allMatch(timeline -> timeline.getKeywords().stream().anyMatch(keyWord->keyWord.toLowerCase().contains(sut.searchInput.getText().toLowerCase()))));
        int actual = sut.list.getItems().size();
        int expected = 0;
        assertNotEquals(expected, actual); //If list is empty, then previous assert defaults to true
    }

    @Test
    void testSimpleSearchByTimelineNameAndSort() throws InterruptedException {
        addNewTimelineToDBByName("aPlease don't make a timeline with this name it will ruin my tests",
                "bPlease don't make a timeline with this name it will ruin my tests",
                "cPlease don't make a timeline with this name it will ruin my tests",
                "dPlease don't make a timeline with this name it will ruin my tests",
                "ePlease don't make a timeline with this name it will ruin my tests");

        Platform.runLater(() -> {
            sut.initialize();
            sut.searchInput.setText("Please don't make a timeline with this name it will ruin my tests");
            sut.sortBy.getSelectionModel().clearAndSelect(0);    //Alphabetically
        });
        waitForRunLater();

        Timeline higherTimelineOnList;
        Timeline lowerTimelineOnList;
        ArrayList<Timeline> timelinesList = new ArrayList<>(sut.list.getItems());

        for (int i = 0; i < timelinesList.size() - 1; i++) {  //For each timeline on the list except the last one,
            higherTimelineOnList = timelinesList.get(i);
            lowerTimelineOnList = timelinesList.get(i + 1);

            assertTrue(higherTimelineOnList.getName().compareTo(lowerTimelineOnList.getName()) <= 0);   //assert that the one below it comes after alphabetically by name, or is the same
            assertTrue(lowerTimelineOnList.getName().contains("Please don't make a timeline with this name it will ruin my tests"));
        }
        assertTrue(timelinesList.get(0).getName().contains("Please don't make a timeline with this name it will ruin my tests"));

        changeSortBy(1);    //Reverse alphabetically
        timelinesList = new ArrayList<>(sut.list.getItems());

        for (int i = 0; i < timelinesList.size() - 1; i++) {  //For each timeline on the list except the last one,
            higherTimelineOnList = timelinesList.get(i);
            lowerTimelineOnList = timelinesList.get(i + 1);

            assertTrue(higherTimelineOnList.getName().compareTo(lowerTimelineOnList.getName()) >= 0);   //assert that the one below it comes before alphabetically by name, or is the same
            assertTrue(lowerTimelineOnList.getName().contains("Please don't make a timeline with this name it will ruin my tests"));
        }
        assertTrue(timelinesList.get(0).getName().contains("Please don't make a timeline with this name it will ruin my tests"));
    }

    @Test
    void testSimpleSearchByTimelineNameAndSortPersonalOnly() throws InterruptedException {
        addNewTimelineToDBByOwnerIdAndName("aPlease don't make a timeline with this name it will ruin my tests", loginUser);
        addNewTimelineToDBByOwnerIdAndName("bPlease don't make a timeline with this name it will ruin my tests", loginUser);
        addNewTimelineToDBByOwnerIdAndName("cPlease don't make a timeline with this name it will ruin my tests", loginUser);
        addNewTimelineToDBByOwnerIdAndName("dPlease don't make a timeline with this name it will ruin my tests", loginUser);
        addNewTimelineToDBByOwnerIdAndName("ePlease don't make a timeline with this name it will ruin my tests", loginUser);
        addNewTimelineToDBByOwnerIdAndName("aPlease don't make a timeline with this name it will ruin my tests", new User());
        addNewTimelineToDBByOwnerIdAndName("bPlease don't make a timeline with this name it will ruin my tests", new User());
        addNewTimelineToDBByOwnerIdAndName("cPlease don't make a timeline with this name it will ruin my tests", new User());
        addNewTimelineToDBByOwnerIdAndName("dPlease don't make a timeline with this name it will ruin my tests", new User());
        addNewTimelineToDBByOwnerIdAndName("ePlease don't make a timeline with this name it will ruin my tests", new User());

        Platform.runLater(() -> {
            sut.checkboxOnlyViewPersonalLines.setSelected(true);
            sut.initialize();
            sut.searchInput.setText("Please don't make a timeline with this name it will ruin my tests");
            sut.sortBy.getSelectionModel().clearAndSelect(0);    //Alphabetically
        });
        waitForRunLater();

        Timeline higherTimelineOnList;
        Timeline lowerTimelineOnList;
        ArrayList<Timeline> timelinesList = new ArrayList<>(sut.list.getItems());

        for (int i = 0; i < timelinesList.size() - 1; i++) {  //For each timeline on the list except the last one,
            higherTimelineOnList = timelinesList.get(i);
            lowerTimelineOnList = timelinesList.get(i + 1);

            //Thread.sleep(5000);
            System.out.println(higherTimelineOnList);
            System.out.println(lowerTimelineOnList);

            assertTrue(higherTimelineOnList.getName().compareTo(lowerTimelineOnList.getName()) <= 0);   //assert that the one below it comes after alphabetically by name, or is the same
            assertTrue(lowerTimelineOnList.getName().contains("Please don't make a timeline with this name it will ruin my tests"));
            assertEquals(lowerTimelineOnList.getOwnerID(), loginUser);
        }
        assertTrue(timelinesList.get(0).getName().contains("Please don't make a timeline with this name it will ruin my tests"));
        assertEquals(timelinesList.get(0).getOwnerID(), loginUser);

        changeSortBy(1);    //Reverse alphabetically
        timelinesList = new ArrayList<>(sut.list.getItems());

        for (int i = 0; i < timelinesList.size() - 1; i++) {  //For each timeline on the list except the last one,
            higherTimelineOnList = timelinesList.get(i);
            lowerTimelineOnList = timelinesList.get(i + 1);

            assertTrue(higherTimelineOnList.getName().compareTo(lowerTimelineOnList.getName()) >= 0);   //assert that the one below it comes before alphabetically by name, or is the same
            assertTrue(lowerTimelineOnList.getName().contains("Please don't make a timeline with this name it will ruin my tests"));
            assertEquals(lowerTimelineOnList.getOwnerID(), loginUser);

        }
        assertTrue(timelinesList.get(0).getName().contains("Please don't make a timeline with this name it will ruin my tests"));
        assertEquals(timelinesList.get(0).getOwnerID(), loginUser);
    }

    @Test
    void testAdvancedSearchTimelineName() throws InterruptedException {
        addNewTimelineToDBByName("Please don't make a timeline with this name it will ruin my tests");

        Platform.runLater(() -> {
            sut.initialize();
            sut.toggleAdvancedSearch();
            sut.searchTimelineName.setText("t mak");
            sut.searchAdvanced();
        });
        waitForRunLater();

        assertTrue(sut.list.getItems().stream().allMatch(timeline -> timeline.getName().toLowerCase().contains(sut.searchTimelineName.getText().toLowerCase())));
        int actual = sut.list.getItems().size();
        int expected = 0;
        assertNotEquals(expected, actual); //If list is empty, then previous assert defaults to true
    }

    @Test
    void testAdvancedSearchTimelineOwnerName() throws InterruptedException {
        addNewTimelineToDBByOwnerId(GUIManager.loggedInUser);

        Platform.runLater(() -> {
            sut.initialize();
            sut.toggleAdvancedSearch();
            sut.searchCreator.setText(GUIManager.loggedInUser.getUserName());
            sut.searchAdvanced();
        });
        waitForRunLater();

        assertTrue(sut.list.getItems().stream().allMatch(timeline -> timeline.getOwnerID() == GUIManager.loggedInUser.getID()));
        int actual = sut.list.getItems().size();
        int expected = 0;
        assertNotEquals(expected, actual); //If list is empty, then previous assert defaults to true
    }

    @Test
    void testAdvancedSearchTimelineKeyword() throws InterruptedException {
        addNewTimelineToDBByKeyWords("Please don't make a timeline with this keyword it will ruin my tests");

        Platform.runLater(() -> {
            sut.initialize();
            sut.toggleAdvancedSearch();
            sut.searchKeywords.setText("is ke");
            sut.searchAdvanced();
        });
        waitForRunLater();

        assertTrue(sut.list.getItems().stream().allMatch(timeline -> timeline.getKeywords().stream().anyMatch(keyWord->keyWord.contains(sut.searchKeywords.getText().toLowerCase()))));
        int actual = sut.list.getItems().size();
        int expected = 0;
        assertNotEquals(expected, actual); //If list is empty, then previous assert defaults to true
    }

    /*@Test
    void testAdvancedSearchTimelineStartYear() throws InterruptedException {
        addNewTimelineToDBByStartDate(1550, 10, 3, 6, 9, 34, 25);

        Platform.runLater(() -> {
            sut.initialize();
            sut.toggleAdvancedSearch();
            sut.startInputs.get(0).getValueFactory().setValue(1550);
            sut.searchAdvanced();
        });
        waitForRunLater();

        assertTrue(sut.list.getItems().stream().allMatch(timeline -> timeline.getStartDate().getYear() >= 1550));
        int actual = sut.list.getItems().size();
        int expected = 0;
        assertNotEquals(expected, actual); //If list is empty, then previous assert defaults to true
    }

    @Test
    void testAdvancedSearchTimelineEndYear() throws InterruptedException {
        addNewTimelineToDBByEndDate(1550, 10, 3, 6, 9, 34, 25);

        Platform.runLater(() -> {
            sut.initialize();
            sut.toggleAdvancedSearch();
            sut.endInputs.get(0).getValueFactory().setValue(1550);
            sut.searchAdvanced();
        });
        waitForRunLater();

        assertTrue(sut.list.getItems().stream().allMatch(timeline -> timeline.getStartDate().getYear() <= 1550));
        int actual = sut.list.getItems().size();
        int expected = 0;
        assertNotEquals(expected, actual); //If list is empty, then previous assert defaults to true
    }

    @Test
    void testAdvancedSearchTimelineStartMonth() throws InterruptedException {
        addNewTimelineToDBByStartDate(1550, 10, 3, 6, 9, 34, 25);

        Platform.runLater(() -> {
            sut.initialize();
            sut.toggleAdvancedSearch();
            sut.startInputs.get(1).getValueFactory().setValue(10);
            sut.searchAdvanced();
        });
        waitForRunLater();

        for (Timeline t : sut.list.getItems()) {
            System.out.println(t.getStartDate().getMonthValue());
        }

        assertTrue(sut.list.getItems().stream().allMatch(timeline -> timeline.getStartDate().getMonthValue() >= 10));
        int actual = sut.list.getItems().size();
        int expected = 0;
        assertNotEquals(expected, actual); //If list is empty, then previous assert defaults to true
    }

    @Test
    void testAdvancedSearchTimelineStartDay() throws InterruptedException {
        addNewTimelineToDBByStartDate(1550, 10, 30, 6, 9, 34, 25);

        Platform.runLater(() -> {
            sut.initialize();
            sut.toggleAdvancedSearch();
            sut.startInputs.get(2).getValueFactory().setValue(30);
            sut.searchAdvanced();
        });
        waitForRunLater();

        assertTrue(sut.list.getItems().stream().allMatch(timeline -> timeline.getStartDate().getMonthValue() >= 30));
        int actual = sut.list.getItems().size();
        int expected = 0;
        assertNotEquals(expected, actual); //If list is empty, then previous assert defaults to true
    }*/

    @Test
    void testAdvancedSearchTimelineNameAndOwner() throws InterruptedException {
        addNewTimelineToDBByOwnerIdAndName("Please don't make a timeline with this name it will ruin my tests", GUIManager.loggedInUser);

        Platform.runLater(() -> {
            sut.initialize();
            sut.toggleAdvancedSearch();
            sut.searchTimelineName.setText("t mak");
            sut.searchCreator.setText(GUIManager.loggedInUser.getUserName());
            sut.searchAdvanced();
        });
        waitForRunLater();

        assertTrue(sut.list.getItems().stream().allMatch(timeline -> timeline.getName().toLowerCase().contains(sut.searchTimelineName.getText().toLowerCase())));
        assertTrue(sut.list.getItems().stream().allMatch(timeline -> timeline.getOwnerID() == GUIManager.loggedInUser.getID()));
        int actual = sut.list.getItems().size();
        int expected = 0;
        assertNotEquals(expected, actual); //If list is empty, then previous asserts defaults to true
    }

    @Test
    void testAdvancedSearchTimelineNameAndOwnerAndKeyword() throws InterruptedException {
        Timeline newTimeline = new Timeline();
        newTimeline.setOwner(GUIManager.loggedInUser);
        newTimeline.setName("Please don't make a timeline with this name it will ruin my tests");
        newTimeline.getKeywords().add("Please don't make a timeline with this keyword it will ruin my tests");
        try {DBM.insertIntoDB(newTimeline);} catch (SQLException e) {e.printStackTrace();}


        Platform.runLater(() -> {
            sut.initialize();
            sut.toggleAdvancedSearch();
            sut.searchTimelineName.setText("t mak");
            sut.searchCreator.setText(GUIManager.loggedInUser.getUserName());
            sut.searchKeywords.setText("is ke");
            sut.searchAdvanced();
        });
        waitForRunLater();

        assertTrue(sut.list.getItems().stream().allMatch(timeline -> timeline.getName().toLowerCase().contains(sut.searchTimelineName.getText().toLowerCase())));
        assertTrue(sut.list.getItems().stream().allMatch(timeline -> timeline.getOwnerID() == GUIManager.loggedInUser.getID()));
        assertTrue(sut.list.getItems().stream().allMatch(timeline -> timeline.getKeywords().stream().anyMatch(keyWord->keyWord.contains(sut.searchKeywords.getText().toLowerCase()))));
        int actual = sut.list.getItems().size();
        int expected = 0;
        assertNotEquals(expected, actual); //If list is empty, then previous asserts defaults to true
    }



    //Gui elements take some time to load, and the tests will be run before any changes are made.
    //Putting Gui changes in a Platform.runLater lambda and using this method to wait for those changes to be made
    //ensures that the tests are run after the Gui is properly loaded.
    void waitForRunLater() throws InterruptedException {
        Semaphore semaphore = new Semaphore(0);
        Platform.runLater(semaphore::release);
        semaphore.acquire();
    }

    void changeSortBy(int selection) throws InterruptedException {
        Platform.runLater(() -> sut.sortBy.getSelectionModel().clearAndSelect(selection));
        waitForRunLater();
    }

    //Dashboard must be reinitialized after adding timelines to database, as the list is only updated when pressing certain buttons or reloading the whole dashboard
    void reinitializeDashboard() throws InterruptedException {
        Platform.runLater(() -> sut.initialize());
        waitForRunLater();
    }

    //Helper method for making and adding Timelines to the database

    //Adds a new timeline for each string put in the parameter, each string is the name of the timeline
    void addNewTimelineToDBByName(String... name) {
        for (String n : name) {
            Timeline newTimeline = new Timeline();
            newTimeline.setName(n);
            newTimeline.setOwner(loginUser);
            try {DBM.insertIntoDB(newTimeline);} catch (SQLException e) {e.printStackTrace();}
        }
    }

    //Adds a new timeline for each int put in the parameter, each int is the owner id of that timeline
    void addNewTimelineToDBByOwnerId(User... owner) {
        for (User n : owner) {
            Timeline newTimeline = new Timeline();
            newTimeline.setOwner(n);
            try {DBM.insertIntoDB(newTimeline);} catch (SQLException e) {e.printStackTrace();}
        }
    }

    //Adds a single timeline that has each string as a keyword
    void addNewTimelineToDBByKeyWords(String... words) {
        Timeline newTimeline = new Timeline();
        newTimeline.setOwner(loginUser);
        try {DBM.insertIntoDB(newTimeline);} catch (SQLException e) {e.printStackTrace();}
        for (String n : words)
            newTimeline.getKeywords().add(n);
        try {DBM.updateInDB(newTimeline);} catch (SQLException e) {e.printStackTrace();}
    }

    //Adds a single timeline with the inputted name and inputted owner id
    void addNewTimelineToDBByOwnerIdAndName(String name, User owner) {
        Timeline newTimeline = new Timeline();
        newTimeline.setOwner(owner);
        newTimeline.setName(name);
        try {DBM.insertIntoDB(newTimeline);} catch (SQLException e) {e.printStackTrace();}
    }

    //Adds a single timeline that has each string as a keyword and the inputted owner id
    void addNewTimelineToDBByOwnerIdAndKeyword(User owner, String... words) {
        Timeline newTimeline = new Timeline();
        newTimeline.setOwner(owner);
        for (String n : words)
            newTimeline.getKeywords().add(n);
        try {DBM.insertIntoDB(newTimeline);} catch (SQLException e) {e.printStackTrace();}
    }

    //Adds a single timeline that has a start date with the inputted values
    void addNewTimelineToDBByStartDate(int year, int month, int day, int hour, int minute, int second, int millisecond) {
        Timeline newTimeline = new Timeline();
        newTimeline.setOwner(loginUser);
        try {DBM.insertIntoDB(newTimeline);} catch (SQLException e) {e.printStackTrace();}

        newTimeline.setStartDate(LocalDateTime.of(year, month, day, hour, minute, second, millisecond*1000000));
        newTimeline.setEndDate(LocalDateTime.of(year + 1, 0, 0, 0, 0, 0, 0)); //Timelines must have an end date that is after the start date

        try {DBM.updateInDB(newTimeline);} catch (SQLException e) {e.printStackTrace();}
    }

    //Adds a single timeline that has a start date with the inputted values
    void addNewTimelineToDBByEndDate(int year, int month, int day, int hour, int minute, int second, int millisecond) {
        Timeline newTimeline = new Timeline();
        newTimeline.setOwner(loginUser);
        try {DBM.insertIntoDB(newTimeline);} catch (SQLException e) {e.printStackTrace();}

        newTimeline.setStartDate(LocalDateTime.of(year - 1, 0, 0, 0, 0, 0, 0)); //Timelines must have an end date that is after the start date
        newTimeline.setEndDate(LocalDateTime.of(year, month, day, hour, minute, second, millisecond));

        try {DBM.updateInDB(newTimeline);} catch (SQLException e) {e.printStackTrace();}
    }

    //Helper method for getting the robot to click on a popup window/alert
    private DialogPane getDialogPane() {
        final List<Window> allWindows = Window.getWindows();        //Get a list of all currently open windows
        for (Window w : allWindows)
        {
            if (w != null && w.isFocused())                         //alerts and popups have focus, so return the currently focused window
                return (DialogPane) w.getScene().getRoot();
        }
        return null;
    }

}