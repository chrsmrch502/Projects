package controllers;

import com.google.gson.Gson;
import database.DBM;
import database.JSONTimeline;
import database.Timeline;
import javafx.beans.Observable;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.StringConverter;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.function.Predicate;

public class Dashboard {
    final List<Spinner<Integer>> startInputs = new ArrayList<>();
    final List<Spinner<Integer>> endInputs = new ArrayList<>();
    final ObservableList<Timeline> timelineList = FXCollections.observableArrayList();
    final FilteredList<Timeline> filteredTimelines = new FilteredList<>(timelineList);
    final SortedList<Timeline> sortedTimelines = new SortedList<>(filteredTimelines);
    @FXML ScrollPane listScrollPane;
    @FXML Button importButton;
    @FXML VBox greetingBox;
    @FXML StackPane rightStack;
    @FXML Button adminGUI;
    @FXML Button btnCreate;
    @FXML ListView<Timeline> list;
    @FXML TextField searchInput;
    @FXML TextField searchTimelineName;
    @FXML TextField searchCreator;
    @FXML TextField searchKeywords;
    @FXML ComboBox<Integer> searchRating;
    @FXML CheckBox checkboxOnlyViewPersonalLines;
    @FXML ComboBox<String> sortBy;
    @FXML GridPane advancedSearchView;
    @FXML GridPane startDates;
    @FXML GridPane endDates;

    public void initialize() {
        //Set Up the Spinners for Start/End Inputs, would have bloated the .fxml and variable list a ton if these were in fxml
        setupTimeInputStartAndEnd("Year", -1000000000, 999999999, 0, 0, 0);
        setupTimeInputStartAndEnd("Month", 0, 12, 1, 0, 1);
        setupTimeInputStartAndEnd("Day", 0, 31, 2, 0, 2);
        setupTimeInputStartAndEnd("Hour", -1, 23, 3, 0, 3);
        setupTimeInputStartAndEnd("Minute", -1, 59, 0, 2, 4);
        setupTimeInputStartAndEnd("Second", -1, 59, 1, 2, 5);
        setupTimeInputStartAndEnd("Millisecond", -1, 999, 2, 2, 6);

        initializeButtons();

        // Fill ListView with the timelines
        populateTimelineList();
        list.setItems(sortedTimelines);
        list.setCellFactory((ListView<Timeline> ls) -> new TimelineCellListCell());

        // Add sorting options
        sortBy.getItems().setAll("Alphabetically", "Reverse-Alphabetically", "Most Recent", "Oldest", "Rating");
        // Sort order selection events
        sortBy.getSelectionModel().selectedIndexProperty().addListener(ov -> sortTimelines());
        sortBy.getSelectionModel().select(4);

        // Search field
        checkboxOnlyViewPersonalLines.selectedProperty().addListener(this::simpleSearch);
        searchInput.textProperty().addListener(this::simpleSearch);

        list.getSelectionModel().selectedIndexProperty().addListener(e -> updateDisplays());

        //Ratings combobox in advanced search
        searchRating.getItems().setAll(Arrays.asList(0, 1, 2, 3, 4, 5));
        searchRating.setButtonCell(new RatingsListCell());
        searchRating.setCellFactory(param -> new RatingsListCell());

        updateDisplays();

        list.widthProperty().addListener(e -> list.refresh());

        GUIManager.mainStage.setTitle("Dashboard");
    }

    private void populateTimelineList() {
        try {
            PreparedStatement stmt = DBM.conn.prepareStatement("SELECT * FROM timelines");
            timelineList.setAll(DBM.getFromDB(stmt, new Timeline()));
        } catch (SQLException e) {
            System.err.println("Could not read timelines from database.");
        }
    }

    private void initializeButtons() {
        boolean admin = !GUIManager.loggedInUser.getAdmin();
        rightStack.getChildren().remove(advancedSearchView);
        btnCreate.setDisable(admin);
        importButton.setDisable(admin);
        adminGUI.setDisable(admin);
    }

    private void simpleSearch(Observable obs) {
        Timeline currentlySelectedTimeline = list.getSelectionModel().getSelectedItem();
        String searchText = searchInput.getText();
        if (searchText == null || searchText.isEmpty())
            filteredTimelines.setPredicate(timeline -> true);
        else
            filteredTimelines.setPredicate(timeline -> timeline.getName().toLowerCase().contains(searchText.toLowerCase())
                    || timeline.getKeywords().stream().anyMatch(k -> k.toLowerCase().contains(searchText.toLowerCase())));

        Predicate<Timeline> onlyPersonal = timeline -> timeline.getOwnerID() == GUIManager.loggedInUser.getID();
        if (checkboxOnlyViewPersonalLines.isSelected())
            filteredTimelines.setPredicate(onlyPersonal.and(filteredTimelines.getPredicate()));

        handleAutoSelection(currentlySelectedTimeline);
    }

