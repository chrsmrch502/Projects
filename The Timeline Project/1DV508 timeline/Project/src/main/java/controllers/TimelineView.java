package controllers;

import database.DBM;
import database.Event;
import database.Timeline;
import javafx.embed.swing.SwingFXUtils;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.scene.SnapshotParameters;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.control.*;
import javafx.scene.image.WritableImage;
import javafx.scene.input.ScrollEvent;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.text.Text;
import utils.DateUtils;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;
import java.util.*;

public class TimelineView {
    private final List<EventNode> eventList = new ArrayList<>();
    @FXML Slider zoomSlider;
    @FXML TextField zoomLabel;
    @FXML GridPane timelineGrid;
    @FXML ScrollPane mainScrollPane;
    @FXML StackPane rightSidebar;
    @FXML StackPane leftSidebar;
    @FXML StackPane centeringStack;
    @FXML TimelineEditor timelineEditorController;
    @FXML EventSelector eventSelectorController;
    @FXML EventEditor eventEditorController;
    Timeline activeTimeline;

    /*Initializes the timeline view window - sets the timeline and controller for the event selector and event editor*/
    public void initialize() {
        timelineEditorController.setParentController(this);
        eventSelectorController.setParentController(this);
        eventEditorController.setParentController(this);

        leftSidebar.getChildren().add(timelineEditorController.editor);
        rightSidebar.getChildren().add(eventSelectorController.selector);

        centeringStack.addEventFilter(ScrollEvent.ANY, event -> {
            if (event.isControlDown())              //zoom when control + scrolling
                scrollHandler(event);
        });

        zoomSlider.valueProperty().addListener(e -> zoomSlider());

        zoomLabel.textProperty().addListener((obs, oldV, newV) -> { //enforces zoom label formatting to percent
            if (!newV.matches("[\\d]*%"))
                zoomLabel.setText(oldV);
        });

        zoomLabel.setOnMouseClicked(e -> {
            if (e.getClickCount() == 2)
                zoom(1);
        });
    }

    /*Sets the active timeline (the timeline being displayed)*/
    void setActiveTimeline(Timeline t) {
        this.activeTimeline = t;
        timelineEditorController.setTimeline(t);
        eventSelectorController.setTimelineSelected(activeTimeline); // sets the selected index to the currently viewed timeline
        if (activeTimeline.getID() > 0)
            populateDisplay();
    }

    /*Pulls the timeline from the database and populates the display*/
    void populateDisplay() {
        try (PreparedStatement stmt = DBM.conn.prepareStatement("SELECT * FROM timelines where TimelineID = ?")) {
            stmt.setInt(1, activeTimeline.getID());
            activeTimeline = DBM.getFromDB(stmt, new Timeline()).get(0);
        } catch (SQLException e) {
            System.err.println("Could not update timeline from database.");
        }
        timelineGrid.getChildren().clear();
        timelineGrid.getColumnConstraints().clear();
        setupTimeline();
    }

    /*Calculates sets the length of the timeline itself by adding columns to the pane which holds the timeline
     * This is computed depending on the start date, end date and the units that has been chosen for the timeline*/
    private void setupTimeline() {
        int numberOfCol = DateUtils.distanceBetween(activeTimeline.getStartDate(), activeTimeline.getEndDate(), activeTimeline.getScale());
        if (numberOfCol > 1000 && !timelineTooBigAskIfContinueAnyways())         //if timeline is too large and will likely crash, offer to not load it visually
            return;

        Pane mainLine = new Pane();
        mainLine.setMaxHeight(25);
        mainLine.getStyleClass().add("timeline");

        int start = 1, frequency = 1;

        switch (activeTimeline.getScale()) {
            case 8:
                frequency = 2;
                start = activeTimeline.getStartDate().getYear();
                break;
            case 9:
                start = activeTimeline.getStartDate().getYear() / 10;
                break;
            case 10:
                start = activeTimeline.getStartDate().getYear() / 100;
                break;
            case 11:
                start = activeTimeline.getStartDate().getYear() / 1000;
                break;
        }

        ColumnConstraints[] constraints = new ColumnConstraints[numberOfCol];
        Arrays.fill(constraints, new ColumnConstraints(70));
        timelineGrid.getColumnConstraints().addAll(constraints);

        for (int i = 0; i <= numberOfCol; i += frequency) {
            timelineGrid.add(new Text(String.valueOf(i + start)), i, 0);
        }

        if (numberOfCol >= 1)                                                               //if the start date is later than the end date, numberOfCol would be negative,
            timelineGrid.add(mainLine, 0, 0, numberOfCol, 1);    //which does not work for the amount of columns
        GridPane.setMargin(mainLine, new Insets(25, 0, -25, 0));

        setupEventNodes();
    }

