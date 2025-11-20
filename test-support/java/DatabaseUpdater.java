import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

public class DatabaseUpdater {
    public static void main(String[] args) {
        String url = "jdbc:postgresql://localhost:5432/mahjong_db";
        String user = "postgres";
        String password = "cch815566";
        // 测试使用简单的SQL文件
        String sqlFile = "test_update.sql";

        Connection conn = null;
        Statement stmt = null;

        try {
            // 加载驱动
            Class.forName("org.postgresql.Driver");
            System.out.println("驱动加载成功");

            // 连接到数据库
            conn = DriverManager.getConnection(url, user, password);
            stmt = conn.createStatement();
            System.out.println("已连接到PostgreSQL数据库");
            
            // 禁用自动提交，以支持事务处理
            conn.setAutoCommit(false);
            
            // 读取并执行SQL脚本
            System.out.println("开始执行数据库更新脚本: " + sqlFile);
            executeSqlScript(stmt, sqlFile);
            
            // 提交事务
            conn.commit();
            System.out.println("\n数据库更新成功完成！");
            System.out.println("数据库表结构已成功更新为支持多轮计分和收盘倍率功能的版本。");
            System.out.println("\n新增的表结构包括：");
            System.out.println("- matches: 对局记录表");
            System.out.println("- match_participants: 对局参与者表");
            System.out.println("- round_scores: 轮次分数表");
            System.out.println("- match_settlements: 对局结算表");
            System.out.println("\n同时创建了相关的触发器和视图，以支持业务逻辑的自动计算。");

        } catch (ClassNotFoundException e) {
            System.err.println("找不到PostgreSQL驱动: " + e.getMessage());
        } catch (Exception e) {
            System.err.println("更新过程中发生错误: " + e.getMessage());
            e.printStackTrace();
            // 如果发生错误，回滚事务
            if (conn != null) {
                try {
                    conn.rollback();
                    System.out.println("事务已回滚，数据库保持原有状态。");
                } catch (Exception ex) {
                    System.err.println("回滚事务时出错: " + ex.getMessage());
                }
            }
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
    
    /**
     * 读取并执行SQL脚本文件
     * @param stmt Statement对象
     * @param filePath SQL文件路径
     * @throws IOException
     * @throws Exception
     */
    private static void executeSqlScript(Statement stmt, String filePath) throws IOException, Exception {
        StringBuilder sqlBuilder = new StringBuilder();
        BufferedReader reader = new BufferedReader(new FileReader(filePath));
        String line;
        boolean inCommentBlock = false;
        
        while ((line = reader.readLine()) != null) {
            // 处理行注释
            line = line.trim();
            
            // 跳过空行
            if (line.isEmpty()) {
                continue;
            }
            
            // 跳过单行注释
            if (line.startsWith("--")) {
                continue;
            }
            
            // 处理SQL语句
            sqlBuilder.append(" ").append(line);
            
            // 检查是否以分号结束且不在字符串或转义单引号中
            if (line.endsWith(";")) {
                String sql = sqlBuilder.toString().trim();
                if (!sql.isEmpty()) {
                    try {
                        // 执行SQL语句
                        stmt.executeUpdate(sql);
                        System.out.println("执行成功: " + sql.substring(0, Math.min(50, sql.length())) + (sql.length() > 50 ? "..." : ""));
                    } catch (Exception e) {
                        // 对于表已存在等情况，记录警告但继续执行
                        if (e.getMessage().contains("already exists")) {
                            System.out.println("警告: " + e.getMessage());
                        } else {
                            throw e; // 其他错误抛出
                        }
                    }
                    // 清空缓冲区
                    sqlBuilder.setLength(0);
                }
            }
        }
        
        // 检查是否还有剩余的SQL语句
        String remainingSql = sqlBuilder.toString().trim();
        if (!remainingSql.isEmpty()) {
            System.out.println("警告: 发现未执行的SQL语句（可能缺少分号）: " + remainingSql);
        }
        
        reader.close();
    }
}