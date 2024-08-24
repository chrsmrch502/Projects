package controllers;

import com.google.gson.Gson;
import database.DBM;
import database.JSONTimeline;
import database.Timeline;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Side;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import org.apache.commons.io.FileUtils;
import utils.ImageUtils;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Optional;

public class TimelineEditor extends Editor {
    private final ObservableList<String> keywords = FXCollections.observableArrayList();
    @FXML HBox keywordBox;
    @FXML ComboBox<String> timeInput;
    @FXML ListView<String> keywordView;
    @FXML Label feedbackText;
    @FXML Button exportButton;
    @FXML ContextMenu exportPopup;
    @FXML MenuItem exportJSONButton;
    @FXML TextField keywordInput;
    private Timeline timeline;

    @Override
    public void initialize() {
        super.initialize();
        outPath = "src/main/resources/images/timeline/";

        toggleEditable(false);
        keywordView.setItems(keywords);

        timeInput.valueProperty().addListener(e -> {
            setExpansion(startPane, startBoxes, startExpanded, timeInput.getSelectionModel().getSelectedIndex() + 1);
            setExpansion(endPane, endBoxes, endExpanded, timeInput.getSelectionModel().getSelectedIndex() + 1);
        });

        // Get list of scales
        try {
            PreparedStatement state = DBM.conn.prepareStatement("SELECT Unit FROM scales");
            timeInput.setItems(FXCollections.observableArrayList(DBM.getFromDB(state, rs -> rs.getString("unit"))));
        } catch (SQLException e) {
            e.printStackTrace();
        }

        if (!GUIManager.loggedInUser.getAdmin())
            exportPopup.getItems().remove(exportJSONButton);        //only admins can export
        exportButton.setContextMenu(null);                  //later we reapply context menu via onAction so it works with different buttons (MenuButton was not cooperating with CSS)

        GUIManager.mainStage.setTitle("Timeline Editor");
    }

    void setTimeline(Timeline timeline) {
        this.timeline = timeline;
        itemInEditor = timeline;

        setOwner(GUIManager.loggedInUser.getID() == timeline.getOwnerID());        // Check if Admin
        populateDisplay();
    }

    @Override
    void toggleEditable(boolean editable) {
        super.toggleEditable(editable);

        keywordBox.setDisable(!editable);
        timeInput.setDisable(!editable);
    }

    @Override
    void populateDisplay() {
        super.populateDisplay(); // populate inputs common to editors

        if (timeline.getKeywords() != null) {
            keywords.clear();
            keywords.addAll(timeline.getKeywords());
            keywords.sort(String::compareTo);
        } else
            timeline.setKeywords(FXCollections.observableArrayList());
        timeInput.getSelectionModel().select(timeline.getScale() > 0 ? timeline.getScale() - 1 : 4);
    }

    @Override
    void updateItem() {
        super.updateItem(); // update variables common to TimelineObjects

        timeline.getKeywords().clear();
        timeline.getKeywords().addAll(keywords);

        feedbackText.setText("");

        timeline.setScale((timeInput.getSelectionModel().getSelectedIndex()) + 1);
        parentController.setActiveTimeline(timeline);
        parentController.eventSelectorController.populateTimelineList();
    }

