package Server.Repository;
import Server.Action;

import java.sql.*;

public class JdbcDao {

    private static final String DATABASE_URL = "jdbc:mysql://localhost:3306/socket_chat?useSSL=false";
    private static final String DATABASE_USERNAME = "root";
    private static final String DATABASE_PASSWORD = "root";
    private static final String INSERT_NEW_USER_QUERY = "INSERT INTO users (nick, user_password) VALUES (?, ?)";
    private static final String IS_USER_EXISTS_QUERY = "SELECT COUNT(*) FROM users WHERE nick = ?";
    private static final String CHECK_USER_AUTH_QUERY = "SELECT COUNT(*) FROM users WHERE nick = ? AND user_password = ?";
    private static final String UPDATE_USER_QUERY = "UPDATE users SET last_connection = ?, port = ? WHERE nick = ?";
    private static final String UPDATE_USER_DISCONNECT_QUERY = "UPDATE users SET last_disconnection = ? WHERE nick = ?";
    private static final String INSERT_NEW_LOG_QUERY = "INSERT INTO logs (type_id, message) VALUES (?, ?)";
    private static final String FETCH_LOGS_QUERY = "SELECT lt.type, lg.message, lg.log_time FROM logs lg INNER JOIN log_types lt on lg.type_id = lt.id";

    //Используем паттерн Singleton
    public static JdbcDao Instance;

    static {
        Instance = new JdbcDao();
    }

    private JdbcDao() {}

    public boolean isUserExists(String nick) {
        try (Connection connection = DriverManager.getConnection(DATABASE_URL, DATABASE_USERNAME, DATABASE_PASSWORD);
             PreparedStatement preparedStatement = connection.prepareStatement(IS_USER_EXISTS_QUERY)) {
            preparedStatement.setString(1, nick);
            var query = preparedStatement.executeQuery();
            int count = 0;
            if(query.next()){
                count = query.getInt(1);
            }
            return count > 0;
        } catch (SQLException e) {
            printSQLException(e);
            return false;
        }
    }

    public boolean checkUserAuth(String nick, String password) {
        try (Connection connection = DriverManager.getConnection(DATABASE_URL, DATABASE_USERNAME, DATABASE_PASSWORD);
             PreparedStatement preparedStatement = connection.prepareStatement(CHECK_USER_AUTH_QUERY)) {
            preparedStatement.setString(1, nick);
            preparedStatement.setString(2, password);
            var query = preparedStatement.executeQuery();
            int count = 0;
            if(query.next()){
                count = query.getInt(1);
            }
            return count > 0;
        } catch (SQLException e) {
            printSQLException(e);
            return false;
        }
    }

    public void updateUserData(Timestamp date, int port, String nick) {
        try (Connection connection = DriverManager.getConnection(DATABASE_URL, DATABASE_USERNAME, DATABASE_PASSWORD);
             PreparedStatement preparedStatement = connection.prepareStatement(UPDATE_USER_QUERY)) {
            preparedStatement.setTimestamp(1, date);
            preparedStatement.setInt(2, port);
            preparedStatement.setString(3, nick);
            preparedStatement.executeUpdate();
        } catch (SQLException e) {
            printSQLException(e);
        }
    }

    public void updateUserDisconnection(Timestamp date, String nick) {
        try (Connection connection = DriverManager.getConnection(DATABASE_URL, DATABASE_USERNAME, DATABASE_PASSWORD);
             PreparedStatement preparedStatement = connection.prepareStatement(UPDATE_USER_DISCONNECT_QUERY)) {
            preparedStatement.setTimestamp(1, date);
            preparedStatement.setString(2, nick);
            preparedStatement.executeUpdate();
        } catch (SQLException e) {
            printSQLException(e);
        }
    }

    public void insertNewUser(String nick, String password) {
        try (Connection connection = DriverManager.getConnection(DATABASE_URL, DATABASE_USERNAME, DATABASE_PASSWORD);
             PreparedStatement preparedStatement = connection.prepareStatement(INSERT_NEW_USER_QUERY)) {
            preparedStatement.setString(1, nick);
            preparedStatement.setString(2, password);
            preparedStatement.executeUpdate();
        } catch (SQLException e) {
            printSQLException(e);
        }
    }

    public void insertNewLog(Action messageType, String message) {
        try (Connection connection = DriverManager.getConnection(DATABASE_URL, DATABASE_USERNAME, DATABASE_PASSWORD);
             PreparedStatement preparedStatement = connection.prepareStatement(INSERT_NEW_LOG_QUERY)) {
            preparedStatement.setInt(1, messageType.getValue());
            preparedStatement.setString(2, message);
            preparedStatement.executeUpdate();
        } catch (SQLException e) {
            printSQLException(e);
        }
    }

    public String fetchLogs() {
        try (Connection connection = DriverManager.getConnection(DATABASE_URL, DATABASE_USERNAME, DATABASE_PASSWORD);
             PreparedStatement preparedStatement = connection.prepareStatement(FETCH_LOGS_QUERY)) {
            var query = preparedStatement.executeQuery();
            var builder = new StringBuilder();
            while (query.next()){
                builder.append(query.getString(1)).append(" [").append(query.getString(3)).append("] ").append(query.getString(2)).append("\n");
            }

            return builder.toString();
        } catch (SQLException e) {
            printSQLException(e);
            return "";
        }
    }

    public static void printSQLException(SQLException ex) {
        for (Throwable e: ex) {
            if (e instanceof SQLException) {
                e.printStackTrace(System.err);
                System.err.println("SQLState: " + ((SQLException) e).getSQLState());
                System.err.println("Error Code: " + ((SQLException) e).getErrorCode());
                System.err.println("Message: " + e.getMessage());
                Throwable t = ex.getCause();
                while (t != null) {
                    System.out.println("Cause: " + t);
                    t = t.getCause();
                }
            }
        }
    }
}