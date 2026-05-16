import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;

public class DbStateCheck {
    public static void main(String[] args) {
        String url = "jdbc:postgresql://localhost:5432/todo_db";
        String user = "admin";
        String pass = "admin123";
        try (Connection conn = DriverManager.getConnection(url, user, pass);
             Statement stmt = conn.createStatement()) {
            
            System.out.println("--- CHECKING USERS TABLE ---");
            try (ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM information_schema.tables WHERE table_schema='public' AND table_name='users'")) {
                if (rs.next()) System.out.println("Table exists count: " + rs.getInt(1));
            }
            try (ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM users")) {
                if (rs.next()) System.out.println("Users count: " + rs.getInt(1));
            } catch (Exception e) {
                System.out.println("Users table might not exist: " + e.getMessage());
            }

            System.out.println("--- CHECKING DATABASECHANGELOG ---");
            try (ResultSet rs = stmt.executeQuery("SELECT id, dateexecuted, md5sum FROM databasechangelog")) {
                while (rs.next()) {
                    System.out.println("ID: " + rs.getString("id") + " | DATE: " + rs.getString("dateexecuted") + " | MD5: " + rs.getString("md5sum"));
                }
            } catch (Exception e) {
                System.out.println("Changelog table might not exist: " + e.getMessage());
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
