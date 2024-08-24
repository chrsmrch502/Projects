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
import javafx.stage.Stage;
import javafx.stage.Window;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.testfx.api.FxRobot;
import org.testfx.framework.junit5.ApplicationExtension;
import org.testfx.framework.junit5.Start;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.Semaphore;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(ApplicationExtension.class)
public class EventEditorTest {
    static private int testCount = 0;
    TimelineView parent;
    EventEditor sut;
    EventSelector selector;
    FxRobot robot = new FxRobot();

    @BeforeAll
    public static void beforeAll() {
        new DBM("test");
    }

    @Start
    public void start(Stage stage) throws Exception {
        System.out.println("Test " + ++testCount);

        DBM.setupSchema();
        DBM.createTestData();
        GUIManager.loggedInUser = new User();

        FXMLLoader loader = new FXMLLoader(getClass().getResource("../../classes/FXML/TimelineView.fxml"));
        GUIManager.mainStage = stage;
        stage.setScene(new Scene(loader.load()));
        parent = loader.getController();
        parent.setActiveTimeline(DBM.getFromDB(DBM.conn.prepareStatement("SELECT * FROM timelines LIMIT 1"), new Timeline()).get(0));
        selector = parent.eventSelectorController;
        sut = parent.eventEditorController;

        stage.show();
    }

    @AfterEach
    void tearDown() {
    }

    /*
    @Test
    void initialize() {
    }


        @Test
        void setParentController() {
        }

        @Test
        void saveEditButton() {
        }

        @Test
        void toggleEditable() {
        }

        @Test
        void setEvent() {
        }

        @Test
        void testSetEvent() {
        }

        @Test
        void updateEvent() {
        }

        @Test
        void toggleStartExpanded() {
        }

        @Test
        void toggleEndExpanded() {
        }
*/
    @Test
    void hasChangesNewEventNoChanges() throws InterruptedException {
        runLater(() -> {
            sut.setEvent(new Event());
            assertFalse(sut.hasChanges());
        });
    }

    @Test
    void hasChangesViewEventNoChanges() throws SQLException, InterruptedException {
        Event event1 = DBM.getFromDB(DBM.conn.prepareStatement("SELECT * FROM events"), new Event()).get(0);
        runLater(() -> {
            sut.setEvent(event1);
            assertFalse(sut.hasChanges());
        });
    }

    @Test
    void newEventSaved() throws SQLException, InterruptedException {
        int expectedDB = 1 + DBM.getFromDB(DBM.conn.prepareStatement("SELECT COUNT(*) FROM events LIMIT 1"), rs -> rs.getInt(1)).get(0);
        int expectedTimelineList = 1 + parent.activeTimeline.getEventList().size();

        setAdminLoggedIn(true);
        runLater(() -> selector.newEvent());
        PreparedStatement stmt = DBM.conn.prepareStatement("SELECT COUNT(*) FROM timelineevents WHERE EventID = ?");
        stmt.setInt(1, sut.event.getID());

        boolean inDB = DBM.getFromDB(stmt, rs -> rs.getInt(1)).get(0) > 0;
        assertFalse(inDB);

        runLater(() -> {
            sut.toggleEditable(true);
            sut.titleInput.setText("test");
            sut.saveEditButton();
        });
        runLater(() -> {
            DialogPane alert = getDialogPane();
            robot.clickOn(alert.lookupButton(ButtonType.OK));
        });
        runLater(() -> {
            int actualDB = 0;

            try {
                actualDB = DBM.getFromDB(DBM.conn.prepareStatement("SELECT COUNT(*) FROM events LIMIT 1"), rs -> rs.getInt(1)).get(0);

                stmt.setInt(1, sut.event.getID());
                assertTrue(DBM.getFromDB(stmt, rs -> rs.getInt(1)).get(0) > 0);    //assert count in junction table > 0
            } catch (SQLException e) {
                e.printStackTrace();
            }
            int actualTimelineList = parent.activeTimeline.getEventList().size();
            assertEquals(expectedDB, actualDB);
            assertEquals(expectedTimelineList, actualTimelineList);
        });

    }

    @Test
    void oldEventSaved() throws InterruptedException {
        String expected = "test";

        openEventFromSelector(1, true);
        runLater(() -> {
            sut.saveEditButton();
            sut.titleInput.setText("test");
            sut.saveEditButton();
        });
        runLater(() -> {
            DialogPane alert = getDialogPane();
            robot.clickOn(alert.lookupButton(ButtonType.OK));
        });
        runLater(() -> {
            String actual = null;
            try {
                actual = DBM.getFromDB(DBM.conn.prepareStatement("SELECT * FROM events WHERE EventID = " + sut.event.getID()), rs -> rs.getString("EventName")).get(0);
            } catch (SQLException e) {
                e.printStackTrace();
            }
            assertEquals(expected, actual);
        });
    }

