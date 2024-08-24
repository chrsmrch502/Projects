package database;


import java.sql.PreparedStatement;
import java.sql.SQLException;

public interface DBObject<T> extends CreatableFromDB<T> {
    PreparedStatement getInsertQuery() throws SQLException;

    PreparedStatement getUpdateQuery() throws SQLException;

    void addToBatch(PreparedStatement stmt) throws SQLException;

    PreparedStatement getDeleteQuery() throws SQLException;

    void deleteImage();

    void setID(int id);

    int getID();
}
