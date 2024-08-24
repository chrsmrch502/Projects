package database;

import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;
import org.apache.commons.io.FileUtils;
import utils.ImageUtils;

import java.io.File;
import java.io.IOException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.*;

//Convenience class that gathers all relevant information about a timeline for easy JSON export/import
public class JSONTimeline {
    private final Timeline timeline;            //Timelines hold their events in their own list, no need to duplicate here
    private final Map<String, Integer> ratings;
    private final String timelineImage;
    private final List<String> eventImages;

    public JSONTimeline(Timeline timeline) {    //gathers information about passed timeline for export
        this.timeline = timeline;
        this.timelineImage = toBase64(timeline.getImagePath());
        eventImages = makeEventImages();
        ratings = makeRatings();
    }

    //////////////////////////EXPORT METHODS//////////////////////////

    private List<String> makeEventImages() {
        List<String> out = new ArrayList<>(timeline.getEventList().size());

        String imageContent;
        for (int i = 0; i < timeline.getEventList().size(); i++) {
            imageContent = timeline.getEventList().get(i).getImagePath();       //for each event, get their image
            out.add(toBase64(imageContent));                            //convert to base64, and add that to the list
        }
        return out;
    }

    private String toBase64(String filePath) {          //read a file from its path and convert it to a Base 64 string
        if (filePath == null)
            return null;

        try {
            File imageFile = new File(filePath);
            byte[] imageFileContent = FileUtils.readFileToByteArray(imageFile);
            return Base64.getEncoder().encodeToString(imageFileContent);
        } catch (IOException e) {
            System.err.println("Could not find image at:" + filePath);
            return null;
        }
    }

    private Map<String, Integer> makeRatings() {                        //grab all ratings for this timeline from DB and store in List
        Map<String, Integer> out = new TreeMap<>();
        try (PreparedStatement stmt = DBM.conn.prepareStatement("SELECT u.UserEmail, r.Rating FROM ratings r " +
                "INNER JOIN users u ON r.UserID = u.UserID " +      //can't just use calcRatings() in Timeline because that stores by ID
                "WHERE r.TimelineID = ?")) {
            stmt.setInt(1, timeline.getID());
            ResultSet rs = stmt.executeQuery();
            while (rs.next())
                out.put(rs.getString("UserEmail"), rs.getInt("Rating"));
        } catch (SQLException e) {
            System.err.println("Could not read ratings from database.");
        }
        return out;
    }

    //////////////////////////IMPORT METHODS//////////////////////////

    //After importing a JSON file, use this to insert its contents into the DB
    public void importToDB() {
        int ownerID = matchOwnerInDB();         //check if owner is already in DB
        if (ownerID > 0)                        //if he is, pass local owner's ID to other objects
            setOwnership(ownerID);
        else {                                  //otherwise add them to DB and pass newly generated ID to other objects
            importOwner();
            setOwnership(timeline.getOwner().getID());
        }

        importTimeline();
        importEventList();
        importRatings();
    }

    private void importTimeline() {
        String filePath = ImageUtils.importImageFromBase64(timelineImage, timeline.getImagePath());           //save image and give its new filepath to the timeline
        timeline.setImage(filePath);
        try {
            DBM.insertIntoDB(timeline);                         //no dupe checking, if they're at this point the user may want a dupe timeline
        } catch (SQLException e) {
            System.err.println("Could not access timeline database");
        }
    }

    private void importEventList() {
        Event eventToImport;
        int eventID;
        for (int i = 0; i < timeline.getEventList().size(); i++) {
            eventToImport = timeline.getEventList().get(i);
            eventID = matchEventInDB(eventToImport);
            if (eventID > 0)                                        //if identical event is in DB, pass its ID to this event and call them equal
                eventToImport.setID(eventID);
            else {                                                  //otherwise add event to DB and pass newly generated ID to this event
                String filePath = ImageUtils.importImageFromBase64(eventImages.get(i), timeline.getEventList().get(i).getImagePath());  //save image located in same index of imagesList, and give its new filepath to the timeline
                eventToImport.setImage(filePath);

                try {
                    DBM.insertIntoDB(eventToImport);
                } catch (SQLException e) {
                    System.err.println("Could not access users database");
                }
            }

            try {                                                   //add event to the new timeline on junction table
                eventToImport.addToTimeline(timeline.getID());
            } catch (SQLException e) {
                System.err.println("Could not access timelineevents database.");
            }
        }
    }

