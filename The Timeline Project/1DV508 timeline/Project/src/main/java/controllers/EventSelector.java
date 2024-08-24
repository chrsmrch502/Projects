package controllers;

import database.DBM;
import database.Event;
import database.Timeline;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;

public class EventSelector {
    private final ObservableList<Event> eventList = FXCollections.observableArrayList();
    private final FilteredList<Event> filterableEventList = new FilteredList<>(eventList);
    private final SortedList<Event> sortableEventList = new SortedList<>(filterableEventList);
    @FXML GridPane selector;
    @FXML ComboBox<Timeline> timelineComboBox;
    @FXML Button viewButton;
    @FXML ComboBox<String> sortBy;
    @FXML Button deleteButton;
    @FXML TextField searchInput;
    @FXML Button newButton;
    @FXML Button addToRemoveFromTimelineButton;
    @FXML ListView<Event> eventListView;
    private TimelineView parentController;
    private List<List<Integer>> timelineEventLinks;

    public void initialize() {
        if (!GUIManager.loggedInUser.getAdmin()) {
            newButton.setVisible(false);
            deleteButton.setVisible(false);
            addToRemoveFromTimelineButton.setVisible(false);
        }

        eventListView.setItems(sortableEventList);

        populateDisplay();


        sortBy.getItems().setAll("Alphabetic", "Reverse Alphabetic", "Creation Date", "Reverse Creation Date", "Priority");
        sortBy.getSelectionModel().selectedIndexProperty().addListener(ov -> sortEvents(sortBy.getSelectionModel().getSelectedIndex()));

        //formatting for timeline and event selectors
        timelineComboBox.setButtonCell(new TimelineListCell());
        timelineComboBox.setCellFactory(param -> new TimelineListCell());
        eventListView.setCellFactory(param -> new EventListCell());

        //search bar listener
        searchInput.textProperty().addListener(e -> setFilter());

        //listeners to respond to a timeline or event being selected
        timelineComboBox.getSelectionModel().selectedIndexProperty().addListener(event -> {     //on selecting a different timeline, clear the event selection and disable event controls
            setFilter();
            eventListView.getSelectionModel().clearSelection();
            setEventControlButtons();
        });

        eventListView.getSelectionModel().selectedIndexProperty().addListener(e -> setEventControlButtons());
    }

    private void setEventControlButtons() {
        boolean hasSelection = eventListView.getSelectionModel().getSelectedIndex() >= 0;
        boolean owner = hasSelection && GUIManager.loggedInUser.getID() == eventListView.getSelectionModel().getSelectedItem().getOwnerID();    //logged in user is owner of selected event

        viewButton.setDisable(!hasSelection);           //uses !hasSelection because setDISABLE, it's an annoying double-negative
        deleteButton.setDisable(!owner);
        addToRemoveFromTimelineButton.setDisable(!owner ||
                GUIManager.loggedInUser.getID() != eventListView.getSelectionModel().getSelectedItem().getOwnerID());   //also check if owner of timeline

        if (owner && parentController.activeTimeline.getEventList().contains(eventListView.getSelectionModel().getSelectedItem()))
            addToRemoveFromTimelineButton.setText("Remove From Timeline");          //if selected event is on the active timeline, button removes it
        else
            addToRemoveFromTimelineButton.setText("Add To Timeline");               //otherwise, button adds it to active timeline (may be disabled if no selection)
    }

    void setParentController(TimelineView parentController) {
        this.parentController = parentController;
    }

    @FXML
    void newEvent() {
        openEditor(new Event(), true);
    }

    @FXML
    void openEvent() {
        openEditor(eventListView.getSelectionModel().getSelectedItem(), false);
    }

    private void openEditor(Event eventToOpen, boolean editable) {
        parentController.eventEditorController.setEvent(eventToOpen);
        parentController.eventEditorController.toggleEditable(editable);
        parentController.rightSidebar.getChildren().add(parentController.eventEditorController.editor);
    }

    @FXML
    boolean deleteButton() {
        return deleteEvent(eventListView.getSelectionModel().getSelectedItem());
    }

    boolean deleteEvent(Event eventToDelete) {
        Alert confirmDelete = new Alert(Alert.AlertType.CONFIRMATION);
        confirmDelete.setTitle("Confirm Delete");
        confirmDelete.setHeaderText("Deleting " + eventToDelete.getName() + " will remove it from all other timelines as well.");
        confirmDelete.setContentText("Are you ok with this?");

        Optional<ButtonType> result = confirmDelete.showAndWait();

        if (result.isPresent() && result.get() == ButtonType.CANCEL)
            return false;

        try {
            if (eventToDelete.getID() == 0)
                throw new IllegalArgumentException("Event not in database.");

            DBM.deleteFromDB(eventToDelete);
            populateDisplay();
            parentController.populateDisplay();
            return true;
        } catch (SQLException e) {
            return false;
        }
    }