    @FXML
    void searchAdvanced() {
        try (ResultSet data = DBM.conn.prepareStatement(                                        //get searchable info from database
                "SELECT t.*, u.UserName, COALESCE(AVG(r.Rating), 0) as Rating FROM timelines t " +
                        "INNER JOIN users u ON t.TimelineOwner = u.UserID " +
                        "LEFT JOIN ratings r ON t.TimelineID = r.TimeLineID " +
                        "GROUP BY t.TimelineID").executeQuery()) {

            List<Integer> listOfIDs = parseResultsForAdvancedSearch(data);                      //check for matches against search criteria
            filteredTimelines.setPredicate(timeline -> listOfIDs.contains(timeline.getID()));   //apply matches as filter to timeline list
            list.getSelectionModel().clearSelection();
            updateDisplays();
        } catch (SQLException e) {
            System.err.println("Could not read timelines from database.");
        }
    }

    List<Integer> parseResultsForAdvancedSearch(ResultSet data) throws SQLException {
        List<Integer> out = new ArrayList<>();
        boolean addToList = true;

        while (data.next()) {   //if the search input has an entry, but doesn't match DB contents, then not a match. check for each input
            //Timeline Name
            if (!searchTimelineName.getText().isEmpty() && !data.getString("TimelineName").toLowerCase().contains(searchTimelineName.getText().toLowerCase())) {
                addToList = false;
            }
            //Timeline Owner
            if (!searchCreator.getText().isEmpty() && !data.getString("UserName").toLowerCase().contains(searchCreator.getText().toLowerCase())) {
                addToList = false;
            }

            //Keywords
            Predicate<String> keywordMatches = k -> {
                try {
                    return data.getString("Keywords").toLowerCase().contains(k.toLowerCase());
                } catch (SQLException e) {
                    e.printStackTrace();
                    return false;
                }
            };
            if (!searchKeywords.getText().isEmpty() && !Arrays.stream(searchKeywords.getText().toLowerCase().split(" ")).allMatch(keywordMatches)) {
                addToList = false;
            }

            if (dateSearchedBy(startInputs)) {
                //StartDate
                LocalDateTime startDateSpinner = readTimeInputs(startInputs);
                LocalDateTime startDateInDB = LocalDateTime.of(data.getInt("StartYear"), data.getInt("StartMonth"), data.getInt("StartDay"),
                        data.getInt("StartHour"), data.getInt("StartMinute"), data.getInt("StartSecond"), data.getInt("StartMillisecond"));

                if (startDateInDB.compareTo(startDateSpinner) < 0) {
                    addToList = false;
                }
            }

            if (dateSearchedBy(endInputs)) {
                //EndDate
                LocalDateTime endDateSpinner = readTimeInputs(endInputs);
                LocalDateTime endDateInDB = LocalDateTime.of(data.getInt("EndYear"), data.getInt("EndMonth"), data.getInt("EndDay"),
                        data.getInt("EndHour"), data.getInt("EndMinute"), data.getInt("EndSecond"), data.getInt("EndMillisecond"));

                if (endDateInDB.compareTo(endDateSpinner) > 0) {
                    addToList = false;
                }
            }

            //Rating
            if (searchRating.getSelectionModel().getSelectedIndex() > 0 && Math.ceil(data.getDouble("Rating")) < searchRating.getSelectionModel().getSelectedIndex())
                addToList = false;

            if (addToList)
                out.add(data.getInt("TimelineID"));
            addToList = true;           //reset for next line of ResultSet
        }

        return out;
    }

    private boolean dateSearchedBy(List<Spinner<Integer>> inputs) {     //returns whether or not ANY inputs of either start or end dates are being used
        for (Spinner<Integer> s : inputs) {
            if (s.getValue() != ((SpinnerValueFactory.IntegerSpinnerValueFactory) s.getValueFactory()).getMin())
                return true;
        }
        return false;
    }

    private LocalDateTime readTimeInputs(List<Spinner<Integer>> inputs) {
        return LocalDateTime.of(
                (inputs.get(0).getValue() == -1000000000) ? 0 : inputs.get(0).getValue(),
                Math.max(1, inputs.get(1).getValue()),
                Math.max(1, inputs.get(2).getValue()),
                Math.max(0, inputs.get(3).getValue()),
                Math.max(0, inputs.get(4).getValue()),
                Math.max(0, inputs.get(5).getValue()),
                Math.max(0, inputs.get(6).getValue()));
    }

