package database;

import utils.PasswordEncryption;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;

public class User implements DBObject<User> {
    private transient int userID = 0;
    private String userEmail;
    private String userName;
    private String encryptedPass;
    private String salt;
    private String theme;
    private boolean admin;

    public User() {
    }

    //For creating new users, password gets encrypted before storage
    public User(String name, String email, String password) {
        setUserName(name);
        setUserEmail(email);
        setPassword(password);
    }

    //For reading from database only, don't use for new user creation
    private User(int userID, String name, String email, String encryptedPass, String salt, Boolean admin, String theme) {
        setID(userID);
        setUserName(name);
        setUserEmail(email);
        setAdmin(admin);
        setTheme(theme);
        this.encryptedPass = encryptedPass;
        this.salt = salt;
    }

    @Override
    public User createFromDB(ResultSet rs) throws SQLException {
        int userID = rs.getInt("UserID");
        String name = rs.getString("UserName");
        String email = rs.getString("UserEmail");
        String encryptedPass = rs.getString("Password");
        String salt = rs.getString("Salt");
        boolean admin = rs.getBoolean("Admin");
        String theme = rs.getString("Theme");

        return new User(userID, name, email, encryptedPass, salt, admin, theme);
    }

    @Override
    public PreparedStatement getInsertQuery() throws SQLException {
        return DBM.conn.prepareStatement("INSERT INTO `users` (`UserName`, `UserEmail`, `Password`, `Salt`, `Admin`, `Theme`) VALUES (?,?,?,?,?,?)", Statement.RETURN_GENERATED_KEYS);
    }

    @Override
    public PreparedStatement getUpdateQuery() throws SQLException {
        return DBM.conn.prepareStatement("UPDATE `users` SET `UserName` = ?, `UserEmail` = ?, `Password` = ?, `Salt` = ?, `Admin` = ?, `Theme` = ? WHERE (`UserID` = ?)");
    }

    @Override
    public void addToBatch(PreparedStatement stmt) throws SQLException {
        stmt.clearParameters();

        stmt.setString(1, userName);
        stmt.setString(2, userEmail);
        stmt.setString(3, encryptedPass);
        stmt.setString(4, salt);
        stmt.setBoolean(5, admin);
        stmt.setString(6, theme);
        if (userID > 0)
            stmt.setInt(7, userID);

        stmt.addBatch();
    }

    @Override
    public PreparedStatement getDeleteQuery() throws SQLException {
        return DBM.conn.prepareStatement("DELETE FROM `users` WHERE (`UserID` = ?)");
    }

    @Override
    public void deleteImage() {
        //didn't end up implementing profile pictures
    }

    public String getUserEmail() {
        return this.userEmail;
    }

    public void setUserEmail(String userEmail) throws IllegalArgumentException {
        if (!(userEmail.matches("\\p{all}+@[\\p{all}]+\\.\\p{all}+")))      //if it doesn't match chars@chars.chars
            throw new IllegalArgumentException("Invalid email format");
        this.userEmail = userEmail;
    }

    //static check so new objects don't need to be created to validate
    public static boolean validateUnique(String email) throws SQLException {
        if (!(email.matches("\\p{all}+@[\\p{all}]+\\.\\p{all}+")))         //if it doesn't match chars@chars.chars
            throw new IllegalArgumentException("Invalid email format");
        List<String> dbList = DBM.getFromDB(DBM.conn.prepareStatement("SELECT UserEmail FROM users"), rs -> rs.getString("UserEmail"));

        for (String db : dbList) {
            if (email.equalsIgnoreCase(db)) {
                return false;
            }
        }
        return true;
    }

    @Override
    public void setID(int id) {
        this.userID = id;
    }

    @Override
    public int getID() {
        return userID;
    }

    public void toggleAdmin() {
        admin = !admin;
    }

    public String getUserName() { //Not sure about this one
        return this.userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public void setAdmin(Boolean admin) {
        this.admin = admin;
    }

    public boolean getAdmin() {
        return this.admin;
    }

    public void setTheme(String theme) {
        this.theme = theme;
    }

    public String getTheme() {
        return theme;
    }

    public void setPassword(String pass) throws IllegalArgumentException {
        //We can split the regex down to be more specific in the error handling - no need for all possibilities, just one at a time.
        if (!(pass.matches("^(?=.*\\p{Digit})(?=.*\\p{Ll})(?=.*\\p{Lu})(?=.*\\p{Punct})(?=\\S+$).{8,}$")))//rules in order,at least: one digit, one lower case, one upper case,  one special character, no white space and min length 8
            throw new IllegalArgumentException("Invalid password, must include at least: one digit, one lower case, one upper case, one special character, no white space and be at least 8 character long");
        this.salt = PasswordEncryption.getSalt(30); //Length of the salt string
        this.encryptedPass = PasswordEncryption.generateSecurePassword(pass, this.salt);
    }

    public Boolean verifyPass(String pass, String encrypted, String salt) {
        return PasswordEncryption.verifyUserPassword(pass, encrypted, salt);   //salt in DB associated with the encrypted password there and created with setPassword
    }

    //getters for pass and salt

    public String getEncrypted() {
        return this.encryptedPass;
    }

    public String getSalt() {
        return this.salt;
    }

    //Two methods for junit test only - if I set private, have to learn how to mock them if possible

    public String getEncryptedForTest() { //This method only when I am testing the getInsertQuery() and getUpdateQuery()
        return this.encryptedPass;
    }

    public String getSaltForTest() {
        return this.salt;
    }

    @Override
    public String toString() {
        return "User ID: " + userID + " Name: " + userName + " Email: " + userEmail;
    }
}