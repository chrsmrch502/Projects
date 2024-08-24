package controllers;

import database.DBM;
import database.User;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.text.Text;
import javafx.stage.Stage;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Comparator;
import java.util.List;

public class AdminRoleManager {
    @FXML Text userText;
    @FXML ListView<User> userListView;
    @FXML CheckBox toggle;
    @FXML ComboBox<String> sortBy;
    @FXML TextField searchInput;
    final ObservableList<User> userList = FXCollections.observableArrayList();

    public void initialize() {
        // ComboBox items (observable list)
        final ObservableList<String> sortOptions = FXCollections.observableArrayList();
        sortOptions.add("Alphabetically");
        sortOptions.add("Reverse-Alphabetically");
        sortOptions.add("User ID");
        sortOptions.add("Reverse User ID");
        sortBy.setItems(sortOptions);

        /* Define what is shown in the user list (User ID and email for now)*/
        userListView.setCellFactory(param -> new ListCell<>() {
            @Override
            protected void updateItem(User item, boolean empty) {
                super.updateItem(item, empty);

                if (empty || item == null || item.getUserEmail() == null) {
                    setText(null);
                } else {
                    setText("ID: " + item.getID() + " - " + item.getUserEmail());
                }

                this.setOnMouseClicked(e -> {
                    if (e.getClickCount() == 2) {
                        toggleClicked();
                        updateCheckBox();
                    }
                });
            }
        });

        fillListView();
        updateCheckBox();

        /*Listener for dropdown menu, that sort the user list depending on the dropdown index*/
        userListView.getSelectionModel().selectedIndexProperty().addListener(ov -> {

            if (userListView.getSelectionModel().getSelectedIndex() >= 0) {
                userText.setText(userListView.getSelectionModel().getSelectedItem().getUserEmail());
                updateCheckBox();
            }
        });

        // sort order selection events
        sortBy.getSelectionModel().selectedIndexProperty().addListener(ov -> {
            switch (sortBy.getSelectionModel().getSelectedIndex()) {
                case 0:
                    userList.sort(Comparator.comparing(User::getUserName));
                    break;
                case 1:
                    userList.sort((t1, t2) -> (t2.getUserName().compareTo(t1.getUserName())));
                    break;
                case 2:
                    userList.sort(Comparator.comparingInt(User::getID));
                    break;
                case 3:
                    userList.sort((t1, t2) -> (Integer.compare(t2.getID(), t1.getID())));
                    break;
            }
        });
    }

    /*Searches the database for input matches, currently supports username and email*/
    @FXML
    void search() {
        try {
            String sql = "SELECT * FROM users WHERE UserName LIKE '%" + searchInput.getText() + "%' OR UserEmail LIKE'%" + searchInput.getText() + "%'";
            PreparedStatement search = DBM.conn.prepareStatement(sql);
            List<User> userList = DBM.getFromDB(search, new User());
            userListView.setItems(FXCollections.observableArrayList(userList));
            //userListView.refresh();
        } catch (SQLException e) {
            System.err.println("Could not access users database.");
        }
    }

    /*Fills the user list with the users from the database*/
    void fillListView() {
        try {
            List<User> usersFromDB = DBM.getFromDB(DBM.conn.prepareStatement("SELECT * FROM users "), new User());
            userList.addAll(usersFromDB);
        } catch (SQLException e) {
            System.err.println("Could not access users database.");
        }
        userListView.setItems(userList);
        userListView.getSelectionModel().select(0);
    }

    /*Updates the checkbox that shows whether a user is an admin*/
    void updateCheckBox() {
        toggle.setSelected(userListView.getSelectionModel().getSelectedItem().getAdmin());
        toggle.setDisable(userListView.getSelectionModel().getSelectedItem().getID() <= 2);
    }

    /*Sets the switches between admin and non-admin for the selected user*/
    @FXML
    void toggleClicked() {
        if (userListView.getSelectionModel().getSelectedItem().getID() <= 2)
            return;
        userListView.getSelectionModel().getSelectedItem().toggleAdmin();

        try {
            DBM.updateInDB(userListView.getSelectionModel().getSelectedItem());
        } catch (SQLException e) {
            System.err.println("Could not access users database.");
        }
    }

    /*Connects the admin GUI and to the dashboard*/
    @FXML
    void back() {
        ((Stage) userListView.getScene().getWindow()).close();
    }

}
