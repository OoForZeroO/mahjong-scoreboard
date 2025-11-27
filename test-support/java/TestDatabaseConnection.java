import java.sql.*;

public class TestDatabaseConnection {
    public static void main(String[] args) {
        String url = "jdbc:postgresql://localhost:5432/mahjong_score_system";
        String username = "postgres";
        String password = "cch815566";
        
        try (Connection conn = DriverManager.getConnection(url, username, password)) {
            System.out.println("✓ 数据库连接成功！");
            System.out.println("数据库: " + conn.getCatalog());
            
            // 检查match_results表是否存在
            DatabaseMetaData metaData = conn.getMetaData();
            ResultSet tables = metaData.getTables(null, "public", "match_results", null);
            
            if (tables.next()) {
                System.out.println("✓ match_results表存在");
                
                // 检查表的所有字段
                ResultSet columns = metaData.getColumns(null, "public", "match_results", null);
                System.out.println("\n表字段列表:");
                System.out.println("字段名\t\t数据类型\t\t可空");
                System.out.println("----------------------------------------");
                
                while (columns.next()) {
                    String columnName = columns.getString("COLUMN_NAME");
                    String dataType = columns.getString("TYPE_NAME");
                    String nullable = columns.getString("IS_NULLABLE");
                    System.out.println(columnName + "\t\t" + dataType + "\t\t" + nullable);
                }
                
                // 特别检查total_duration字段
                columns = metaData.getColumns(null, "public", "match_results", "total_duration");
                if (columns.next()) {
                    System.out.println("\n✓ total_duration字段存在");
                    System.out.println("数据类型: " + columns.getString("TYPE_NAME"));
                } else {
                    System.out.println("\n✗ total_duration字段不存在！");
                }
                
            } else {
                System.out.println("✗ match_results表不存在！");
            }
            
        } catch (SQLException e) {
            System.err.println("数据库连接失败: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
