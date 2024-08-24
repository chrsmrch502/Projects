package database;

import com.google.gson.Gson;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.charset.Charset;
import java.sql.*;
import java.util.*;

//Database manager class for easier connecting and interacting
public class DBM {
    private static final String JDBC_DRIVER = "com.mysql.cj.jdbc.Driver";
    private static final String creationScript = "src/main/resources/Database_Creation_Script.sql";
    public static Connection conn = null;
    private static String DB_URL = "jdbc:mysql://localhost?useTimezone=true&serverTimezone=UTC";
    private static String USER = "root";
    private static String PASS = "yourpasshere";
    private static String SCHEMA = "project";

    public DBM() {                                                         //Connect to server with default settings
        this(SCHEMA);
    }

    public DBM(String SCHEMA) {                                            //Connect to server with alternate schema name
        this(DB_URL, USER, PASS, SCHEMA);
    }

    public DBM(String USER, String PASS) {                                 //Connect to server with alternate login
        this(DB_URL, USER, PASS);
    }

    public DBM(String DB_URL, String USER, String PASS) {                  //Connect to alternate server
        this(DB_URL, USER, PASS, SCHEMA);
    }

    public DBM(String DB_URL, String USER, String PASS, String SCHEMA) {   //Connect to server with alternate settings
        close();                        //if connection is already open, close it before making a new one

        DBM.DB_URL = DB_URL;
        DBM.USER = USER;
        DBM.PASS = PASS;
        DBM.SCHEMA = SCHEMA;
        try {
            //Register JDBC driver
            Class.forName(JDBC_DRIVER);

            //Open a connection
            System.out.println("Connecting to selected database...");
            conn = DriverManager.getConnection(DB_URL, USER, PASS);

            //Connect to schema
            useSchema(SCHEMA);
        } catch (ClassNotFoundException e) {
            System.err.println("Could not access JDBC drivers");
        } catch (SQLException e) {
            System.err.println("Could not connect to database.");
        }
        System.out.println("Connected to database successfully.");
    }

    public static void setupSchema() {   //creates schema from default script
        try {
            System.out.println("Creating schema...");
            dropSchema();
            useSchema(SCHEMA);

            //Read and run the database creation script
            System.out.println("Creating table(s) in given database...");
            runScript(creationScript);
            System.out.println("Schema created successfully.");
        } catch (SQLException | FileNotFoundException e) {
            e.printStackTrace();
            System.err.println("Could not run Database_Creation_Script");
        }
    }

    public static void useSchema(String SCHEMA) throws SQLException {
        System.out.println("Attempting to connect to schema...");
        try (Statement stmt = conn.createStatement()) {
            if (!stmt.executeQuery("SHOW DATABASES LIKE '" + SCHEMA + "';").next()) //swaps to a different schema, creating it if it doesn't exist
                stmt.execute("CREATE SCHEMA `" + SCHEMA + "`");                     //note: you may want to rerun setupSchema() if on a brand new schema
            stmt.execute("USE " + SCHEMA);
            DBM.SCHEMA = SCHEMA;
        }
    }

    public static void createTestData() throws FileNotFoundException, SQLException {    //for testing
        runScript("src/main/resources/Test_Data_Setup.sql");
    }

    private static void runScript(String script) throws FileNotFoundException, SQLException {      //private read-in method for DB creation script
        File sql = new File(script);
        Statement stmt = conn.createStatement();
        Scanner sqlScan = new Scanner(sql);
        sqlScan.useDelimiter(";[\\r\\n]{3,}");
        String query;

        while (sqlScan.hasNext()) {
            query = sqlScan.next();
            if (stmt != null && !query.isEmpty())
                stmt.execute(query);                //technically SQL-injectable but by the time someone can edit the script they can do anything
        }

        sqlScan.close();
        if (stmt != null)
            stmt.close();
    }