    void populateDisplay() {
        Timeline currentSelection = timelineComboBox.getSelectionModel().getSelectedItem();
        populateTimelineList();
        populateEventList();
        setTimelineSelected(currentSelection);
    }

    void populateTimelineList() {
        try {
            PreparedStatement stmt = DBM.conn.prepareStatement("SELECT * FROM timelines");
            timelineComboBox.getItems().setAll(DBM.getFromDB(stmt, new Timeline()));
        } catch (SQLException e) {
            System.err.println("Could not access timelines database.");
        }
    }

    void populateEventList() {
        try {
            eventList.setAll(DBM.getFromDB(DBM.conn.prepareStatement("SELECT * FROM events"), new Event()));
            timelineEventLinks = DBM.getFromDB(DBM.conn.prepareStatement("SELECT * FROM timelineevents"),
                    rs -> Arrays.asList(rs.getInt("TimelineID"), rs.getInt("EventID")));
            setFilter();
        } catch (SQLException e) {
            System.out.println("Could not access events database.");
        }
    }

    void setTimelineSelected(Timeline timelineToSelect) {
        timelineComboBox.getSelectionModel().select(-1);
        if (timelineToSelect == null)
            return;
        for (Timeline t : timelineComboBox.getItems()) {
            if (timelineToSelect.equals(t)) {
                timelineComboBox.getSelectionModel().select(t);
                break;
            }
        }
        //disable adding new event if not owner
        newButton.setDisable(GUIManager.loggedInUser.getID()
                != parentController.activeTimeline.getOwnerID());
    }

    void sortEvents(int selection) {
        switch (selection) {
            case 0:
                sortableEventList.setComparator((e1, e2) -> (e1.getName().compareToIgnoreCase(e2.getName())));
                break;
            case 1:
                sortableEventList.setComparator((e1, e2) -> (e2.getName().compareToIgnoreCase(e1.getName())));
                break;
            case 2:
                sortableEventList.setComparator(Comparator.comparing(Event::getCreationDate).reversed());
                break;
            case 3:
                sortableEventList.setComparator(Comparator.comparing(Event::getCreationDate));
                break;
            case 4:
                sortableEventList.setComparator((e1, e2) -> (Integer.compare(e2.getEventPriority(), e1.getEventPriority())));
                break;
        }
    }

    private void setFilter() {
        filterableEventList.setPredicate(getTimelineFilter().and(getSearchFilter()));
    }

    Predicate<Event> getSearchFilter() {
        String searchText = searchInput.getText();
        if (searchText == null || searchText.isEmpty())
            return timeline -> true;
        else
            return timeline -> timeline.getName().toLowerCase().contains(searchText.toLowerCase());
    }

    Predicate<Event> getTimelineFilter() {
        if (timelineComboBox.getSelectionModel().getSelectedIndex() < 0)                                //if no selection, display everything
            return e -> true;
        else
            return e -> (timelineEventLinks.stream().anyMatch(                                          //checks the junction table
                    te -> te.get(0) == timelineComboBox.getSelectionModel().getSelectedItem().getID()   //filters by the selected timeline
                            && e.getID() == te.get(1)));                                                //and returns whether each event is on that timeline
    }

    @FXML
    void addRemoveTimeline() {
        if (addToRemoveFromTimelineButton.getText().equals("Remove From Timeline"))
            removeFromTimeline();
        else
            addToTimeline();
    }

    private void addToTimeline() {
        try {
            eventListView.getSelectionModel().getSelectedItem().addToTimeline(parentController.activeTimeline.getID());
            parentController.activeTimeline.getEventList().add(eventListView.getSelectionModel().getSelectedItem());
            populateEventList();
            parentController.populateDisplay();
        } catch (SQLException e) {
            System.out.println("Timeline not found.");
        }
    }

    private void removeFromTimeline() {
        try {
            eventListView.getSelectionModel().getSelectedItem().removeFromTimeline(parentController.activeTimeline.getID());
            parentController.activeTimeline.getEventList().remove(eventListView.getSelectionModel().getSelectedItem());
            populateEventList();
            parentController.populateDisplay();
        } catch (SQLException e) {
            System.out.println("Timeline not found.");
        }
    }

    @FXML
    void clearTimelineSelection() {
        timelineComboBox.getSelectionModel().select(-1);
    }

    private class EventListCell extends ListCell<Event> {         //changes how Events are displayed (name only)
        @Override
        protected void updateItem(Event item, boolean empty) {
            super.updateItem(item, empty);
            if (empty || item == null || item.getName() == null) {
                setText(null);
            } else {
                setText(item.getName());
            }

            this.setOnMouseClicked(e -> {
                if (e.getClickCount() == 2)
                    openEditor(item, false);
            });
        }
    }

    private static class TimelineListCell extends ListCell<Timeline> {         //changes how Events are displayed (name only)
        @Override
        protected void updateItem(Timeline item, boolean empty) {
            super.updateItem(item, empty);
            if (empty || item == null || item.getName() == null) {
                setText(null);
            } else {
                setText(item.getName());
            }
        }
    }
}
