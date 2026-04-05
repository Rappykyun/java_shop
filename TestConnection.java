import java.sql.Connection;

public class TestConnection {
    public static void main(String[] args) {
        try (Connection connection = DBConnection.getConnection()) {
            System.out.println("Connected to MySQL successfully.");
        } catch (Exception exception) {
            System.out.println("Database connection failed.");
            exception.printStackTrace();
        }
    }
}