    @Test
    void disableEndDateAndSave() throws InterruptedException {
        openEventFromSelector(1, true);

        LocalDateTime expected = sut.event.getStartDate();
        assertNotEquals(0, expected.compareTo(sut.event.getEndDate()));

        runLater(() -> {
            sut.saveEditButton();
            sut.hasDuration.setSelected(false);
            sut.toggleHasDuration();
            sut.saveEditButton();
        });
        runLater(() -> {
            DialogPane alert = getDialogPane();
            robot.clickOn(alert.lookupButton(ButtonType.OK));
        });
        runLater(() -> assertEquals(0, expected.compareTo(sut.event.getEndDate())));

    }

    @Test
    void ownerCanEdit() throws InterruptedException {
        openEventFromSelector(1, true);

        runLater(() -> {
            sut.saveEditButton();
            sut.hasDuration.setSelected(false);
            sut.toggleHasDuration();
        });
        assertFalse(sut.saveEditButton.isDisable());
        assertFalse(sut.deleteButton.isDisable());
    }

    @Test
    void nonOwnerAdminCantEdit() throws InterruptedException {
        openEventFromSelector(1, false);
        setAdminLoggedIn(true);
        assertTrue(sut.saveEditButton.isDisable());
        assertTrue(sut.deleteButton.isDisable());
    }

    @Test
    void editTextFieldsAndSave() throws InterruptedException {
        openEventFromSelector(1, true);

        assertEquals(sut.titleInput.getText(), sut.event.getName());
        assertEquals(sut.descriptionInput.getText(), sut.event.getDescription());

        runLater(() -> {
            sut.saveEditButton();
            sut.titleInput.setText("testtext");
            sut.descriptionInput.setText("testtext");
            assertNotEquals(sut.titleInput.getText(), sut.event.getName());
            assertNotEquals(sut.descriptionInput.getText(), sut.event.getDescription());
        });
        runLater(() -> sut.saveEditButton());
        runLater(() -> robot.clickOn(getDialogPane().lookupButton(ButtonType.OK)));
        runLater(() -> {
            assertEquals(sut.titleInput.getText(), sut.event.getName());
            assertEquals(sut.descriptionInput.getText(), sut.event.getDescription());
        });
    }

    @Test
    void strangeTextFieldsSaved() throws InterruptedException {
        openEventFromSelector(1, true);

        assertEquals(sut.titleInput.getText(), sut.event.getName());
        assertEquals(sut.descriptionInput.getText(), sut.event.getDescription());

        runLater(() -> {
            sut.saveEditButton();
            sut.titleInput.setText("<foo val=“bar” />");
            sut.descriptionInput.setText("test \t\t\t\t test text test text test text test text test text test text test text test text test text test text test text test text test text test text test text test text test text test text test text test text test text test text test text test text test text test text test text test text test text test text test text test text test text test text test text test text test text test text test text test text test text test text test text test text test text test text test text test text test text test text test text test text test text test text test text test text test text test text test text test text test text test text test text test text test text test text test text test text test text test text test text test text test text test text test text test text test text test text test text test text test text test text test text test text test text test text test text test text test text test text test text test text test text test text test text test text test text test text test text ");
            assertNotEquals(sut.titleInput.getText(), sut.event.getName());
            assertNotEquals(sut.descriptionInput.getText(), sut.event.getDescription());
        });
        runLater(() -> sut.saveEditButton());
        runLater(() -> robot.clickOn(getDialogPane().lookupButton(ButtonType.OK)));
        runLater(() -> {
            assertEquals(sut.titleInput.getText(), sut.event.getName());
            assertEquals(sut.descriptionInput.getText(), sut.event.getDescription());
        });
    }

    @Test
    void editStartDateAndSave() throws InterruptedException {
        openEventFromSelector(1, true);
        runLater(() -> {
            LocalDateTime readStart = LocalDateTime.of(sut.startInputs.get(0).getValue(), sut.startInputs.get(1).getValue(), sut.startInputs.get(2).getValue(),
                    sut.startInputs.get(3).getValue(), sut.startInputs.get(4).getValue(), sut.startInputs.get(5).getValue(), sut.startInputs.get(6).getValue()*1000000);
            assertEquals(readStart, sut.event.getStartDate());
        });
        runLater(() -> {
            sut.saveEditButton();
            sut.startInputs.get(0).getValueFactory().setValue(-100000);
            sut.startInputs.get(1).getValueFactory().setValue(1);
            sut.startInputs.get(2).getValueFactory().setValue(6);
            sut.startInputs.get(3).getValueFactory().setValue(0);
            sut.startInputs.get(4).getValueFactory().setValue(59);
            sut.startInputs.get(5).getValueFactory().setValue(59);
            sut.startInputs.get(6).getValueFactory().setValue(880);

            LocalDateTime readStart = LocalDateTime.of(sut.startInputs.get(0).getValue(), sut.startInputs.get(1).getValue(), sut.startInputs.get(2).getValue(),
                    sut.startInputs.get(3).getValue(), sut.startInputs.get(4).getValue(), sut.startInputs.get(5).getValue(), sut.startInputs.get(6).getValue()*1000000);

            assertNotEquals(readStart, sut.event.getStartDate());
        });
        runLater(() -> sut.saveEditButton());
        runLater(() -> robot.clickOn(getDialogPane().lookupButton(ButtonType.OK)));

        runLater(() -> {
            LocalDateTime readStart = LocalDateTime.of(sut.startInputs.get(0).getValue(), sut.startInputs.get(1).getValue(), sut.startInputs.get(2).getValue(),
                    sut.startInputs.get(3).getValue(), sut.startInputs.get(4).getValue(), sut.startInputs.get(5).getValue(), sut.startInputs.get(6).getValue()*1000000);
            assertEquals(readStart, sut.event.getStartDate());
        });
    }

