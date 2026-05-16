import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;

public class ResetLog {
    public static void main(String[] args) {
        String url = "jdbc:postgresql://localhost:5432/todo_db";
        String user = "admin";
        String pass = "admin123";
        try (Connection conn = DriverManager.getConnection(url, user, pass);
             Statement stmt = conn.createStatement()) {
            stmt.execute("DELETE FROM databasechangelog");
            System.out.println("CHANGELOG CLEARED");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
