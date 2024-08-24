package database;

import controllers.GUIManager;
import javafx.scene.control.Alert;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Timeline extends TimelineObject<Timeline> {
    private transient int timelineID;
    private int scale = 8;
    private String timelineName = "New Timeline";
    private String timelineDescription = "";
    private List<Event> eventList = new ArrayList<>();
    private List<String> keywords = new ArrayList<>();
    private transient double rating;                //average rating, transient because entire ratings list is exported
    private User owner;

    public Timeline() {
    }

    private Timeline(int timelineID, String timelineName, String timelineDescription, int scale,
                     LocalDateTime startDate, LocalDateTime endDate, LocalDateTime dateCreated, List<String> keywords,
                     List<Event> eventList, String imagePath, double rating, User owner) {
        this.timelineID = timelineID;
        this.timelineName = timelineName;
        this.scale = scale;
        this.timelineDescription = timelineDescription;
        this.startDate = startDate;
        this.endDate = endDate;
        this.creationDate = dateCreated;
        this.keywords = keywords;
        this.eventList = eventList;
        this.imagePath = imagePath;
        this.rating = rating;
        this.owner = owner;
    }

    @Override
    public Timeline createFromDB(ResultSet rs) throws SQLException {
        int timelineID = rs.getInt("TimelineID");
        int scale = rs.getInt("Scale");
        String timelineName = rs.getString("TimelineName");
        String timelineDescription = rs.getString("TimelineDescription");
        String imagePath = rs.getString("ImagePath");
        int startYear = rs.getInt("StartYear");
        int startMonth = rs.getInt("StartMonth");
        int startDay = rs.getInt("StartDay");
        int startHour = rs.getInt("StartHour");
        int startMinute = rs.getInt("StartMinute");
        int startSecond = rs.getInt("StartSecond");
        int startMillisecond = rs.getInt("StartMillisecond");
        int endYear = rs.getInt("EndYear");
        int endMonth = rs.getInt("EndMonth");
        int endDay = rs.getInt("EndDay");
        int endHour = rs.getInt("EndHour");
        int endMinute = rs.getInt("EndMinute");
        int endSecond = rs.getInt("EndSecond");
        int endMillisecond = rs.getInt("EndMillisecond");
        int createdYear = rs.getInt("CreatedYear");
        int createdMonth = rs.getInt("CreatedMonth");
        int createdDay = rs.getInt("CreatedDay");
        int createdHour = rs.getInt("CreatedHour");
        int createdMinute = rs.getInt("CreatedMinute");
        int createdSecond = rs.getInt("CreatedSecond");
        int createdMillisecond = rs.getInt("CreatedMillisecond");
        String keywordString = rs.getString("Keywords");

        LocalDateTime startDate = LocalDateTime.of(startYear, startMonth, startDay, startHour, startMinute, startSecond, startMillisecond * 1000000);
        LocalDateTime endDate = LocalDateTime.of(endYear, endMonth, endDay, endHour, endMinute, endSecond, endMillisecond * 1000000);
        LocalDateTime createdDate = LocalDateTime.of(createdYear, createdMonth, createdDay, createdHour, createdMinute, createdSecond,
                createdMillisecond * 1000000);

        // keyword list generation from comma string
        String[] words = keywordString.split(",");
        List<String> keywords = new ArrayList<>(Arrays.asList(words));

        List<Event> eventList;
        try (PreparedStatement stmt = DBM.conn.prepareStatement("SELECT e.* FROM events e " +
                "INNER JOIN timelineevents t " +
                "ON e.EventID = t.EventID " +
                "WHERE t.TimelineID = ?")) {
            stmt.setInt(1, timelineID);
            eventList = DBM.getFromDB(stmt, new Event());
        }

        double rating = calcRating(timelineID);

        PreparedStatement stat = DBM.conn.prepareStatement("SELECT * FROM users WHERE UserID = ?");
        stat.setInt(1, rs.getInt("TimelineOwner"));
        User owner = DBM.getFromDB(stat, new User()).get(0);

        return new Timeline(timelineID, timelineName, timelineDescription, scale, startDate, endDate, createdDate,
                keywords, eventList, imagePath, rating, owner);
    }

    @Override
    public PreparedStatement getInsertQuery() throws SQLException {
        return DBM.conn.prepareStatement(
                "INSERT INTO `timelines` ( `Scale`,`TimelineName`, `TimelineDescription`, `StartYear`,`StartMonth`,`StartDay`,`StartHour`"
                        + ",`StartMinute`,`StartSecond`,`StartMillisecond`,`EndYear`,`EndMonth`,`EndDay`,`EndHour`,`EndMinute`,`EndSecond`,"
                        + "`EndMillisecond`,`TimelineOwner`,`Keywords`,`ImagePath`) VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)",
                Statement.RETURN_GENERATED_KEYS);
    }

    @Override
    public PreparedStatement getUpdateQuery() throws SQLException {
        return DBM.conn.prepareStatement(
                "UPDATE `timelines` SET `Scale` = ?, `TimelineName` = ?, `TimelineDescription` = ?, "
                        + "`StartYear` = ?,  `StartMonth` = ?,  `StartDay` = ?,  `StartHour` = ?,  `StartMinute` = ?,  `StartSecond` = ?,  "
                        + "`StartMillisecond` = ?,    `EndYear` = ?,  `EndMonth` = ?,  `EndDay` = ?,  `EndHour` = ?,  `EndMinute` = ?,  "
                        + "`EndSecond` = ?,  `EndMillisecond` = ?, `TimelineOwner` = ?, `Keywords` = ?, `ImagePath` = ? WHERE (`TimelineID` = ?)");
    }

    @Override
    public void addToBatch(PreparedStatement stmt) throws SQLException {
        stmt.clearParameters();

        stmt.setInt(1, scale);
        stmt.setString(2, timelineName);
        stmt.setString(3, timelineDescription);
        stmt.setInt(4, startDate.getYear());
        stmt.setInt(5, startDate.getMonthValue());
        stmt.setInt(6, startDate.getDayOfMonth());
        stmt.setInt(7, startDate.getHour());
        stmt.setInt(8, startDate.getMinute());
        stmt.setInt(9, startDate.getSecond());
        stmt.setInt(10, startDate.getNano() / 1000000);
        stmt.setInt(11, endDate.getYear());
        stmt.setInt(12, endDate.getMonthValue());
        stmt.setInt(13, endDate.getDayOfMonth());
        stmt.setInt(14, endDate.getHour());
        stmt.setInt(15, endDate.getMinute());
        stmt.setInt(16, endDate.getSecond());
        stmt.setInt(17, endDate.getNano() / 1000000);
        stmt.setInt(18, getOwnerID());
        // keyword string generation from list
        StringBuilder sb = new StringBuilder();
        for (String s : keywords) {
            sb.append(s);
            sb.append(",");
        }
        stmt.setString(19, sb.toString());
        stmt.setString(20, imagePath);
        if (timelineID > 0)
            stmt.setInt(21, timelineID);

        stmt.addBatch();
    }

    @Override
    public PreparedStatement getDeleteQuery() throws SQLException {
        return DBM.conn.prepareStatement("DELETE FROM `timelines` WHERE (`TimelineID` = ?)");
    }

    public void deleteImage() {
        if (getImagePath() != null) {
            try {
                Files.deleteIfExists(Paths.get(getImagePath()));
            } catch (IOException e) {
                System.err.println("Could not access Timeline's image path.");
            }
        }
    }

    public void deleteOrphans() throws SQLException {
        try (PreparedStatement stmt = DBM.conn.prepareStatement("DELETE e.* FROM `timelines` t " +
                "LEFT JOIN timelineevents te " +
                "ON t.TimelineID = te.TimelineID " +                //destroys this timeline's about-to-be orphaned events (i.e. events where there are no
                "LEFT JOIN events e " +                             //junction table records for them with a different TimelineID
                "ON te.EventID = e.EventID AND e.EventID NOT IN (SELECT EventID FROM timelineevents WHERE TimelineID != ?) " +
                "WHERE t.TimelineID = ? AND e.EventID IS NOT NULL")) {
            stmt.setInt(1, timelineID);
            stmt.setInt(2, timelineID);
            stmt.execute();
        }
    }

    public void rateTimeline(int index) throws SQLException {
        if (GUIManager.loggedInUser.getID() == getOwnerID()) {
            Alert confirmDelete = new Alert(Alert.AlertType.INFORMATION);
            confirmDelete.setTitle("Rating Failed");
            confirmDelete.setHeaderText("You may not rate your own timeline.");

            confirmDelete.showAndWait();
            return;
        }

        if (checkIfRated()) {
            updateRating(index, GUIManager.loggedInUser.getID());
        } else {
            addRating(index, GUIManager.loggedInUser.getID());
        }
    }

    public void addRating(int rating, int userId) throws SQLException {
        PreparedStatement out = DBM.conn.prepareStatement("INSERT INTO ratings (`Rating`, `UserId`, `TimeLineID`) VALUES (?, ?, ?)");
        out.setInt(1, rating);
        out.setInt(2, userId);
        out.setInt(3, this.timelineID);
        out.execute();
    }

    public void updateRating(int rating, int userId) throws SQLException {
        PreparedStatement out = DBM.conn.prepareStatement("UPDATE ratings SET `Rating` = ? WHERE (`UserId` = ? AND `TimeLineID` = ?)");
        out.setInt(1, rating);
        out.setInt(2, userId);
        out.setInt(3, this.timelineID);
        out.execute();
    }

    public boolean checkIfRated() throws SQLException {
        PreparedStatement rate = DBM.conn.prepareStatement("SELECT COUNT(*) FROM ratings WHERE UserID = ? AND TimeLineID = ?");
        rate.setInt(1, GUIManager.loggedInUser.getID());
        rate.setInt(2, this.getID());
        ResultSet rs = rate.executeQuery();
        rs.next();
        return rs.getInt(1) > 0;
    }

    public void updateRatingFromDB() throws SQLException {
        this.rating = calcRating();
    }

    double calcRating() throws SQLException {
        return calcRating(getID());
    }

    double calcRating(int id) throws SQLException {
        PreparedStatement rate = DBM.conn.prepareStatement("SELECT AVG(Rating) FROM ratings WHERE TimeLineID = ?");
        rate.setInt(1, id);
        ResultSet rs = rate.executeQuery();
        rs.next();
        return rs.getDouble(1);
    }

    @Override
    public String toString() {
        return "Name: " + timelineName + " Description: " + timelineDescription;
    }

    // Getters/Setters
    public int getID() {
        return this.timelineID;
    }

    @Override
    public void setID(int id) {
        this.timelineID = id;
    }

    @Override
    public int getOwnerID() {
        return owner.getID();
    }

    @Override
    public void setOwnerID(int ownerID) {
        owner.setID(ownerID);
    }

    @Override
    public String getDescription() {
        return this.timelineDescription;
    }

    @Override
    public void setDescription(String description) {
        this.timelineDescription = description;
    }

    @Override
    public String getName() {
        return this.timelineName;
    }

    @Override
    public void setName(String name) {
        this.timelineName = name;
    }

    public double getRating() {
        return rating;
    }

    public User getOwner() {
        return owner;
    }

    public void setOwner(User owner) {
        this.owner = owner;
    }

    public int getScale() {
        return this.scale;
    }

    public void setScale(int scale) {
        this.scale = scale;
    }

    public List<String> getKeywords() {
        return keywords;
    }

    public void setKeywords(List<String> keywords) {
        this.keywords = keywords;
    }

    public List<Event> getEventList() {
        return eventList;
    }

    public boolean equals(Timeline other) {
        if (this.timelineID == 0)
            return false;
        return this.timelineID == other.timelineID;
    }
}