    @Test
    void editEndDateAndSave() throws InterruptedException {
        openEventFromSelector(1, true);
        runLater(() -> {
            LocalDateTime readEnd = LocalDateTime.of(sut.endInputs.get(0).getValue(), sut.endInputs.get(1).getValue(), sut.endInputs.get(2).getValue(),
                    sut.endInputs.get(3).getValue(), sut.endInputs.get(4).getValue(), sut.endInputs.get(5).getValue(), sut.endInputs.get(6).getValue()*1000000);
            assertEquals(readEnd, sut.event.getEndDate());
        });
        runLater(() -> {
            sut.saveEditButton();
            sut.endInputs.get(0).getValueFactory().setValue(99999);
            sut.endInputs.get(1).getValueFactory().setValue(12);
            sut.endInputs.get(2).getValueFactory().setValue(1);
            sut.endInputs.get(3).getValueFactory().setValue(2);
            sut.endInputs.get(4).getValueFactory().setValue(14);
            sut.endInputs.get(5).getValueFactory().setValue(34);
            sut.endInputs.get(6).getValueFactory().setValue(0);

            LocalDateTime readEnd = LocalDateTime.of(sut.endInputs.get(0).getValue(), sut.endInputs.get(1).getValue(), sut.endInputs.get(2).getValue(),
                    sut.endInputs.get(3).getValue(), sut.endInputs.get(4).getValue(), sut.endInputs.get(5).getValue(), sut.endInputs.get(6).getValue()*1000000);

            assertNotEquals(readEnd, sut.event.getEndDate());
        });
        runLater(() -> sut.saveEditButton());
        runLater(() -> robot.clickOn(getDialogPane().lookupButton(ButtonType.OK)));

        runLater(() -> {
            LocalDateTime readEnd = LocalDateTime.of(sut.endInputs.get(0).getValue(), sut.endInputs.get(1).getValue(), sut.endInputs.get(2).getValue(),
                    sut.endInputs.get(3).getValue(), sut.endInputs.get(4).getValue(), sut.endInputs.get(5).getValue(), sut.endInputs.get(6).getValue()*1000000);
            assertEquals(readEnd, sut.event.getEndDate());
        });
    }

    @Test
    void editPriorityAndSave() throws InterruptedException {
        openEventFromSelector(1, true);

        assertEquals(sut.prioritySlider.getValue(), sut.event.getEventPriority());

        runLater(() -> {
            sut.saveEditButton();
            sut.prioritySlider.setValue(3);
            assertNotEquals(sut.prioritySlider.getValue(), sut.event.getEventPriority());
        });
        runLater(() -> sut.saveEditButton());
        runLater(() -> robot.clickOn(getDialogPane().lookupButton(ButtonType.OK)));
        runLater(() -> assertEquals(sut.prioritySlider.getValue(), sut.event.getEventPriority()));
    }

    /* ********** Helper Methods ****************/
    //constrain code to main thread (so something isn't called before it loads)
    void runLater(Runnable runnable) throws InterruptedException {
        Platform.runLater(runnable);
        Semaphore semaphore = new Semaphore(0);
        Platform.runLater(semaphore::release);
        semaphore.acquire();
    }

    //returns popup windows so they can be interacted with
    private DialogPane getDialogPane() {
        final List<Window> allWindows = Window.getWindows();            //Get a list of windows
        for (Window w : allWindows) {                                   //if a window is a DialogPane with the correct title, return it
            if (w != null && w.getScene().getRoot() instanceof DialogPane)
                return (DialogPane) w.getScene().getRoot();
        }
        return null;
    }

    //control who is logged in
    void setAdminLoggedIn(boolean admin) {
        GUIManager.loggedInUser.setAdmin(admin);
    }

    //open a specific event from the Event Selector's event list
    void openEventFromSelector(int optionNum, boolean owner) throws InterruptedException {
        runLater(() -> {
            selector.eventListView.getSelectionModel().select(optionNum);
            GUIManager.loggedInUser.setID(owner ? selector.eventListView.getSelectionModel().getSelectedItem().getOwnerID() : 0);
            GUIManager.loggedInUser.setAdmin(owner);
            selector.openEvent();
        });
    }
}