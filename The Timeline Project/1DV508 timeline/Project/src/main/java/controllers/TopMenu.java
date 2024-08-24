package controllers;

import javafx.fxml.FXML;
import javafx.scene.control.Menu;

import java.io.IOException;


public class TopMenu {

    @FXML Menu loggedInStatus = new Menu();

    public void initialize() {
        updateLoggedInStatus();
    }

    @FXML
    void styleDefaultPressed() {
        GUIManager.applyStyle("Default");
    }

    @FXML
    void styleLightPressed() {
        GUIManager.applyStyle("Light");
    }

    @FXML
    void styleBluePressed() {
        GUIManager.applyStyle("Blue");
    }

    @FXML
    void styleDarkPressed() {
        GUIManager.applyStyle("Dark");
    }

    @FXML
    void styleMaroonPressed() {
        GUIManager.applyStyle("Maroon");
    }

    @FXML
    void updateLoggedInStatus() {
        if (GUIManager.loggedInUser == null) {
            loggedInStatus.setText("Not logged in");
            loggedInStatus.setDisable(true);
        } else {
            loggedInStatus.setText("Logged in as: " + GUIManager.loggedInUser.getUserEmail());
            loggedInStatus.setDisable(false);
        }
    }

    @FXML
    void logOutPressed() {
        GUIManager.loggedInUser = null;
        updateLoggedInStatus();
        GUIManager.applyStyle("Default");
        try {
            GUIManager.swapScene("LoginAndRegistration");
        } catch (IOException e) {
            System.err.println("Could not load Login Screen.");
            System.exit(1);
        }
    }
}
