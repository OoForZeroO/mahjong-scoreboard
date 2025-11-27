import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;

public class AddTotalDurationField {
    public static void main(String[] args) {
        String url = "jdbc:postgresql://localhost:5432/mahjong_score_system";
        String user = "postgres";
        String password = "123456";
        
        try (Connection conn = DriverManager.getConnection(url, user, password);
             Statement stmt = conn.createStatement()) {
            
            System.out.println("Connected to PostgreSQL database!");
            
            // 添加 total_duration 字段
            String sql1 = "ALTER TABLE match_results ADD COLUMN total_duration BIGINT";
            stmt.executeUpdate(sql1);
            System.out.println("✓ Added total_duration column to match_results table");
            
            // 为现有记录设置默认值
            String sql2 = "UPDATE match_results SET total_duration = 0 WHERE total_duration IS NULL";
            int updatedRows = stmt.executeUpdate(sql2);
            System.out.println("✓ Updated " + updatedRows + " existing records with default total_duration value");
            
            System.out.println("Database update completed successfully!");
            
        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