    private int matchEventInDB(Event eventToImport) {                              //checks if identical event is in DB, returns event's ID if it is
        try (PreparedStatement stmt = DBM.conn.prepareStatement("SELECT e.EventID FROM events e " +
                "INNER JOIN users u ON e.EventOwner = u.UserID " +
                "WHERE u.UserEmail = ? AND e.EventName = ? AND e.EventDescription = ? AND e.StartYear = ? AND " +
                "StartMonth = ? AND `StartDay` = ? AND `StartHour` = ? AND `StartMinute` = ? AND `StartSecond` = ? AND " +
                "`StartMillisecond` = ? AND `EndYear` = ? AND `EndMonth` = ? AND `EndDay` = ? AND `EndHour` = ? AND " +
                "`EndMinute` = ? AND `EndSecond` = ? AND `EndMillisecond` = ? ")) {
            stmt.setString(1, timeline.getOwner().getUserEmail());      //timeline and its events have same owner
            stmt.setString(2, eventToImport.getName());
            stmt.setString(3, eventToImport.getDescription());
            stmt.setInt(4, eventToImport.getStartDate().getYear());
            stmt.setInt(5, eventToImport.getStartDate().getMonthValue());
            stmt.setInt(6, eventToImport.getStartDate().getDayOfMonth());
            stmt.setInt(7, eventToImport.getStartDate().getHour());
            stmt.setInt(8, eventToImport.getStartDate().getMinute());
            stmt.setInt(9, eventToImport.getStartDate().getSecond());
            stmt.setInt(10, eventToImport.getStartDate().getNano() / 1000000);
            stmt.setInt(11, eventToImport.getEndDate().getYear());
            stmt.setInt(12, eventToImport.getEndDate().getMonthValue());
            stmt.setInt(13, eventToImport.getEndDate().getDayOfMonth());
            stmt.setInt(14, eventToImport.getEndDate().getHour());
            stmt.setInt(15, eventToImport.getEndDate().getMinute());
            stmt.setInt(16, eventToImport.getEndDate().getSecond());
            stmt.setInt(17, eventToImport.getEndDate().getNano() / 1000000);

            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getInt(1);
            }
            rs.close();
        } catch (SQLException e) {
            System.err.println("Could not access events database");
        }
        return 0;
    }

    private int matchOwnerInDB() {                              //checks if owner is in DB, returns owner's ID if they are
        try (PreparedStatement stmt = DBM.conn.prepareStatement("SELECT UserID FROM users WHERE `UserEmail` = ?")) {
            stmt.setString(1, timeline.getOwner().getUserEmail());
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getInt(1);
            }
            rs.close();
        } catch (SQLException e) {
            System.err.println("Could not access users database");
        }
        return 0;
    }

    private void importOwner() {
        try {
            DBM.insertIntoDB(timeline.getOwner());
        } catch (SQLException e) {
            System.err.println("Could not access users database");
        }
    }

    private void setOwnership(int ownerID) {                //sets ownerID of all objects to the passed value
        timeline.getOwner().setID(ownerID);
        timeline.setOwnerID(ownerID);
        for (int i = 0; i < timeline.getEventList().size(); i++) {
            timeline.getEventList().get(i).setOwnerID(ownerID);
        }
    }

    public void importRatings() {
        try (PreparedStatement readStmt = DBM.conn.prepareStatement("SELECT UserID, UserEmail FROM users");
             PreparedStatement writeStmt = DBM.conn.prepareStatement("INSERT INTO ratings (`Rating`, `UserId`, `TimeLineID`) VALUES (?, ?, ?)")) {
            writeStmt.setInt(3, timeline.getID());
            DBM.conn.setAutoCommit(false);

            ResultSet rs = readStmt.executeQuery();

            Map<String, Integer> emailList = new TreeMap<>();
            while (rs.next())                       //adds each user's email address alongside user's ID
                emailList.put(rs.getString("UserEmail"), rs.getInt("UserID"));

            ratings.keySet().retainAll(emailList.keySet());     //if user with that email is not found in local DB, don't add their rating

            for (String rater : ratings.keySet()) {
                writeStmt.setInt(1, ratings.get(rater));
                writeStmt.setInt(2, emailList.get(rater));
                writeStmt.addBatch();
            }

            rs.close();

            writeStmt.executeBatch();
        } catch (SQLException e) {                  //if we can't access the database to match users to local IDs, no point trying to add ratings
            e.printStackTrace();//System.err.println("Could not access ratings database");
        } finally {
            try {
                DBM.conn.setAutoCommit(true);
            } catch (SQLException e) {
                System.err.println("Could not access database.");
            }
        }
    }


    /////////////////JSON Serializers/Deserializers/////////////////
    public static Gson getGson() {
        return new GsonBuilder()
                .setPrettyPrinting()
                .registerTypeAdapter(LocalDateTime.class, getDateSerializer().nullSafe())
                .setFieldNamingPolicy(FieldNamingPolicy.UPPER_CAMEL_CASE_WITH_SPACES)
                .create();
    }

    public static TypeAdapter<LocalDateTime> getDateSerializer() {
        return new TypeAdapter<>() {
            @Override
            public void write(JsonWriter jsonWriter, LocalDateTime localDateTime) throws IOException {
                jsonWriter.value(localDateTime.toString());
            }

            @Override
            public LocalDateTime read(JsonReader jsonReader) throws IOException {
                if (jsonReader.peek() == JsonToken.NULL) {
                    jsonReader.nextNull();
                    return null;
                }

                return LocalDateTime.parse(jsonReader.nextString());
            }
        };
    }
}
