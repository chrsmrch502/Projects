package controllers;

import database.Event;
import database.Timeline;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.Pane;
import utils.DateUtils;

public class EventNode implements Comparable<EventNode> {

    @FXML Pane displayPane;
    @FXML Label eventNameDisplay;
    @FXML Tooltip hoverFlag;
    private TimelineView parentController;
    private Event activeEvent;
    private int startColumn;
    private int columnSpan;
    private int row;

    public void initialize() {
        hoverFlag.setShowDelay(hoverFlag.getShowDelay().divide(8));
    }

    void setActiveEvent(Event event, Timeline activeTimeline, TimelineView parentController) {
        this.activeEvent = event;
        this.parentController = parentController;

        setStartColumn(DateUtils.distanceBetween(activeTimeline.getStartDate(), activeEvent.getStartDate(), activeTimeline.getScale()));
        setColumnSpan(Math.max(DateUtils.distanceBetween(activeEvent.getStartDate(), activeEvent.getEndDate(), activeTimeline.getScale()), 1));   //instant events still need 1 whole column
        eventNameDisplay.setText(activeEvent.getName());
        hoverFlag.setText(activeEvent.getName() + "\n" + activeEvent.getDescription());
    }

    @FXML
    void openEventViewer() {       //upon clicking a node
        parentController.eventEditorController.setEvent(activeEvent);
        parentController.eventEditorController.toggleEditable(false);
        parentController.rightSidebar.getChildren().add(parentController.eventEditorController.editor);
    }

    @Override
    public int compareTo(EventNode o) {     //sorts by highest priority first, then earlier start, then by longest span as tiebreakers
        if (this.activeEvent.getEventPriority() != o.activeEvent.getEventPriority())
            return o.activeEvent.getEventPriority() - this.activeEvent.getEventPriority();
        if (this.startColumn != o.startColumn)
            return this.startColumn - o.startColumn;
        if (this.columnSpan != o.columnSpan)
            return o.columnSpan - this.columnSpan;
        return this.activeEvent.getID() - o.activeEvent.getID();        //if events are basically identical, at least force consistent placement by sorting by ID
    }

    Pane getDisplayPane() {
        return displayPane;
    }

    void setStartColumn(int startColumn) {
        this.startColumn = startColumn;
    }

    void setColumnSpan(int columnSpan) {
        this.columnSpan = columnSpan;
    }

    int getRow() {
        return row;
    }

    void setRow(int row) {
        this.row = row;
    }

    int getStartColumn() {
        return startColumn;
    }

    int getColumnSpan() {
        return columnSpan;
    }
}