    @FXML
    void toggleAdvancedSearch() {
        if (rightStack.getChildren().contains(advancedSearchView)) {
            rightStack.getChildren().set(0, greetingBox);
            searchInput.setDisable(false);
            checkboxOnlyViewPersonalLines.setDisable(false);
        } else {
            clearAdvancedSearch();
            rightStack.getChildren().set(0, advancedSearchView);
            searchInput.setDisable(true);
            checkboxOnlyViewPersonalLines.setDisable(true);
        }
    }

    @FXML
    void clearAdvancedSearch() {
        Timeline currentlySelectedTimeline = list.getSelectionModel().getSelectedItem();
        searchTimelineName.clear();
        searchCreator.clear();
        searchKeywords.clear();
        searchInput.clear();
        searchRating.getSelectionModel().select(0);
        startInputs.forEach(spinner -> spinner.getValueFactory().setValue(((SpinnerValueFactory.IntegerSpinnerValueFactory) spinner.getValueFactory()).getMin()));
        endInputs.forEach(spinner -> spinner.getValueFactory().setValue(((SpinnerValueFactory.IntegerSpinnerValueFactory) spinner.getValueFactory()).getMin()));
        checkboxOnlyViewPersonalLines.setSelected(false);
        filteredTimelines.setPredicate(t -> true);
        handleAutoSelection(currentlySelectedTimeline);
    }

    private void handleAutoSelection(Timeline currentlySelectedTimeline) {
        if (currentlySelectedTimeline == null || !filteredTimelines.contains(currentlySelectedTimeline))
            list.getSelectionModel().clearSelection();
        else if (filteredTimelines.contains(currentlySelectedTimeline))
            list.getSelectionModel().select(currentlySelectedTimeline);
        list.refresh();
        updateDisplays();
    }

    void sortTimelines() {
        switch (sortBy.getSelectionModel().getSelectedIndex()) {
            case 0:
                sortedTimelines.setComparator((t1, t2) -> (t1.getName().compareToIgnoreCase(t2.getName())));
                break;
            case 1:
                sortedTimelines.setComparator((t1, t2) -> (t2.getName().compareToIgnoreCase(t1.getName())));
                break;
            case 2:
                sortedTimelines.setComparator(Comparator.comparing(Timeline::getCreationDate).reversed());
                break;
            case 3:
                sortedTimelines.setComparator(Comparator.comparing(Timeline::getCreationDate));
                break;
            case 4:
                sortedTimelines.setComparator(Comparator.comparing(Timeline::getRating).reversed());
                break;
        }
    }

    @FXML
    void adminScreen() throws IOException {
        Stage adminManagerStage = new Stage();
        adminManagerStage.setTitle("Admin Manager");
        adminManagerStage.initOwner(GUIManager.mainStage);         //These two lines make sure you can't click back to the timeline window,
        adminManagerStage.initModality(Modality.WINDOW_MODAL);     //so you can't have 10 windows open at once.

        Parent root = FXMLLoader.load(GUIManager.class.getResource("../FXML/AdminRoleManager.fxml"));
        adminManagerStage.setScene(new Scene(root));
        adminManagerStage.getScene().getStylesheets().addAll(GUIManager.mainStage.getScene().getStylesheets());
        adminManagerStage.show();
    }

