import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class DatabaseInitializer {
    private static final String DB_HOST = "localhost";
    private static final int DB_PORT = 5432;
    private static final String DB_ADMIN_NAME = "postgres";
    private static final String DB_ADMIN_USER = "postgres";
    private static final String DB_ADMIN_PASSWORD = "cch815566";
    private static final String DB_NAME = "mahjong_score_system";
    private static final String SQL_FILE_PATH = "create_database_tables_simple.sql";
    
    public static void main(String[] args) {
        System.out.println("开始初始化PostgreSQL数据库...");
        
        try {
            // 加载驱动
            Class.forName("org.postgresql.Driver");
            System.out.println("PostgreSQL驱动加载成功");
            
            // 首先检查数据库是否存在
            if (!checkDatabaseExists()) {
                System.out.println("数据库不存在，正在创建...");
                createDatabase();
            } else {
                System.out.println("数据库已存在，跳过创建");
            }
            
            // 读取SQL文件
            String sqlContent = readSqlFile();
            
            // 执行SQL脚本
            executeSqlScript(sqlContent);
            
            System.out.println("\n数据库初始化成功！");
            System.out.println("已创建表：users, wechat_users, rooms, matches, match_participants, round_scores, match_results, match_settlements, score_records");
            
        } catch (ClassNotFoundException e) {
            System.err.println("错误：找不到PostgreSQL驱动。请确保lib目录下有postgresql驱动jar包。");
            e.printStackTrace();
        } catch (SQLException e) {
            System.err.println("错误：数据库操作失败。");
            e.printStackTrace();
        } catch (IOException e) {
            System.err.println("错误：读取SQL文件失败。");
            e.printStackTrace();
        } catch (Exception e) {
            System.err.println("错误：发生未知错误。");
            e.printStackTrace();
        }
        
        System.out.println("\n按Enter键继续...");
        try {
            System.in.read();
        } catch (IOException e) {
        }
    }
    
    private static boolean checkDatabaseExists() throws SQLException {
        String url = String.format("jdbc:postgresql://%s:%d/%s", DB_HOST, DB_PORT, DB_ADMIN_NAME);
        try (Connection conn = DriverManager.getConnection(url, DB_ADMIN_USER, DB_ADMIN_PASSWORD);
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT 1 FROM pg_database WHERE datname = '" + DB_NAME + "'");) {
            return rs.next();
        }
    }
    
    private static void createDatabase() throws SQLException {
        String url = String.format("jdbc:postgresql://%s:%d/%s", DB_HOST, DB_PORT, DB_ADMIN_NAME);
        try (Connection conn = DriverManager.getConnection(url, DB_ADMIN_USER, DB_ADMIN_PASSWORD);
             Statement stmt = conn.createStatement()) {
            stmt.executeUpdate("CREATE DATABASE " + DB_NAME);
            System.out.println("数据库创建成功：" + DB_NAME);
        }
    }
    
    private static String readSqlFile() throws IOException {
        File file = new File(SQL_FILE_PATH);
        if (!file.exists()) {
            throw new IOException("SQL文件不存在：" + SQL_FILE_PATH);
        }
        return new String(Files.readAllBytes(file.toPath()));
    }
    
    private static void executeSqlScript(String sqlContent) throws SQLException {
        String url = String.format("jdbc:postgresql://%s:%d/%s", DB_HOST, DB_PORT, DB_NAME);
        try (Connection conn = DriverManager.getConnection(url, DB_ADMIN_USER, DB_ADMIN_PASSWORD)) {
            // 设置自动提交为false，这样可以控制事务
            conn.setAutoCommit(true);
            
            // 将SQL语句按块分割，而不是简单地按分号分割
            List<String> sqlBlocks = parseSqlBlocks(sqlContent);
            
            for (String block : sqlBlocks) {
                block = block.trim();
                if (!block.isEmpty() && !block.startsWith("--") && !block.startsWith("/*")) {
                    try (Statement stmt = conn.createStatement()) {
                        System.out.println("正在执行SQL块...");
                        stmt.execute(block);
                        System.out.println("SQL块执行成功");
                    } catch (SQLException e) {
                        // 更全面地捕获常见错误
                        String errorMsg = e.getMessage();
                        if (errorMsg.contains("already exists")) {
                            System.out.println("对象已存在，跳过");
                        } else if (errorMsg.contains("不存在") || errorMsg.contains("does not exist")) {
                            System.out.println("警告：引用的对象不存在，稍后重试该语句");
                            // 稍后重试这些语句
                            handleDependentStatements(conn, block);
                        } else {
                            System.out.println("执行失败，继续执行下一个块：" + errorMsg);
                        }
                    }
                }
            }
            
            System.out.println("SQL脚本执行完成");
        }
    }
    
    private static List<String> parseSqlBlocks(String sqlContent) {
        List<String> blocks = new ArrayList<>();
        StringBuilder currentBlock = new StringBuilder();
        boolean inComment = false;
        boolean inString = false;
        char stringDelimiter = '"';
        
        for (int i = 0; i < sqlContent.length(); i++) {
            char c = sqlContent.charAt(i);
            
            // 处理字符串
            if ((c == '\'' || c == '"') && !inComment) {
                if (!inString) {
                    inString = true;
                    stringDelimiter = c;
                } else if (c == stringDelimiter && i > 0 && sqlContent.charAt(i-1) != '\\') {
                    inString = false;
                }
            }
            
            // 处理注释
            if (!inString && !inComment) {
                if (i < sqlContent.length() - 1 && c == '-' && sqlContent.charAt(i+1) == '-') {
                    inComment = true;
                } else if (i < sqlContent.length() - 1 && c == '/' && sqlContent.charAt(i+1) == '*') {
                    inComment = true;
                    i++; // 跳过 *
                }
            }
            
            if (inComment) {
                if (c == '\n') {
                    inComment = false; // 行注释结束
                } else if (i < sqlContent.length() - 1 && c == '*' && sqlContent.charAt(i+1) == '/') {
                    inComment = false;
                    i++; // 跳过 /
                }
            }
            
            // 添加字符到当前块
            if (!inComment || (inComment && (c == '\n' || (i < sqlContent.length() - 1 && c == '*' && sqlContent.charAt(i+1) == '/')))) {
                currentBlock.append(c);
            }
            
            // 检查是否为语句结束
            if (c == ';' && !inString && !inComment) {
                blocks.add(currentBlock.toString());
                currentBlock = new StringBuilder();
            }
        }
        
        // 添加最后一个块（如果有）
        if (currentBlock.length() > 0) {
            blocks.add(currentBlock.toString().trim());
        }
        
        return blocks;
    }
    
    private static void handleDependentStatements(Connection conn, String sqlBlock) {
        // 简单处理：等待1秒后重试
        try {
            Thread.sleep(1000);
            try (Statement stmt = conn.createStatement()) {
                stmt.execute(sqlBlock);
                System.out.println("重试成功");
            }
        } catch (Exception e) {
            System.out.println("重试失败：" + e.getMessage());
        }
    }
}