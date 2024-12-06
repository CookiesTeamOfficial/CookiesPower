package arkadarktime.interfaces;

import java.sql.Connection;
import java.sql.ResultSet;

public interface DatabaseManager {
    Connection connect();

    void disconnect();

    ResultSet get(String columns, String whereClause, Object... params);

    boolean insert(String[] columns, Object... values);

    boolean update(String[] columns, String whereClause, Object... values);

    boolean delete(String whereClause, Object... params);

    void createTable(String tableStructure);
}
