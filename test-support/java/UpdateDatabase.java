import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;
import java.sql.ResultSet;

public class UpdateDatabase {
    public static void main(String[] args) {
        String url = "jdbc:postgresql://localhost:5432/mahjong_score_system";
        String user = "postgres";
        String password = "123456";
        
        try (Connection conn = DriverManager.getConnection(url, user, password);
             Statement stmt = conn.createStatement()) {
            
            System.out.println("Connected to PostgreSQL database!");
            
            // 检查 total_duration 字段是否存在
            String checkSql = "SELECT column_name FROM information_schema.columns WHERE table_name = 'match_results' AND column_name = 'total_duration'";
            ResultSet rs = stmt.executeQuery(checkSql);
            
            if (!rs.next()) {
                // 字段不存在，添加它
                String addColumnSql = "ALTER TABLE match_results ADD COLUMN total_duration BIGINT";
                stmt.executeUpdate(addColumnSql);
                System.out.println("✓ Added total_duration column to match_results table");
                
                // 为现有记录设置默认值
                String updateSql = "UPDATE match_results SET total_duration = 0 WHERE total_duration IS NULL";
                int updatedRows = stmt.executeUpdate(updateSql);
                System.out.println("✓ Updated " + updatedRows + " existing records with default total_duration value");
            } else {
                System.out.println("✓ total_duration column already exists");
            }
            
            // 验证表结构
            String verifySql = "SELECT column_name, data_type, is_nullable FROM information_schema.columns WHERE table_name = 'match_results' ORDER BY ordinal_position";
            ResultSet verifyRs = stmt.executeQuery(verifySql);
            System.out.println("\nCurrent match_results table structure:");
            while (verifyRs.next()) {
                System.out.println("  " + verifyRs.getString("column_name") + " - " + verifyRs.getString("data_type") + " (" + verifyRs.getString("is_nullable") + ")");
            }
            
            System.out.println("\nDatabase update completed successfully!");
            
        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
