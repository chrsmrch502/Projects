package database;

import java.sql.ResultSet;
import java.sql.SQLException;

@FunctionalInterface
public interface CreatableFromDB<T> {
    T createFromDB(ResultSet rs) throws SQLException;
}
