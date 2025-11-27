import java.sql.Connection;
import java.sql.DriverManager;

public class PostgreSQLTest {
    public static void main(String[] args) {
        try {
            // 加载驱动
            Class.forName("org.postgresql.Driver");
            System.out.println("Driver loaded successfully");
            
            // 连接信息 - 使用正确的密码
            String url = "jdbc:postgresql://localhost:5432/postgres";
            String user = "postgres";
            String password = "cch815566";
            
            System.out.println("Connecting to: " + url);
            System.out.println("Username: " + user);
            System.out.println("Password: ********");
            
            // 尝试连接
            Connection conn = DriverManager.getConnection(url, user, password);
            System.out.println("Connection successful!");
            conn.close();
            
        } catch (Exception e) {
            System.out.println("Error message: " + e.getMessage());
            System.out.println("Try to connect with pgAdmin first to check the correct password");
        }
    }
}