    private boolean timelineTooBigAskIfContinueAnyways() {
        Alert confirmLoad = new Alert(Alert.AlertType.CONFIRMATION);
        confirmLoad.setTitle("Size Warning!");
        confirmLoad.setHeaderText("This timeline is very large and may fail to load!");
        confirmLoad.setContentText("Would you like to load the view anyways?");
        Optional<ButtonType> result = confirmLoad.showAndWait();

        return result.isPresent() && result.get() == ButtonType.OK;
    }

    /*Sets up the events in the right side bar*/
    private void setupEventNodes() {
        eventList.clear();
        EventNode newNode;
        for (Event e : activeTimeline.getEventList()) {
            newNode = addEvent(e);
            if (newNode != null)
                eventList.add(newNode);
        }
        Collections.sort(eventList);            //sort in order of priority, then earlier events first, then longer events
        for (int i = 0; i < eventList.size(); i++)
            placeEvent(eventList.get(i), i);
    }

    private EventNode addEvent(Event event) {
        try {
            FXMLLoader nodeLoader = new FXMLLoader(getClass().getResource("../FXML/EventNode.fxml"));
            nodeLoader.load();
            EventNode newNode = nodeLoader.getController();
            newNode.setActiveEvent(event, activeTimeline, this);
            return newNode;
        } catch (IOException e) {
            System.err.println("Could not load EventNode.fxml");
            return null;
        }
    }

    /*Places the even on the timeline in the correct column*/
    private void placeEvent(EventNode newNode, int eventsPlacedCount) {
        if (newNode.getStartColumn() < 0) {                                        //if node starts before the timeline begins, cut the beginning
            newNode.setColumnSpan(newNode.getColumnSpan() + newNode.getStartColumn());
            newNode.setStartColumn(0);
        }
        if (newNode.getStartColumn() + newNode.getColumnSpan() > timelineGrid.getColumnCount()) // if node goes past the timeline's end, cut the end
            newNode.setColumnSpan(timelineGrid.getColumnCount() - newNode.getStartColumn() - 1);
        if (newNode.getColumnSpan() < 1)                                            //if, after cutting, nothing remains, don't display it at all
            return;

        boolean[] usedRows = new boolean[timelineGrid.getRowCount()];               //array to mark rows as occupied or not to the given event node
        EventNode eventBeingChecked;
        for (int i = 0; i < eventsPlacedCount; i++) {                               //check previous nodes to see if they occupy desired columns
            eventBeingChecked = eventList.get(i);
            if (eventBeingChecked.getStartColumn() < newNode.getStartColumn() + newNode.getColumnSpan()                     //if a previous node on current row starts before the new one would end
                    && eventBeingChecked.getStartColumn() + eventBeingChecked.getColumnSpan() > newNode.getStartColumn())   //and it ends after the new one starts
                usedRows[eventBeingChecked.getRow()] = true;                                                                //then mark that row as occupied for the desired columns
        }

        int row;
        for (row = 1; row < usedRows.length; row++) {           //find the first unoccupied (unmarked) row and place the new event there
            if (!usedRows[row])                                 //storing occupied rows in an array and traversing that is far faster than re-traversing the eventList for larger timelines
                break;
        }
        newNode.setRow(row);
        timelineGrid.add(newNode.getDisplayPane(), newNode.getStartColumn(), row, newNode.getColumnSpan(), 1);
    }

