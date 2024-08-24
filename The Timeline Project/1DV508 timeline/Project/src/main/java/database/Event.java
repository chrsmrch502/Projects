package database;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDateTime;

public class Event extends TimelineObject<Event> {
    private transient int eventID = 0;
    private int eventPriority = 0;
    private String eventName = "New Event";
    private String eventDescription = "";
    transient int ownerID;

    public Event() {
    }

    private Event(int eventID, int ownerID, LocalDateTime startDate, LocalDateTime endDate, LocalDateTime creationDate, String title, String description, String imagePath, int eventPriority) {      //for reading from database
        this.eventID = eventID;
        this.ownerID = ownerID;
        this.startDate = startDate;
        this.endDate = endDate;
        this.creationDate = creationDate;
        this.eventName = title;
        this.eventDescription = description;
        this.imagePath = imagePath;
        this.eventPriority = eventPriority;
    }

    @Override
    public Event createFromDB(ResultSet rs) throws SQLException {
        int eventID = rs.getInt("EventID");
        String imagePath = rs.getString("ImagePath");
        int ownerID = rs.getInt("EventOwner");
        String eventName = rs.getString("EventName");
        String eventDescription = rs.getString("EventDescription");
        int startYear = rs.getInt("StartYear");
        int startMonth = rs.getInt("StartMonth");
        int startDay = rs.getInt("StartDay");
        int startHour = rs.getInt("StartHour");
        int startMinute = rs.getInt("StartMinute");
        int startSecond = rs.getInt("StartSecond");
        int startNano = rs.getInt("StartMillisecond") * 1000000;
        int endYear = rs.getInt("EndYear");
        int endMonth = rs.getInt("EndMonth");
        int endDay = rs.getInt("EndDay");
        int endHour = rs.getInt("EndHour");
        int endMinute = rs.getInt("EndMinute");
        int endSecond = rs.getInt("EndSecond");
        int endNano = rs.getInt("EndMillisecond") * 1000000;
        int createdYear = rs.getInt("CreatedYear");
        int createdMonth = rs.getInt("CreatedMonth");
        int createdDay = rs.getInt("CreatedDay");
        int createdHour = rs.getInt("CreatedHour");
        int createdMinute = rs.getInt("CreatedMinute");
        int createdSecond = rs.getInt("CreatedSecond");
        int createdNano = rs.getInt("CreatedMillisecond") * 1000000;
        LocalDateTime startDate = LocalDateTime.of(startYear, startMonth, startDay, startHour, startMinute, startSecond, startNano);
        LocalDateTime endDate = LocalDateTime.of(endYear, endMonth, endDay, endHour, endMinute, endSecond, endNano);
        LocalDateTime createdDate = LocalDateTime.of(createdYear, createdMonth, createdDay, createdHour, createdMinute, createdSecond, createdNano);
        int EventPriority = rs.getInt("EventPriority");

        return new Event(eventID, ownerID, startDate, endDate, createdDate, eventName, eventDescription, imagePath, EventPriority);
    }


    @Override
    public PreparedStatement getInsertQuery() throws SQLException, RuntimeException {
        return DBM.conn.prepareStatement("INSERT INTO `events` (`EventName` , `EventDescription` , `ImagePath`, " +
                "`StartYear`,  `StartMonth`,  `StartDay`,  `StartHour`,  `StartMinute`, `StartSecond`,  `StartMillisecond`, " +
                "`EndYear`,  `EndMonth`,  `EndDay`,  `EndHour`,  `EndMinute`,  `EndSecond`,  `EndMillisecond`, `EventOwner`, " +
                "`EventPriority`) VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?);", Statement.RETURN_GENERATED_KEYS);
    }

    @Override
    public PreparedStatement getUpdateQuery() throws SQLException {
        return DBM.conn.prepareStatement("UPDATE `events` SET `EventName` = ?, `EventDescription` = ?, `ImagePath` = ?, " +
                "`StartYear` = ?,  `StartMonth` = ?,  `StartDay` = ?,  `StartHour` = ?,  `StartMinute` = ?,  `StartSecond` = ?,  " +
                "`StartMillisecond` = ?, `EndYear` = ?,  `EndMonth` = ?,  `EndDay` = ?,  `EndHour` = ?,  `EndMinute` = ?,  " +
                "`EndSecond` = ?, `EndMillisecond` = ?, `EventOwner` = ?,  `EventPriority` = ?  WHERE (`EventID` = ?);");
    }

    @Override
    public void addToBatch(PreparedStatement stmt) throws SQLException {
        stmt.clearParameters();

        stmt.setString(1, eventName);
        stmt.setString(2, eventDescription);
        stmt.setString(3, imagePath);
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
        stmt.setInt(18, ownerID);
        stmt.setInt(19, eventPriority);
        if (eventID > 0)
            stmt.setInt(20, eventID);

        stmt.addBatch();
    }

    @Override
    public PreparedStatement getDeleteQuery() throws SQLException {
        return DBM.conn.prepareStatement("DELETE FROM `events` WHERE (`EventID` = ?)");
    }

    @Override
    public void deleteImage() {
        // Deleting the images
        if (getImagePath() != null) {
            try {
                Files.deleteIfExists(Paths.get(getImagePath()));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public boolean addToTimeline(int timelineID) throws SQLException {  //IGNORE suppresses warnings, adding a dupe simply fails and returns false
        PreparedStatement out = DBM.conn.prepareStatement("INSERT IGNORE INTO `timelineevents` (`TimelineID`, `EventID`) VALUES (?, ?);");
        out.setInt(1, timelineID);
        out.setInt(2, this.eventID);
        return out.executeUpdate() > 0;
    }

    public void removeFromTimeline(int timelineID) throws SQLException {
        PreparedStatement stmt = DBM.conn.prepareStatement("DELETE FROM `timelineevents` WHERE EventID = ? AND TimelineID = ?");
        stmt.setInt(1, eventID);
        stmt.setInt(2, timelineID);
        stmt.executeUpdate();
        deleteIfOrphan();
    }

    public void deleteIfOrphan() throws SQLException {    //destroy if orphaned (i.e. not present on any timeline)
        try (PreparedStatement stmt = DBM.conn.prepareStatement("DELETE e FROM events e " +
                "LEFT JOIN timelineevents t on e.EventID = t.EventID " +
                "WHERE t.TimelineID IS NULL AND e.EventID = ?;")) {
            stmt.setInt(1, eventID);
            stmt.execute();
        }
    }

    @Override
    public int getID() {
        return this.eventID;
    }

    @Override
    public void setID(int id) {
        this.eventID = id;
    }

    @Override
    public int getOwnerID() {
        return ownerID;
    }

    @Override
    public void setOwnerID(int ownerID) {
        this.ownerID = ownerID;
    }

    @Override
    public String getDescription() {
        return this.eventDescription;
    }

    @Override
    public void setDescription(String description) {
        this.eventDescription = description;
    }

    @Override
    public String getName() {
        return this.eventName;
    }

    @Override
    public void setName(String name) {
        this.eventName = name;
    }

    public int getEventPriority() {
        return eventPriority;
    }

    public void setEventPriority(int eventPriority) {
        this.eventPriority = eventPriority;
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof Event))
            return false;
        if (this.getID() <= 0)
            return false;
        return this.getID() == ((Event) o).getID();
    }

    @Override
    public String toString() {
        return "EventID: " + eventID + " EventName " + eventName + " EventDescription " + eventDescription + " Start : " + startDate + " End : " + endDate + " Created: " + creationDate;
    }
}
