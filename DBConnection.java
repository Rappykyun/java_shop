import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public final class DBConnection {
    private static final String URL =
            "jdbc:mysql://localhost:3306/java_shop?allowPublicKeyRetrieval=true&serverTimezone=Asia/Manila";
    private static final String USERNAME = "root";
    private static final String PASSWORD = "";

    private DBConnection() {
    }

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL, USERNAME, PASSWORD);
    }
}