    @FXML
    void returnToDashboard() {
        if (!timelineEditorController.isOkayToLeavePage())
            return;

        try {
            GUIManager.swapScene("Dashboard");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    WritableImage snapshot(boolean currentViewOnly) {
        SnapshotParameters snapshotParams = new SnapshotParameters();

        //Read the current color used for TimelineGrid background
        Color backgroundColor = Color.decode("#" + mainScrollPane.getBackground().getFills().get(0).getFill().toString().substring(2, 8));
        snapshotParams.setFill(mainScrollPane.getBackground().getFills().get(0).getFill());

        //take snapshot, either current view or whole timeline depending on selection
        WritableImage workingImage = currentViewOnly ? snapshotCurrentView(snapshotParams) : snapshotWholeTimeline(snapshotParams);

        //add burn-in padding and return, to be sent to preview window
        return addPadding(workingImage, backgroundColor);
    }

    private WritableImage snapshotCurrentView(SnapshotParameters snapshotParams) {
        mainScrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);     //temporarily remove scroll bars for snapshot
        mainScrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        WritableImage out = mainScrollPane.snapshot(snapshotParams, new WritableImage(
                (int) mainScrollPane.getLayoutBounds().getWidth(), (int) mainScrollPane.getLayoutBounds().getHeight()));
        mainScrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        mainScrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        return out;
    }

    private WritableImage snapshotWholeTimeline(SnapshotParameters snapshotParams) {
        double currentScale = timelineGrid.getScaleX();
        timelineGrid.setScaleX(1);                          //temporarily set scale to default for snapshot
        timelineGrid.setScaleY(1);
        WritableImage out = timelineGrid.snapshot(snapshotParams, new WritableImage(
                (int) timelineGrid.getLayoutBounds().getWidth(), (int) timelineGrid.getLayoutBounds().getHeight()));
        timelineGrid.setScaleX(currentScale);
        timelineGrid.setScaleY(currentScale);
        return out;
    }

    //create buffered image and add padding on top and bottom
    private WritableImage addPadding(WritableImage workingImage, Color backgroundColor) {
        BufferedImage fromFXImage = SwingFXUtils.fromFXImage(workingImage, null);

        // Calculate width, height, offset
        int width = fromFXImage.getWidth();
        int height = (int) (fromFXImage.getHeight() * 1.30);
        int offset = (int) (fromFXImage.getHeight() * 0.15);

        // Create another image with new height & width
        BufferedImage backImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);

        Graphics2D imageOverlay = backImage.createGraphics();
        imageOverlay.setColor(backgroundColor);
        imageOverlay.fillRect(0, 0, width, height);                        //Fill the background with color
        imageOverlay.drawImage(fromFXImage, 0, offset, null);         //Overlay with image from offset
        imageOverlay.dispose();

        return SwingFXUtils.toFXImage(backImage, null);
    }

    void zoom(double newScale) {
        zoom(newScale, mainScrollPane.getHvalue(), mainScrollPane.getVvalue());
    }

    void zoom(double newScale, double scrollHvalue, double scrollVvalue) {
        timelineGrid.setScaleX(newScale);                                           //apply scaling/zooming
        timelineGrid.setScaleY(newScale);

        mainScrollPane.layout();                                                    //update contents based on new scale, which automatically jumps the view around

        mainScrollPane.setHvalue(scrollHvalue);                                     //apply (adjusted) snapshots of scrollbar positions, overriding the jumping
        mainScrollPane.setVvalue(scrollVvalue);

        zoomLabel.setText((int) (newScale * 100) + "%");                            //update zoom label when zoom changes
        zoomSlider.setValue(newScale * 100);
    }

    private void scrollHandler(ScrollEvent event) {
        final double scaleFactor = 1.2;

        double oldScale = timelineGrid.getScaleX();
        double newScale = event.getDeltaY() > 0 ? oldScale * scaleFactor : oldScale / scaleFactor; //calculate new scale based on old
        newScale = clampScale(newScale);

        event.consume();                                                            //consume the mouse event to prevent normal scrollbar functions

        double hMousePosition = (event.getX() / centeringStack.getWidth());         //record mouse position for "zoom to mouse"
        double vMousePosition = (event.getY() / centeringStack.getHeight());

        double weight = Math.pow(oldScale / newScale, 2);
        double adjustedHvalue = mainScrollPane.getHvalue() * weight     //snapshot scrollbar positions before resizing moves them
                + hMousePosition * (1 - weight);                        //adjust snapshots based on mouse position, weighted average of old position and mouse position,
        double adjustedVvalue = mainScrollPane.getVvalue() * weight
                + vMousePosition * (1 - weight);                        //weight becomes negative to zoom out "away" from mouse

        zoom(newScale, adjustedHvalue, adjustedVvalue);
    }

    private void zoomSlider() {
        double newScale = clampScale(zoomSlider.getValue() / 100);          //values are displayed as percent to user
        zoom(newScale);
    }

    @FXML
    void decrementZoom() {
        zoomSlider.decrement();
    }

    @FXML
    void incrementZoom() {
        zoomSlider.increment();
    }

    @FXML
    void zoomLabel() {                  //type to input zoom level
        String zoomString = (zoomLabel.getText().replace("%", ""));
        if (zoomString.isEmpty())
            zoomString = "100";

        double newScale = clampScale(Double.parseDouble(zoomString) / 100);          //values are displayed as percent to user
        zoom(newScale);
    }

    private double clampScale(double scale) {
        if (scale > 5)                      //max zoom is 5x
            return 5;
        return Math.max(scale, .001);       //min zoom is 1/100x
    }
}