    @FXML
    TimelineView createTimeline() {
        Timeline t = new Timeline();
        t.setOwner(GUIManager.loggedInUser);

        try {
            TimelineView timelineView = GUIManager.swapScene("TimelineView");
            timelineView.setActiveTimeline(t);
            timelineView.timelineEditorController.toggleEditable(true);
            return timelineView;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    @FXML
    void importFromJSON() {
        FileChooser chooser = new FileChooser();                                            //open FileChooser for user to pick import .json
        chooser.getExtensionFilters().addAll(new FileChooser.ExtensionFilter("JSON", "*.json"));
        File fileChosen = chooser.showOpenDialog(GUIManager.mainStage);
        if (fileChosen == null)             //usually only the case if the user cancels out of the file chooser
            return;

        try {
            String inJSON = FileUtils.readFileToString(fileChosen, (Charset) null);         //import Json from file
            Gson gson = JSONTimeline.getGson();
            JSONTimeline readJson = gson.fromJson(inJSON, JSONTimeline.class);              //parse Json with GSON object
            readJson.importToDB();                                                          //add imported data to database
            populateTimelineList();
        } catch (IOException e) {
            System.err.println("Could not read file.");
        }
        list.getSelectionModel().clearSelection();
        updateDisplays();
    }

    @FXML
    private void updateDisplays() {
        list.setPrefHeight((list.getItems().size() * 84) + (322 * list.getSelectionModel().getSelectedIndices().size()));
        if (list.getSelectionModel().getSelectedItem() != null)  //If a timeline is selected, scroll to the timeline directly above the selected one
            listScrollPane.setVvalue((84 / (list.getHeight() - listScrollPane.getHeight())) * (list.getSelectionModel().getSelectedIndex() - 1));
    }


    //applies equivalent setups to both start and end spinners
    private void setupTimeInputStartAndEnd(String timeSpinnerLabel, int minValue, int maxValue, int column, int row, int index) {
        setupTimeInput(timeSpinnerLabel, minValue, maxValue, column, row, startInputs, startDates, index);
        setupTimeInput(timeSpinnerLabel, minValue, maxValue, column, row, endInputs, endDates, index);
    }

    //creates spinners to handle dates with appropriate min/max values and invalid input handling
    private void setupTimeInput(String timeSpinnerLabel, int minValue, int maxValue, int column, int row, List<Spinner<Integer>> spinnerList, GridPane spinnerDates, int index) {
        SpinnerValueFactory.IntegerSpinnerValueFactory valueFactory = new SpinnerValueFactory.IntegerSpinnerValueFactory(minValue, maxValue, minValue) {
            @Override
            public void increment(int steps) {
                super.increment(steps);                         //makes blank years pretend to be 0 when using buttons, by incrementing to 1 and decrementing to -1
                if (getValue() == -999999999)
                    setValue(1);
            }

            @Override
            public void decrement(int steps) {
                super.decrement(steps);
                if (getValue() == 999999999)
                    setValue(-1);
            }
        };
        valueFactory.setConverter(new StringConverter<>() { //makes spinners revert to default values in case of invalid input
            @Override
            public String toString(Integer value) {     //called by spinner to update the displayed value in the box
                if (value == null)
                    return "";
                if (value == minValue)
                    return "";
                return value.toString();
            }

            @Override
            public Integer fromString(String string) {  //called by spinner to read the value from the box and convert to int
                try {
                    if (string == null)
                        return minValue;
                    string = string.trim();
                    if (string.length() < 1)
                        return minValue;
                    return Integer.parseInt(string);
                } catch (NumberFormatException ex) {
                    return minValue;
                }
            }
        });

        valueFactory.setWrapAround(true);
        spinnerList.add(index, new Spinner<>(valueFactory));
        spinnerList.get(index).setEditable(true);

        spinnerList.get(index).focusedProperty().addListener((observableValue, oldValue, newValue) -> {
            if (!newValue)                                  //the display doesn't restore if invalid info is entered repeatedly, this fixes that
                spinnerList.get(column).cancelEdit();        //note: cancelEdit() is really more like "update display" as implemented. this triggers it upon losing focus
        });                                                 //why this isn't default behavior I'll never know

        //adds each spinner to a VBox underneath its label, to keep the two connected as they move around
        Label spinnerHeader = new Label(timeSpinnerLabel);
        spinnerHeader.getStyleClass().add("smallText");
        if (column == 2 && row == 2)
            spinnerDates.add(spinnerHeader, column, row, 2, 1);
        else
            spinnerDates.add(spinnerHeader, column, row);
        spinnerDates.add(spinnerList.get(index), column, row + 1);
    }

    private static class RatingsListCell extends ListCell<Integer> {    //for displaying ratings in combobox
        @Override
        protected void updateItem(Integer item, boolean empty) {
            super.updateItem(item, empty);
            if (empty || item == null || item == 0) {                   //zero treated as blank/unused
                setText(null);
            } else {
                setText(item.toString());
            }
        }
    }

    private class TimelineCellListCell extends ListCell<Timeline> {         //for displaying timelines
        private Node cellNode;
        private TimelineCell cell;

        public TimelineCellListCell() {
            super();
            FXMLLoader loader = new FXMLLoader(getClass().getResource("../FXML/TimelineCell.fxml"));
            try {
                cellNode = loader.load();
                cell = loader.getController();
            } catch (IOException e) {
                System.err.println("Could not load TimelineCell.fxml");
            }
            cell.list = list;
            cell.filteredTimelines = filteredTimelines;

            this.setStyle("-fx-padding: 0px; -fx-border-width: 2px; -fx-border-color: transparent; -fx-border-style: solid;");

            this.selectedProperty().addListener((observable, oldValue, newValue) -> {
                cell.focused = newValue;
                cell.ratingBox.setDisable(!newValue);

                if (cell.timeline != null) {
                    cell.setBGImage();

                    if (newValue)
                        cell.pane.add(cell.cellButtonBox, 1, 0);
                    else
                        cell.pane.getChildren().remove(cell.cellButtonBox);
                }
            });
        }

        @Override
        protected void updateItem(Timeline item, boolean empty) {
            super.updateItem(item, empty);
            if (item != null && !empty) //test for null item and empty parameter
            {
                setGraphic(cellNode);
                if (cell != null)
                    cell.setTimeline(item);
            } else
                setGraphic(null);

            this.setOnMouseClicked(e -> {
                if (e.getClickCount() == 2)
                    cell.openTimeline();
            });
        }
    }
}
