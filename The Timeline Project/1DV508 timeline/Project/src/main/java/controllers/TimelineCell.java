package controllers;

import database.DBM;
import database.Timeline;
import database.User;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Polygon;

import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class TimelineCell {

    @FXML Button cellDeleteTimelineButton;
    @FXML VBox cellButtonBox;
    @FXML GridPane pane;
    @FXML HBox ratingBox;
    @FXML Label title;
    @FXML Label description;
    @FXML Label keywords;
    @FXML Label author;
    List<Polygon> ratingButtons;
    Timeline timeline;
    protected FilteredList<Timeline> filteredTimelines;
    protected ListView<Timeline> list;
    protected User user;
    boolean focused = false;

    public void initialize() {
        // Ratings
        ratingButtons = new ArrayList<>(5);
        for (int i = 0; i < 5; i++) {
            ratingButtons.add((Polygon) ratingBox.getChildren().get(i));    // grab each polygon from the HBox and set it up as a pseudo-button
            setupRatingButton(ratingButtons.get(i), i);
        }

        ratingBox.setOnMouseEntered(e -> ratingBox.setOpacity(1));
        ratingBox.setOnMouseMoved(e -> colorStarsByRating((int) Math.ceil(e.getX() * 5 / ratingBox.getWidth())));   //highlight current star and ones to the left
        ratingBox.setOnMouseExited(e -> {
            colorStarsByRating((int) Math.ceil(timeline.getRating()));  //return highlighting to normal
            ratingBox.setOpacity((timeline.getRating() > 1) ? 1 : 0);
        });
        pane.getChildren().remove(cellButtonBox);
        ratingBox.setDisable(true);
    }

    private void setupRatingButton(Polygon button, int index) {
        double starSize = 15;
        int numPoints = 5;

        button.getPoints().clear();
        double angle = 0;
        double distance;

        for (int i = 0; i < numPoints * 2; i++) {   //calculate position of each point on star, starting from top and going clockwise
            if (i % 2 == 0)
                distance = starSize;                //tips stick out further
            else
                distance = starSize / 2;            //intersections don't stick out as much, increase number to increase how "sharp" the star is

            button.getPoints().addAll(Math.sin(angle) * distance, //trig to find point position, easier to adjust than manual point placement
                    Math.cos(angle) * distance * -1);

            angle += Math.PI / numPoints;            //simplified (2*PI / numPoints*2), rotates angle to calculate next tip or intersection
        }

        button.setOnMouseClicked(event -> {
            try {
                timeline.rateTimeline(index + 1);       //click a star to submit a rating and update its display value
                timeline.updateRatingFromDB();
            } catch (SQLException exception) {
                System.err.println("Could not access rating from database.");
            }
            colorStarsByRating((int) Math.ceil(timeline.getRating()));
        });
    }

    private void colorStarsByRating(int rating) {
        for (int i = 0; i < 5; i++) {
            ratingButtons.get(i).setFill((i < rating) ? Color.YELLOW : Color.GREY);   //yellow stars until rating reached, then grey
        }
    }

    void update() {
        if (timeline != null) {
            populateTimelineDetails();
            setBGImage();
            colorStarsByRating((int) Math.ceil(timeline.getRating()));
            ratingBox.setOpacity((timeline.getRating() > 1) ? 1 : 0);
            cellDeleteTimelineButton.setDisable(timeline.getOwner().getID() != GUIManager.loggedInUser.getID());
        }
    }

    void populateTimelineDetails() {
        title.setText("Title: " + timeline.getName());
        author.setText("By: " + user.getUserName());
        description.setText("Description: " + timeline.getDescription());

        StringBuilder keyWords = new StringBuilder();
        keyWords.append("Keywords: ");
        for (String s : timeline.getKeywords())
            keyWords.append(s).append(", ");
        if (keyWords.length() >= 12)
            keyWords.delete(keyWords.length() - 2, keyWords.length());
        keywords.setText(keyWords.toString());

        if (focused) {
            if (!pane.getChildren().contains(description)) {    //If the cell is focused and doesn't show the description
                pane.add(description, 0, 1);
                pane.add(keywords, 0, 2);
            }
        } else if (pane.getChildren().contains(description))    //If the cell is not focused and is still showing the description
            pane.getChildren().removeAll(description, keywords);
    }

    void setTimeline(Timeline timeline) {
        this.timeline = timeline;
        user = timeline.getOwner();
        this.update();
    }

    void setBGImage() {
        String imageURL = timeline.getImagePath() != null ? "url(file:" + timeline.getImagePath() + ")" : null;
        int height = focused ? 400 : 80;
        pane.setStyle(" -fx-padding: 5px; -fx-background-image: " + imageURL + "; -fx-pref-width: " + (list.getWidth() - 6) + "px; -fx-pref-height: " + height + "px;  -fx-background-size: " + (list.getWidth() - 6) + "px, stretch;");
    }

    @FXML
    boolean deleteTimeline() {
        Alert confirmDeleteTimeline = new Alert(Alert.AlertType.CONFIRMATION);
        confirmDeleteTimeline.setTitle("Confirm Deletion");
        confirmDeleteTimeline.setHeaderText("Are you sure you want to delete " + timeline.getName() + "?");
        confirmDeleteTimeline.setContentText("This can not be undone.");

        Optional<ButtonType> result = confirmDeleteTimeline.showAndWait();

        if (result.isPresent() && result.get() == ButtonType.CANCEL)
            return false;
        else {
            try {
                timeline.deleteOrphans();
                DBM.deleteFromDB(timeline);
            } catch (SQLException e) {
                e.printStackTrace();
            }
            filteredTimelines.getSource().remove(timeline);
            list.getSelectionModel().clearSelection();
            return true;
        }
    }

    @FXML
    TimelineView openTimeline() {
        try {
            TimelineView timelineView = GUIManager.swapScene("TimelineView");
            timelineView.setActiveTimeline(timeline);
            return timelineView;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }
}