    //Runs PreparedStatement and uses Functional Interface method to parse each row returned into an object
    //How To Define The "creatable" Input:
    //functional interfaces can use the implementation defined by an object,    e.g. new User() returns a List<User>
    //or they can accept a lambda/method directly, which must take in a ResultSet and output an Object,     e.g. rs -> rs.getString("Name") will return a List<String> from the ResultSet's Name field
    public static <T> List<T> getFromDB(PreparedStatement query, CreatableFromDB<T> creatable) throws SQLException {
        List<T> out = new ArrayList<>();

        //Runs input query to get ResultSet, then adds created objects to list
        try (ResultSet rs = query.executeQuery()) {
            while (rs.next()) {
                out.add(creatable.createFromDB(rs));
            }
            return out;
        }
    }

    public static <T> void insertIntoDB(List<T> insert) throws SQLException {           //convenience method so inserting works with Lists
        insertIntoDB(asArray(insert));
    }

    @SafeVarargs
    public static <T> void insertIntoDB(DBObject<T>... insert) throws SQLException {    //DON'T INSERT OBJECTS OF DIFFERENT TYPES
        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            conn.setAutoCommit(false);                              //turn off autocommit so statements don't run as soon as they're added to the batch
            for (DBObject<T> t : insert) {                          //iterate through all inserted T
                if (t == null)
                    continue;
                if (stmt == null)                                   //get T-specific prepared statement from the first nonnull T
                    stmt = t.getInsertQuery();

                if (t.getID() > 0)                                  //if something has an ID it already exists in DB
                    throw new SQLException("Cannot update " + t.getClass().getSimpleName() + " not in database.");

                t.addToBatch(stmt);                             //get the values of each T and add them to the batch
            }
            if (stmt == null)
                return;
            stmt.executeBatch();                                    //run the batch
            conn.commit();

            rs = stmt.getGeneratedKeys();
            for (DBObject<T> t : insert) {                          //after insertion, get the autogenerated ID and pass it to objects that were inserted
                if (t == null)
                    continue;
                rs.next();
                t.setID(rs.getInt(1));
            }
        } finally {
            if (stmt != null)
                stmt.close();
            if (rs != null)
                rs.close();
            conn.setAutoCommit(true);                               //turn autocommit back on so people can use the DB normally
        }
    }

    public static <T> void updateInDB(List<T> update) throws SQLException {             //convenience method so updating works with Lists
        updateInDB(asArray(update));
    }

    @SafeVarargs
    public static <T> void updateInDB(DBObject<T>... update) throws SQLException {      //DON'T INSERT OBJECTS OF DIFFERENT TYPES
        PreparedStatement stmt = null;
        try {
            conn.setAutoCommit(false);                              //turn off autocommit so statements don't run as soon as they're added to the batch
            for (DBObject<T> t : update) {                          //iterate through all inserted T
                if (t == null)
                    continue;
                if (stmt == null)                                   //get T-specific prepared statement from the first nonnull T
                    stmt = t.getUpdateQuery();

                if (t.getID() == 0)                                 //if something has no ID it doesn't exist in DB
                    throw new SQLException(t.getClass().getSimpleName() + " is already in database");

                t.addToBatch(stmt);                             //get the values of each T and add them to the batch
            }
            if (stmt != null)
                stmt.executeBatch();                                //run the batch
        } finally {
            if (stmt != null)
                stmt.close();
            conn.setAutoCommit(true);                               //turn autocommit back on so people can use the DB normally
        }
    }

    public static <T> void deleteFromDB(List<T> delete) throws SQLException {           //convenience method so deleting works with Lists
        deleteFromDB(asArray(delete));
    }

    @SafeVarargs
    public static <T> void deleteFromDB(DBObject<T>... delete) throws SQLException {           //DON'T INSERT OBJECTS OF DIFFERENT TYPES
        PreparedStatement stmt = null;
        try {
            conn.setAutoCommit(false);                              //turn off autocommit so statements don't run as soon as they're added to the batch
            for (DBObject<T> t : delete) {                          //iterate through all inserted T
                if (t == null)
                    continue;
                if (stmt == null)                                   //get T-specific prepared statement from the first nonnull T
                    stmt = t.getDeleteQuery();

                stmt.setInt(1, t.getID());              //get the ID of each T and
                stmt.addBatch();                                    //add it to the batch
                t.deleteImage();                                    //delete images along with database info
            }

            if (stmt != null)
                stmt.executeBatch();                                //run the batch
        } finally {
            if (stmt != null)
                stmt.close();
            conn.setAutoCommit(true);                               //turn autocommit back on so people can use the DB normally
        }
    }

    @SuppressWarnings("unchecked")
    public static <T> DBObject<T>[] asArray(List<T> list) {         //converts List to Array manually since java doesn't like generic arrays
        try {                                                       //don't mix types, and if in doubt just convert to a typed array yourself
            DBObject<T>[] asArray = (DBObject<T>[]) java.lang.reflect.Array.newInstance(list.get(0).getClass(), list.size());
            for (int i = 0; i < list.size(); i++) {                 //don't try to run empty Lists either, if this method gets abused I'm removing it
                asArray[i] = (DBObject<T>) list.get(i);
            }
            return asArray;
        } catch (ClassCastException e) {
            throw new ClassCastException("Class does not implement DBObject<T>");       //clearer exception message
        }
    }

    public static void dropSchema() {                                      //drop current schema
        try (PreparedStatement stmt = conn.prepareStatement("DROP DATABASE IF EXISTS " + SCHEMA)) {
            stmt.execute();
        } catch (SQLException e) {
            System.err.println("Could not drop database " + SCHEMA);
        }
    }

    public static void firstTimeSetup() {    //check if tables exist in DB, if not then create them and import dummy data
        try {
            DatabaseMetaData schemaCheck = conn.getMetaData();

            try (ResultSet tableList = schemaCheck.getTables(SCHEMA, null, "timelines", null)) {
                if (tableList.next() && (tableList.getString("TABLE_NAME").equals("timelines")))
                    return;
            }
        } catch (SQLException e) {
            System.err.println("Could not determine whether database tables are set up.");
        }

        System.out.println("Beginning first time setup...");
        setupSchema();

        Alert notifyOfAdmin = new Alert(Alert.AlertType.INFORMATION);        //warn about admin login
        notifyOfAdmin.setTitle("Welcome to LyfeLine!");
        notifyOfAdmin.setHeaderText("The default admin login is Admin@gmail.com using password 'Passw0rd!'");
        notifyOfAdmin.setContentText("This message will not show after first time setup.");
        notifyOfAdmin.showAndWait();
        System.out.println("\nWelcome to LyfeLine! The default admin login is Admin@gmail.com using password 'Passw0rd!' This message will not show after first time setup.");

        Alert confirmLoad = new Alert(Alert.AlertType.CONFIRMATION);        //ask if user wants dummy data
        confirmLoad.setTitle("Demonstration Data");
        confirmLoad.setHeaderText("The database is empty.");
        confirmLoad.setContentText("Would you like to load some demonstration data?");
        Optional<ButtonType> result = confirmLoad.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.CANCEL)
            return;

        Gson gson = JSONTimeline.getGson();
        String inJSON;
        File directory = new File("src/main/resources/dummy_data/");
        if (directory.listFiles() == null)
            return;
        for (File f : Objects.requireNonNull(directory.listFiles())) {
            try {
                inJSON = FileUtils.readFileToString(f, (Charset) null);             //import Json from file
                gson.fromJson(inJSON, JSONTimeline.class).importToDB();             //parse Json with GSON object and import it to the DB
            } catch (IOException ignore) {                                          //if one fails to read, skip it
            }
        }
    }

    public static void close() {                                           //close the connection when you're done please
        try {
            if (conn != null) {
                conn.close();
                conn = null;
            }
        } catch (SQLException e) {
            System.err.println("Could not close database.");
        }
    }
}
