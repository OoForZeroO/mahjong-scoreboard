import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;

public class DatabaseValidator {
    public static void main(String[] args) {
        String url = "jdbc:postgresql://localhost:5432/mahjong_db";
        String user = "postgres";
        String password = "cch815566";

        Connection conn = null;
        Statement stmt = null;

        try {
            // 加载驱动
            Class.forName("org.postgresql.Driver");
            System.out.println("验证数据库表结构...");

            // 连接到数据库
            conn = DriverManager.getConnection(url, user, password);
            stmt = conn.createStatement();

            // 检查users表
            ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM users");
            rs.next();
            int userCount = rs.getInt(1);
            System.out.println("✅ users表存在，当前有 " + userCount + " 条记录");

            // 检查rooms表
            rs = stmt.executeQuery("SELECT COUNT(*) FROM rooms");
            rs.next();
            int roomCount = rs.getInt(1);
            System.out.println("✅ rooms表存在，当前有 " + roomCount + " 条记录");

            // 检查score_records表
            rs = stmt.executeQuery("SELECT COUNT(*) FROM score_records");
            rs.next();
            int recordCount = rs.getInt(1);
            System.out.println("✅ score_records表存在，当前有 " + recordCount + " 条记录");

            // 显示用户表的内容
            System.out.println("\n用户表内容预览：");
            rs = stmt.executeQuery("SELECT id, username, phone FROM users");
            while (rs.next()) {
                System.out.println("- ID: " + rs.getLong("id") + ", 用户名: " + rs.getString("username") + ", 手机号: " + rs.getString("phone"));
            }

            System.out.println("\n🎉 数据库表结构验证成功！所有必需的表都已创建完成。");
            System.out.println("现在可以启动Spring Boot应用程序进行开发了。");

        } catch (Exception e) {
            System.err.println("❌ 验证失败: " + e.getMessage());
        } finally {
            // 关闭连接
            try {
                if (stmt != null) stmt.close();
                if (conn != null) conn.close();
            } catch (Exception e) {
                System.err.println("关闭连接时出错: " + e.getMessage());
            }
        }
    }
}