package arkadarktime.utils.database;

import arkadarktime.CookiesPower;
import arkadarktime.interfaces.BukkitConsole;
import arkadarktime.interfaces.DatabaseManager;

import java.io.File;
import java.sql.*;
import java.util.StringJoiner;

public abstract class AbstractDatabaseManager implements DatabaseManager, BukkitConsole {
    protected final CookiesPower plugin;
    private final String database;
    private final String tableName;
    protected Connection connection;

    public AbstractDatabaseManager(CookiesPower plugin, String database, String tableName) {
        this.plugin = plugin;
        this.database = database;
        this.tableName = tableName;
    }

    @Override
    public Connection connect() {
        try {
            if (this.connection != null && !this.connection.isClosed()) {
                return this.connection;
            }

            Class.forName("org.sqlite.JDBC");
            File databaseFile = new File(plugin.getDataFolder(), database + ".db");
            this.connection = DriverManager.getConnection("jdbc:sqlite:" + databaseFile.getAbsolutePath());
        } catch ( ClassNotFoundException | SQLException e ) {
            handleException("Failed to connect to database '" + database + "'", e);
            return null;
        }
        return this.connection;
    }

    @Override
    public void disconnect() {
        try {
            if (this.connection != null && !this.connection.isClosed()) {
                this.connection.close();
            }
        } catch ( SQLException e ) {
            handleException("Failed to disconnect from database '" + database + "'", e);
        }
    }

    @Override
    public void createTable(String tableStructure) {
        String query = "CREATE TABLE IF NOT EXISTS " + tableName + " (" + tableStructure + ")";
        executeUpdate(query);
    }

    @Override
    public ResultSet get(String columns, String whereClause, Object... params) {
        String query = buildSelectQuery(columns, whereClause);
        try {
            Connection connection = this.connect();
            PreparedStatement preparedStatement = connection.prepareStatement(query);
            setParameters(preparedStatement, params);
            return preparedStatement.executeQuery();
        } catch ( SQLException e ) {
            handleException("Failed to retrieve data", e);
        }
        return null;
    }

    @Override
    public boolean insert(String[] columns, Object... values) {
        String query = buildInsertQuery(columns);
        try (Connection connection = connect(); PreparedStatement preparedStatement = connection.prepareStatement(query)) {
            setParameters(preparedStatement, values);
            return preparedStatement.executeUpdate() > 0;
        } catch ( SQLException e ) {
            handleException("Failed to insert data", e);
        }
        return false;
    }

    @Override
    public boolean update(String[] columns, String whereClause, Object... values) {
        String query = buildUpdateQuery(columns, whereClause);
        try (Connection connection = connect(); PreparedStatement preparedStatement = connection.prepareStatement(query)) {
            setParameters(preparedStatement, values);
            return preparedStatement.executeUpdate() > 0;
        } catch ( SQLException e ) {
            handleException("Failed to update data", e);
        }
        return false;
    }

    @Override
    public boolean delete(String whereClause, Object... params) {
        String query = "DELETE FROM " + tableName + " WHERE " + whereClause;
        try (Connection connection = connect(); PreparedStatement preparedStatement = connection.prepareStatement(query)) {
            setParameters(preparedStatement, params);
            return preparedStatement.executeUpdate() > 0;
        } catch ( SQLException e ) {
            handleException("Failed to delete data", e);
        }
        return false;
    }

    private void executeUpdate(String query) {
        try (Connection connection = connect(); PreparedStatement preparedStatement = connection.prepareStatement(query)) {
            preparedStatement.executeUpdate();
        } catch ( SQLException e ) {
            handleException("Failed to execute update", e);
        }
    }

    private String buildSelectQuery(String columns, String whereClause) {
        String query = "SELECT " + columns + " FROM " + tableName;
        if (whereClause != null && !whereClause.isEmpty()) {
            query += " WHERE " + whereClause;
        }
        return query;
    }

    private String buildInsertQuery(String[] columns) {
        StringJoiner columnJoiner = new StringJoiner(", ");
        StringJoiner valueJoiner = new StringJoiner(", ");
        for (String column : columns) columnJoiner.add(column);
        for (int i = 0; i < columns.length; i++) valueJoiner.add("?");
        return "INSERT INTO " + tableName + " (" + columnJoiner + ") VALUES (" + valueJoiner + ")";
    }

    private String buildUpdateQuery(String[] columns, String whereClause) {
        StringJoiner columnJoiner = new StringJoiner(", ");
        for (String column : columns) columnJoiner.add(column + " = ?");
        return "UPDATE " + tableName + " SET " + columnJoiner + " WHERE " + whereClause;
    }

    private void setParameters(PreparedStatement stmt, Object... params) throws SQLException {
        for (int i = 0; i < params.length; i++) {
            stmt.setObject(i + 1, params[i]);
        }
    }

    protected void handleException(String message, Exception e) {
        Console(ConsoleType.ERROR, message + ": " + e.getMessage(), LineType.SIDE_LINES);
        e.printStackTrace();
    }
}