    @FXML
    boolean deleteTimeline() {
        Alert confirmDelete = new Alert(Alert.AlertType.CONFIRMATION);
        confirmDelete.setTitle("Confirm Delete");
        confirmDelete.setHeaderText("This will delete your timeline permanently!");
        confirmDelete.setContentText("Are you ok with this?");

        Optional<ButtonType> result = confirmDelete.showAndWait();

        if (result.isPresent() && result.get() == ButtonType.CANCEL)
            return false;

        try {
            DBM.deleteFromDB(timeline);
            GUIManager.swapScene("Dashboard");
            return true;
        } catch (SQLException | IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    boolean hasChanges() {
        if (super.hasChanges())
            return true;

        if (timeline.getKeywords().size() != keywords.size())
            return true;
        if (timeline.getScale() != timeInput.getSelectionModel().getSelectedIndex() + 1)
            return true;
        for (int i = 0; i < keywords.size(); i++)
            if (timeline.getKeywords().get(i).compareTo(keywords.get(i)) != 0)
                return true;

        return false;
    }

    boolean isUniqueKeyword(String k) {
        for (String s : keywords) {
            if (k.equalsIgnoreCase(s))
                return false;
        }
        return true;
    }

    @FXML
    void addKeyword() {
        String inputWord = keywordInput.getText();
        inputWord = inputWord.replace(",", " ");
        if (inputWord.isBlank()) {
            feedbackText.setText("Keyword cannot be empty!");
        } else {
            if (!isUniqueKeyword(inputWord)) {
                feedbackText.setText("Keyword already exists!");
            } else {
                keywords.add(inputWord);
                feedbackText.setText("Keyword " + inputWord + " added");
                keywords.sort(String::compareTo);
                keywordInput.setText("");
            }
        }
    }

    @FXML
    void removeKeyword() {
        if (keywordView.getSelectionModel().getSelectedIndex() < 0) {
            feedbackText.setText("No keyword selected!");
        } else {
            String removedWord = keywordView.getSelectionModel().getSelectedItem();
            keywords.remove(keywordView.getSelectionModel().getSelectedIndex());
            feedbackText.setText("Keyword " + removedWord + " removed!");
            keywordView.getSelectionModel().select(-1);
        }
    }

    @Override
    boolean save() {
        updateItem();
        super.save();
        parentController.populateDisplay();
        parentController.eventSelectorController.populateDisplay();
        parentController.eventSelectorController.setTimelineSelected(timeline);
        return true;
    }

    boolean isOkayToLeavePage() {
        if (editable && hasChanges() && saveConfirm())      //when admin tries to leave, ask if they want to save changes
            if (validData())                                //if data is valid, save, otherwise prevent leaving
                return save();
            else
                return false;
        return true;                                        //if they don't want to save, just let them leave
    }

    @Override
    boolean validData() {
        if (timeInput.getSelectionModel().getSelectedIndex() >= 0)
            return super.validData();
        else {
            Alert confirmDelete = new Alert(Alert.AlertType.INFORMATION);
            confirmDelete.setTitle("Invalid Units");
            confirmDelete.setHeaderText("A time unit must be selected.");
            confirmDelete.setContentText("Make sure to selected a time unit appropriate for your timeline before saving.");

            confirmDelete.showAndWait();
            return false;
        }
    }

    @Override
    boolean validImage(File imageChosen) {      //adds a resolution check to the regular image validation
        if (!super.validImage(imageChosen))
            return false;
        if (!validResolution(imageChosen)) {
            ImageResolutionNotification();
            return false;
        }
        return true;
    }

    private boolean validResolution(File file) {
        try {
            BufferedImage imageToCheck = ImageIO.read(file);
            if (imageToCheck.getHeight() < 600)
                return false;
            return (imageToCheck.getWidth() >= 800);
        } catch (IOException e) {
            System.err.println("Could not read image.");
            return false;
        }
    }

    private void ImageResolutionNotification() {
        Alert resolutionSaveImage = new Alert(Alert.AlertType.INFORMATION);
        resolutionSaveImage.setTitle("Too low resolution for timeline image");
        resolutionSaveImage.setHeaderText("Resolution of the picture is too low. Minimum resolution is 800x600");
        resolutionSaveImage.showAndWait();
    }

    @FXML
    void snapshotEntireTimeline() {
        imageExport(false);
    }

    @FXML
    void snapshotCurrentView() {
        imageExport(true);
    }

    private void imageExport(boolean currentViewOnly) {
        Stage imageExport = new Stage();
        imageExport.setTitle("Export Image");
        imageExport.initOwner(GUIManager.mainStage);         //These two lines make sure you can't click back to the timeline window,
        imageExport.initModality(Modality.WINDOW_MODAL);     //so you can't have 10 windows open at once.

        try {
            FXMLLoader loader = new FXMLLoader(GUIManager.class.getResource("../FXML/ImageExport.fxml"));
            imageExport.setScene(new Scene(loader.load()));
            ImageExport imageExportObject = loader.getController();
            imageExportObject.setUp(parentController.snapshot(currentViewOnly), parentController.activeTimeline);

            imageExport.getScene().getStylesheets().addAll(GUIManager.mainStage.getScene().getStylesheets());
            imageExport.show();
        } catch (IOException e) {
            System.err.println("Could not start image export");
        }
    }

    @FXML
    void jsonExport() {
        FileChooser chooser = new FileChooser();            //open FileChooser for user to choose save location
        chooser.setTitle("Save Timeline as JSON");
        chooser.setInitialFileName(ImageUtils.convertToSafeFileName(parentController.activeTimeline.getName()));
        chooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("JSON", "*.json"));
        File outFile = chooser.showSaveDialog(GUIManager.mainStage);

        if (outFile == null)                //usually only the case if user cancels out of the file chooser
            return;

        try {
            Gson gson = JSONTimeline.getGson();
            JSONTimeline exportable = new JSONTimeline(parentController.activeTimeline);    //gather all relevant information about a timeline into one object
            String outJSON = gson.toJson(exportable);                                       //convert that to JSON-formatted String

            FileUtils.writeStringToFile(outFile, outJSON, (String) null);                  //save output
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    void openExportMenu() {
        exportPopup.show(exportButton, Side.RIGHT, 0, 0);
    }
}
