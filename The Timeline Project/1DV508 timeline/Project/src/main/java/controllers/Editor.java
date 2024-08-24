package controllers;

import database.DBM;
import database.TimelineObject;
import javafx.fxml.FXML;
import javafx.geometry.Side;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.util.StringConverter;
import org.apache.commons.io.FileUtils;
import utils.ImageUtils;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public abstract class Editor {
    final List<VBox> startBoxes = new ArrayList<>();
    final List<Spinner<Integer>> startInputs = new ArrayList<>();
    final List<VBox> endBoxes = new ArrayList<>();
    final List<Spinner<Integer>> endInputs = new ArrayList<>();
    @FXML Button deleteImageButton;
    @FXML Button uploadImageButton;
    @FXML ImageView image;
    @FXML HBox editor;
    @FXML TextField titleInput = new TextField();
    @FXML TextArea descriptionInput = new TextArea();
    @FXML Button saveEditButton;
    @FXML Button deleteButton;
    @FXML FlowPane startPane;
    @FXML FlowPane endPane;
    boolean editable = true;
    boolean startExpanded;
    boolean endExpanded;
    TimelineView parentController;
    String imageFilePath;
    TimelineObject<?> itemInEditor;
    String outPath;

    public void initialize() {
        editor.getStylesheets().add("styles/DisabledViewable.css");

        //Set Up the Spinners for Start/End Inputs, would have bloated the .fxml and variable list a ton if these were in fxml
        setupTimeInputStartAndEnd("Year", -999999999, 999999999, 0);
        setupTimeInputStartAndEnd("Month", 1, 12, 1);
        setupTimeInputStartAndEnd("Day", 1, 31, 2);
        setupTimeInputStartAndEnd("Hour", 0, 23, 3);
        setupTimeInputStartAndEnd("Minute", 0, 59, 4);
        setupTimeInputStartAndEnd("Second", 0, 59, 5);
        setupTimeInputStartAndEnd("Millisecond", 0, 999, 6);

        ContextMenu fullImagePopup = new ContextMenu();
        MenuItem fullImageContainer = new MenuItem();
        fullImagePopup.getItems().add(fullImageContainer);              //makes full-sized image appear on double click
        image.setOnMouseClicked(e -> {
            if (e.getClickCount() == 2) {
                fullImageContainer.setGraphic(new ImageView(new Image("file:" + imageFilePath)));
                fullImagePopup.show(image, Side.BOTTOM, 0, 0);
            }
            e.consume();        //so editor doesn't also receive double click
        });
        
        editor.setOnMouseClicked(e -> {
            if (e.getClickCount() == 2 && GUIManager.loggedInUser.getID() == this.itemInEditor.getOwnerID())
                saveEditButton();
        });
    }

    @FXML
    void toggleStartExpanded() {        //toggles whether the spinners for inputting start date are expanded
        startExpanded = !startExpanded;
        setExpansion(startPane, startBoxes, startExpanded, parentController.activeTimeline.getScale());
    }

    @FXML
    void toggleEndExpanded() {        //toggles whether the spinners for inputting end date are expanded
        endExpanded = !endExpanded;
        setExpansion(endPane, endBoxes, endExpanded, parentController.activeTimeline.getScale());
    }

    void setExpansion(FlowPane expandPane, List<VBox> boxesToAddFrom, boolean expanding, int scale) {
        expandPane.getChildren().removeAll(boxesToAddFrom);         //clear out the current contents except the expansion button

        if (expanding) {                //if expanding, add everything in
            expandPane.getChildren().addAll(0, boxesToAddFrom);

        } else {                        //if contracting, add based on scale
            if (scale == 1)
                expandPane.getChildren().add(0, boxesToAddFrom.get(6)); //milliseconds
            if (scale <= 2)
                expandPane.getChildren().add(0, boxesToAddFrom.get(5)); //seconds
            if (scale <= 3)
                expandPane.getChildren().add(0, boxesToAddFrom.get(4)); //minutes
            if (scale >= 2 && scale <= 4)
                expandPane.getChildren().add(0, boxesToAddFrom.get(3)); //hours
            if (scale >= 3 && scale <= 7)
                expandPane.getChildren().add(0, boxesToAddFrom.get(2)); //days
            if (scale >= 4 && scale <= 8)
                expandPane.getChildren().add(0, boxesToAddFrom.get(1)); //months
            if (scale >= 5)
                expandPane.getChildren().add(0, boxesToAddFrom.get(0)); //years
        }
        expandPane.getChildren();
    }

    void setParentController(TimelineView parentController) {
        this.parentController = parentController;
    }

    @FXML
    boolean saveEditButton() {
        if (editable && hasChanges()) {
            if (validData() && saveConfirm())           //if unsaved changes, try to save
                save();
            else
                return false;                           //if save cancelled, don't change mode
        }

        toggleEditable(!editable);
        return true;
    }

    void toggleEditable(boolean editable) {
        this.editable = editable;

        uploadImageButton.setDisable(!editable);
        deleteImageButton.setDisable(!editable);

        titleInput.setEditable(editable);
        descriptionInput.setEditable(editable);
        for (Spinner<Integer> s : startInputs)
            s.setDisable(!editable);
        for (Spinner<Integer> s : endInputs)
            s.setDisable(!editable);
        saveEditButton.setText(editable ? "Save" : "Edit");
    }

    @FXML
    boolean saveConfirm() {
        Alert confirmSave = new Alert(Alert.AlertType.CONFIRMATION);

        confirmSave.setTitle("Confirm Save");
        confirmSave.setHeaderText("Would you like to save?");
        confirmSave.setContentText("This will make permanent changes!");

        Optional<ButtonType> result = confirmSave.showAndWait();

        return result.isPresent() && result.get() == ButtonType.OK;
    }

    boolean validData() {
        LocalDateTime newStartDate = LocalDateTime.of(startInputs.get(0).getValue(), startInputs.get(1).getValue(), startInputs.get(2).getValue(),
                startInputs.get(3).getValue(), startInputs.get(4).getValue(), startInputs.get(5).getValue(), startInputs.get(6).getValue());

        LocalDateTime newEndDate = LocalDateTime.of(endInputs.get(0).getValue(), endInputs.get(1).getValue(), endInputs.get(2).getValue(),
                endInputs.get(3).getValue(), endInputs.get(4).getValue(), endInputs.get(5).getValue(), endInputs.get(6).getValue());
        boolean hasNoDuration = (this instanceof EventEditor && !((EventEditor) this).hasDuration.isSelected());

        if (titleInput.getText() == null || titleInput.getText().isEmpty()) {
            Alert confirmDelete = new Alert(Alert.AlertType.INFORMATION);
            confirmDelete.setTitle("Invalid Name");
            confirmDelete.setHeaderText("Name input blank.");
            confirmDelete.setContentText("Make sure to input a name before saving.");

            confirmDelete.showAndWait();
            return false;
        } else if (titleInput.getText().length() > 100) {
            Alert confirmDelete = new Alert(Alert.AlertType.INFORMATION);
            confirmDelete.setTitle("Invalid Name");
            confirmDelete.setHeaderText("Name input too long.");
            confirmDelete.setContentText("Please choose a shorter name.");

            confirmDelete.showAndWait();
            return false;
        } else if (!hasNoDuration && newStartDate.compareTo(newEndDate) > 0) {
            Alert confirmDelete = new Alert(Alert.AlertType.INFORMATION);
            confirmDelete.setTitle("Invalid Dates");
            confirmDelete.setHeaderText("The End Date must be after the Start Date.");
            confirmDelete.setContentText("Make sure to check your dates before saving.");

            confirmDelete.showAndWait();
            return false;
        }
        return true;
    }

    void setOwner(boolean owner) {
        saveEditButton.setDisable(!owner);
        deleteButton.setDisable(!owner);
    }

    void populateDisplay() {
        if (itemInEditor.getImagePath() != null)
            image.setImage(new Image("File:" + itemInEditor.getImagePath()));
        else
            image.setImage(null);

        imageFilePath = itemInEditor.getImagePath();

        titleInput.setText(itemInEditor.getName());
        descriptionInput.setText(itemInEditor.getDescription());

        if (itemInEditor.getStartDate() != null) {
            startInputs.get(0).getValueFactory().setValue(itemInEditor.getStartDate().getYear());
            startInputs.get(1).getValueFactory().setValue(itemInEditor.getStartDate().getMonthValue());
            startInputs.get(2).getValueFactory().setValue(itemInEditor.getStartDate().getDayOfMonth());
            startInputs.get(3).getValueFactory().setValue(itemInEditor.getStartDate().getHour());
            startInputs.get(4).getValueFactory().setValue(itemInEditor.getStartDate().getMinute());
            startInputs.get(5).getValueFactory().setValue(itemInEditor.getStartDate().getSecond());
            startInputs.get(6).getValueFactory().setValue(itemInEditor.getStartDate().getNano() / 1000000);

            populateEndInputs();
        }

        setExpansion(startPane, startBoxes, false, parentController.activeTimeline.getScale());
        setExpansion(endPane, endBoxes, false, parentController.activeTimeline.getScale());
    }

    void populateEndInputs() {            //so that end dates can have their display toggled separately, useful for events
        endInputs.get(0).getValueFactory().setValue(itemInEditor.getEndDate().getYear());
        endInputs.get(1).getValueFactory().setValue(itemInEditor.getEndDate().getMonthValue());
        endInputs.get(2).getValueFactory().setValue(itemInEditor.getEndDate().getDayOfMonth());
        endInputs.get(3).getValueFactory().setValue(itemInEditor.getEndDate().getHour());
        endInputs.get(4).getValueFactory().setValue(itemInEditor.getEndDate().getMinute());
        endInputs.get(5).getValueFactory().setValue(itemInEditor.getEndDate().getSecond());
        endInputs.get(6).getValueFactory().setValue(itemInEditor.getEndDate().getNano() / 1000000);
    }

    void updateItem() {                  //sets object's values based on input fields' values
        if (itemInEditor.getImagePath() != null && (!itemInEditor.getImagePath().equals(imageFilePath))) {     //deletes old picture if it has been replaced
            try {
                Files.deleteIfExists(Paths.get(itemInEditor.getImagePath()));
            } catch (IOException er) {
                er.printStackTrace();
            }
        }

        itemInEditor.setImage(imageFilePath);

        itemInEditor.setName(titleInput.getText());
        itemInEditor.setDescription(descriptionInput.getText().replaceAll("([^\r])\n", "$1\r\n"));

        itemInEditor.setStartDate(LocalDateTime.of(startInputs.get(0).getValue(), startInputs.get(1).getValue(), startInputs.get(2).getValue(),
                startInputs.get(3).getValue(), startInputs.get(4).getValue(), startInputs.get(5).getValue(), startInputs.get(6).getValue() * 1000000));

        itemInEditor.setEndDate(LocalDateTime.of(endInputs.get(0).getValue(), endInputs.get(1).getValue(), endInputs.get(2).getValue(),
                endInputs.get(3).getValue(), endInputs.get(4).getValue(), endInputs.get(5).getValue(), endInputs.get(6).getValue() * 1000000));
    }

    boolean hasChanges() {           //returns true if any input fields don't match the object's values
        if ((itemInEditor.getImagePath() == null) ? imageFilePath != null : !itemInEditor.getImagePath().equals(imageFilePath))     //if item's pic is null, then filename not null
            return true;

        if (!itemInEditor.getName().equals(titleInput.getText())
                || !itemInEditor.getDescription().equals(descriptionInput.getText().replaceAll("([^\r])\n", "$1\r\n")))      //textArea tends to change the newline from \r\n to just \n which breaks some things)
            return true;

        LocalDateTime readStart = LocalDateTime.of(startInputs.get(0).getValue(), startInputs.get(1).getValue(), startInputs.get(2).getValue(),
                startInputs.get(3).getValue(), startInputs.get(4).getValue(), startInputs.get(5).getValue(), startInputs.get(6).getValue() * 1000000);

        LocalDateTime readEnd = LocalDateTime.of(endInputs.get(0).getValue(), endInputs.get(1).getValue(), endInputs.get(2).getValue(),
                endInputs.get(3).getValue(), endInputs.get(4).getValue(), endInputs.get(5).getValue(), endInputs.get(6).getValue() * 1000000);

        return (
                itemInEditor.getStartDate().compareTo(readStart) != 0
                        || itemInEditor.getEndDate().compareTo(readEnd) != 0
        );
    }

    boolean save() {
        try {
            if (itemInEditor.getID() == 0) {
                DBM.insertIntoDB(itemInEditor);
            } else
                DBM.updateInDB(itemInEditor);
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    private void setupTimeInputStartAndEnd(String timeSpinnerLabel, int minValue, int maxValue, int index) {    //applies equivalent setups to both start and end spinners
        setupTimeInput(timeSpinnerLabel, minValue, maxValue, index, startInputs, startBoxes);
        setupTimeInput(timeSpinnerLabel, minValue, maxValue, index, endInputs, endBoxes);
    }

    //creates spinners to handle dates with appropriate min/max values and invalid input handling
    private void setupTimeInput(String timeSpinnerLabel, int minValue, int maxValue, int index, List<Spinner<Integer>> spinnerList, List<VBox> boxList) {
        int initValue = (timeSpinnerLabel.equals("Year")) ? 0 : minValue;   //initial value is equal to minimum, except in the case of years

        SpinnerValueFactory.IntegerSpinnerValueFactory valueFactory = new SpinnerValueFactory.IntegerSpinnerValueFactory(minValue, maxValue, initValue);
        valueFactory.setConverter(new StringConverter<>() {                 //makes spinners revert to default values in case of invalid input
            @Override
            public String toString(Integer value) {     //called by spinner to update the displayed value in the box
                if (value == null)
                    return String.valueOf(initValue);
                return value.toString();
            }

            @Override
            public Integer fromString(String string) {  //called by spinner to read the value from the box and convert to int
                try {
                    if (string == null)
                        return initValue;
                    string = string.trim();
                    if (string.length() < 1)
                        return initValue;
                    return Integer.parseInt(string);

                } catch (NumberFormatException ex) {
                    return initValue;
                }
            }
        });

        spinnerList.add(index, new Spinner<>(valueFactory));
        spinnerList.get(index).setEditable(true);
        spinnerList.get(index).focusedProperty().addListener((observableValue, oldValue, newValue) -> {
            if (!newValue)                                  //the display doesn't restore if invalid info is entered repeatedly, this fixes that
                spinnerList.get(index).cancelEdit();        //note: cancelEdit() is really more like "update display" as implemented. this triggers it upon losing focus
        });                                                 //why this isn't default behavior I'll never know

        //adds each spinner to a VBox underneath its label, to keep the two connected as they move around
        boxList.add(index, new VBox(new Label(timeSpinnerLabel), spinnerList.get(index)));
        boxList.get(index).setPrefWidth(70);
        boxList.get(index).getChildren().get(0).getStyleClass().add("smallText");
    }
    ////////////////////==================================Common image handling========================================////////////////////

    @FXML
    void uploadImage() throws IOException {
        if (itemInEditor.getImagePath() != null && !imageSaveConfirm())             //if item has image, ask user if they want to delete it
            return;

        File imageChosen = ImageUtils.openFileChooser();                            //use file chooser to let user open a local image

        if (validImage(imageChosen)) {                                              //if valid image
            byte[] imageFileContent = FileUtils.readFileToByteArray(imageChosen);   //save locally and display in editor
            imageFilePath = ImageUtils.saveImage(imageFileContent, outPath + imageChosen.getName());
            image.setImage(new Image("File:" + imageFilePath));
        }
    }

    boolean imageSaveConfirm() {
        Alert confirmSaveImage = new Alert(Alert.AlertType.CONFIRMATION);
        confirmSaveImage.setTitle("Confirm Change");
        confirmSaveImage.setHeaderText("Replacing or removing an image will permanently delete it from the system.");
        confirmSaveImage.setContentText("Would you like to make this change?");

        Optional<ButtonType> result = confirmSaveImage.showAndWait();
        return result.isPresent() && result.get() == ButtonType.OK;
    }

    boolean validImage(File imageChosen) {
        if (imageChosen == null)                //usually only null if user cancelled out of the the fileChooser
            return false;
        if (!imageChosen.getName().substring(imageChosen.getName().lastIndexOf(".") + 1).matches("(jpeg|JPEG|png|jpg|bmp|gif|wbmp)")) {
            wrongFormatNotification();          //check for formats we don't support
            return false;
        }
        return true;
    }

    void wrongFormatNotification() {
        Alert formatNotification = new Alert(Alert.AlertType.INFORMATION);
        formatNotification.setTitle("Non-image file");
        formatNotification.setHeaderText("The picture has to be .jpg, .jpeg, .png, .bmp, .gif");
        formatNotification.setContentText("Please provide an image file");
        formatNotification.showAndWait();
    }

    @FXML
    void clearImage() {             //wipes image from the Editor's memory
        imageFilePath = null;       //note that the object, database, and local image file will remain until the user saves the change
        image.setImage(null);
    }
}
