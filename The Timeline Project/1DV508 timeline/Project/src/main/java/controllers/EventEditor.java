package controllers;

import database.Event;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.util.StringConverter;

import java.sql.SQLException;
import java.util.Optional;

public class EventEditor extends Editor {


    @FXML CheckBox hasDuration = new CheckBox();
    @FXML Slider prioritySlider;
    Event event;


    @Override
    public void initialize() {
        super.initialize();
        outPath = "src/main/resources/images/event/";

        //set up priority slider labels
        prioritySlider.setLabelFormatter(new StringConverter<>() {
            @Override
            public String toString(Double n) {
                if (n < 0.5) return "None";
                if (n < 1.5) return "Low";
                if (n < 2.5) return "Medium";
                if (n < 3.5) return "High";

                return "None";
            }

            //probably not used but required for the override
            @Override
            public Double fromString(String s) {
                switch (s) {
                    case "Low":
                        return 1d;
                    case "Medium":
                        return 2d;
                    case "High":
                        return 3d;
                    default:
                        return 0d;
                }
            }
        });
    }

    @FXML
    void toggleHasDuration() {              //toggles whether the event has a distinct end date, as opposed to being instant
        endPane.setDisable(!hasDuration.isSelected());

        setExpansion(endPane, endBoxes, hasDuration.isSelected() && endExpanded, parentController.activeTimeline.getScale());   //compresses if duration is disabled, if enabled leave it as user wanted

        if (hasDuration.isSelected()) {
            populateEndInputs();
            endPane.getStyleClass().clear();
        } else {
            for (int i = 0; i < 7; i++) {
                endInputs.get(i).getValueFactory().setValue(startInputs.get(i).getValue());
            }
            endPane.getStyleClass().add("DisabledAnyways");
        }
    }

    @Override
    void toggleEditable(boolean editable) {
        super.toggleEditable(editable);
        hasDuration.setDisable(!editable);
        prioritySlider.setDisable(!editable);
    }

    void setEvent(Event event) {
        parentController.rightSidebar.getChildren().remove(editor);
        this.event = event;
        itemInEditor = event;
        if (this.event.getID() == 0)       //if new event, set current user as owner
            this.event.setOwnerID(GUIManager.loggedInUser.getID());
        setOwner(GUIManager.loggedInUser.getID() == this.event.getOwnerID());
        populateDisplay();
    }

    @Override
    void populateDisplay() {
        super.populateDisplay();    //populate inputs common to editors

        hasDuration.setSelected(event.getStartDate().compareTo(event.getEndDate()) < 0);       //has no duration if start==end
        toggleHasDuration();

        prioritySlider.setValue(event.getEventPriority());
    }

    @Override
    void updateItem() {                 //sets object's values based on input fields' values
        super.updateItem();        //update variables common to TimelineObjects
        if (!hasDuration.isSelected()) {
            event.setEndDate(event.getStartDate());
            populateEndInputs();
        }

        event.setEventPriority((int) prioritySlider.getValue());
    }

    @Override
    boolean save() {
        updateItem();
        boolean newEvent = event.getID() == 0;

        super.save();          //adds to database

        if (newEvent)
            addToTimeline();        //new event is automatically added to active timeline when saved
        parentController.eventSelectorController.populateDisplay();
        parentController.populateDisplay();
        return true;
    }

    private void addToTimeline() {
        parentController.activeTimeline.getEventList().add(event);
        try {
            if (event.addToTimeline(parentController.activeTimeline.getID()))
                System.out.println("Event added to " + parentController.activeTimeline + " timeline."); // remove this later once more user feedback is implemented
            else
                System.out.println("Event is already on " + parentController.activeTimeline + " timeline.");
        } catch (SQLException e) {
            System.out.println("Timeline not found in database.");
        }
    }

    @FXML
    boolean deleteEvent() {
        parentController.eventSelectorController.deleteEvent(event);
        return parentController.rightSidebar.getChildren().remove(editor);
    }

    @Override
    boolean hasChanges() {
        if (!hasDuration.isSelected() && event.getStartDate().compareTo(event.getEndDate()) != 0)
            return true;
        if (super.hasChanges())
            return true;
        return event.getEventPriority() != prioritySlider.getValue();
    }

    @FXML
    boolean close() {
        if (event != null && hasChanges())
            if (closeConfirm()) {         //save and exit or just exit?
                if (validData())
                    save();
                else
                    return false;
            }
        return parentController.rightSidebar.getChildren().remove(editor);
    }

    @FXML
    boolean closeConfirm() {
        ButtonType yes = new ButtonType("OK", ButtonBar.ButtonData.OK_DONE);
        ButtonType no = new ButtonType("Cancel", ButtonBar.ButtonData.CANCEL_CLOSE);

        Alert confirmSave = new Alert(Alert.AlertType.CONFIRMATION, "Would you like to save changes before closing?", yes, no);

        confirmSave.setTitle("Confirm Close");
        confirmSave.setHeaderText("You have unsaved changes!");

        Optional<ButtonType> result = confirmSave.showAndWait();

        return result.isPresent() && result.get() != no;
    }
}