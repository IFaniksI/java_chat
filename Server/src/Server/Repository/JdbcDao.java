package Server.Repository;
import Connection.*;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class JdbcDao {

    private static final String DATABASE_URL = "jdbc:mysql://localhost:3306/socket_chat?useSSL=false";
    private static final String DATABASE_USERNAME = "root";
    private static final String DATABASE_PASSWORD = "root";
    private static final String INSERT_NEW_USER_QUERY = "INSERT INTO users (nick, user_password) VALUES (?, ?)";
    private static final String INSERT_NEW_LOG_QUERY = "INSERT INTO logs (type_id, message) VALUES (?, ?)";
    private static final String FETCH_LOGS_QUERY = "SELECT * FROM logs INNER JOIN log_types lt on logs.type_id = lt.id";

    //Используем паттерн Singleton
    public static JdbcDao Instance;

    static {
        Instance = new JdbcDao();
    }

    private JdbcDao() {}

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

    public void insertNewLog(MessageType messageType, String message) {
        try (Connection connection = DriverManager.getConnection(DATABASE_URL, DATABASE_USERNAME, DATABASE_PASSWORD);
             PreparedStatement preparedStatement = connection.prepareStatement(INSERT_NEW_LOG_QUERY)) {
            preparedStatement.setInt(1, messageType.getValue());
            preparedStatement.setString(2, message);
            preparedStatement.executeUpdate();
        } catch (SQLException e) {
            printSQLException(e);
        }
    }

    public void fetchLogs(MessageType messageType, String message) {
        try (Connection connection = DriverManager.getConnection(DATABASE_URL, DATABASE_USERNAME, DATABASE_PASSWORD);
             PreparedStatement preparedStatement = connection.prepareStatement(FETCH_LOGS_QUERY)) {
            preparedStatement.setInt(1, messageType.getValue());
            preparedStatement.setString(2, message);
            preparedStatement.executeUpdate();
        } catch (SQLException e) {
            printSQLException(e);
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