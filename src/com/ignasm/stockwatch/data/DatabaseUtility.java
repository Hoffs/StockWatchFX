package com.ignasm.stockwatch.data;

import org.sqlite.SQLiteDataSource;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Ignas Maslinskas
 * 20153209
 * PRIf-15/1
 */
class DatabaseUtility {
    private static SQLiteDataSource dataSource;
    private static Connection connection;

    public static Connection getSqlConnection() {
        if (connection == null) {
            SQLiteDataSource ds = new SQLiteDataSource();
            ds.setUrl("jdbc:sqlite:data.db");
            dataSource = ds;
            try {
                connection = dataSource.getConnection();
                DatabaseUtility.prepareDatabase(connection);
            } catch (SQLException e) {
                e.printStackTrace();
            }
            return getSqlConnection();
        } else {
            return connection;
        }
    }

    private static void prepareDatabase(Connection connection) {
        try {
            connection.setAutoCommit(false);

            PreparedStatement preparedStatement;

            preparedStatement = connection.prepareStatement("CREATE TABLE IF NOT EXISTS stock (id INTEGER PRIMARY KEY AUTOINCREMENT, company_name TEXT, symbol TEXT)");
            preparedStatement.executeUpdate();

            preparedStatement = connection.prepareStatement("CREATE TABLE IF NOT EXISTS stock_price (id INTEGER PRIMARY KEY AUTOINCREMENT, stock REFERENCES stock(id), price REAL, date TEXT)");
            preparedStatement.executeUpdate();

            preparedStatement = connection.prepareStatement("CREATE TABLE IF NOT EXISTS stock_activity (id INTEGER PRIMARY KEY AUTOINCREMENT, stock REFERENCES stock(id), share_change REAL, net_change REAL, price REAL, date TEXT)");
            preparedStatement.executeUpdate();

            /*
            preparedStatement = connection.prepareStatement("CREATE TABLE IF NOT EXISTS activity_entries (id INTEGER PRIMARY KEY AUTOINCREMENT, student REFERENCES students(id), description TEXT, points INTEGER, date TEXT)");
            preparedStatement.executeUpdate();
            */

            connection.commit();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static int executeUpdateStatement(String query) throws SQLException {
        Connection connection = getSqlConnection();
        connection.setAutoCommit(false);
        PreparedStatement preparedStatement = connection.prepareStatement(query);
        int result = preparedStatement.executeUpdate();
        connection.commit();
        return result;
    }

    public static ResultSet executeQueryStatement(String query) throws SQLException {
        Connection connection = getSqlConnection();
        connection.setAutoCommit(false);
        PreparedStatement preparedStatement = connection.prepareStatement(query);
        ResultSet resultSet = preparedStatement.executeQuery();
        connection.commit();
        return resultSet;
    }

}
