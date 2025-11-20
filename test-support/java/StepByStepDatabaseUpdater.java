import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;
import java.sql.SQLException;

public class StepByStepDatabaseUpdater {
    public static void main(String[] args) {
        String url = "jdbc:postgresql://localhost:5432/mahjong_db";
        String user = "postgres";
        String password = "cch815566";

        Connection conn = null;
        Statement stmt = null;

        try {
            // 加载驱动
            Class.forName("org.postgresql.Driver");
            System.out.println("驱动加载成功");

            // 连接到数据库
            conn = DriverManager.getConnection(url, user, password);
            stmt = conn.createStatement();
            conn.setAutoCommit(false);
            System.out.println("已连接到PostgreSQL数据库");
            
            // 步骤1：创建表结构
            System.out.println("\n=== 步骤1: 创建表结构 ===");
            executeStatements(stmt, getCreateTablesStatements());
            
            // 步骤2：创建索引
            System.out.println("\n=== 步骤2: 创建索引 ===");
            executeStatements(stmt, getCreateIndexesStatements());
            
            // 步骤3：创建触发器函数
            System.out.println("\n=== 步骤3: 创建触发器函数 ===");
            executeStatements(stmt, getCreateTriggerFunctionsStatements());
            
            // 步骤4：创建触发器
            System.out.println("\n=== 步骤4: 创建触发器 ===");
            executeStatements(stmt, getCreateTriggersStatements());
            
            // 步骤5：创建视图
            System.out.println("\n=== 步骤5: 创建视图 ===");
            executeStatements(stmt, getCreateViewsStatements());
            
            // 提交所有更改
            conn.commit();
            System.out.println("\n✅ 数据库更新成功完成！");
            System.out.println("数据库表结构已成功更新为支持多轮计分和收盘倍率功能的版本。");

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
                } catch (SQLException ex) {
                    System.err.println("回滚事务时出错: " + ex.getMessage());
                }
            }
        } finally {
            // 关闭连接
            try {
                if (stmt != null) stmt.close();
                if (conn != null) conn.close();
            } catch (SQLException e) {
                System.err.println("关闭连接时出错: " + e.getMessage());
            }
        }
    }
    
    /**
     * 执行SQL语句数组
     */
    private static void executeStatements(Statement stmt, String[] statements) throws SQLException {
        for (String sql : statements) {
            if (sql.trim().isEmpty()) continue;
            try {
                stmt.executeUpdate(sql);
                System.out.println("✅ 执行成功: " + sql.substring(0, Math.min(60, sql.length())) + 
                                  (sql.length() > 60 ? "..." : ""));
            } catch (SQLException e) {
                // 对于已存在的对象，记录警告但继续
                if (e.getMessage().contains("already exists")) {
                    System.out.println("⚠️  警告: " + e.getMessage().substring(0, Math.min(100, e.getMessage().length())));
                } else {
                    throw e;
                }
            }
        }
    }
    
    /**
     * 获取创建表的SQL语句
     */
    private static String[] getCreateTablesStatements() {
        return new String[] {
            "CREATE TABLE IF NOT EXISTS matches (" +
            "    match_id SERIAL PRIMARY KEY,  " +
            "    room_id BIGINT NOT NULL,      " +
            "    room_name VARCHAR(100) NOT NULL,  " +
            "    start_time BIGINT NOT NULL,   " +
            "    end_time BIGINT,             " +
            "    status VARCHAR(20) NOT NULL DEFAULT '进行中',  " +
            "    total_rounds INTEGER NOT NULL DEFAULT 0,  " +
            "    current_round INTEGER DEFAULT 0,  " +
            "    settlement_multiplier DECIMAL(10,2),  " +
            "    create_time BIGINT NOT NULL," +
            "    update_time BIGINT NOT NULL," +
            "    FOREIGN KEY (room_id) REFERENCES rooms(id)" +
            ")",
            
            "CREATE TABLE IF NOT EXISTS match_participants (" +
            "    id SERIAL PRIMARY KEY," +
            "    match_id BIGINT NOT NULL,     " +
            "    user_id BIGINT,              " +
            "    nickname VARCHAR(100) NOT NULL,  " +
            "    avatar VARCHAR(500),         " +
            "    total_score INTEGER NOT NULL DEFAULT 0,  " +
            "    final_score DECIMAL(20,2),   " +
            "    is_quit BOOLEAN NOT NULL DEFAULT FALSE,  " +
            "    quit_time BIGINT,            " +
            "    create_time BIGINT NOT NULL," +
            "    update_time BIGINT NOT NULL," +
            "    FOREIGN KEY (match_id) REFERENCES matches(match_id)," +
            "    FOREIGN KEY (user_id) REFERENCES users(id)," +
            "    CONSTRAINT unique_participant_per_match UNIQUE(match_id, nickname)" +
            ")",
            
            "CREATE TABLE IF NOT EXISTS round_scores (" +
            "    id SERIAL PRIMARY KEY," +
            "    match_id BIGINT NOT NULL,     " +
            "    participant_id BIGINT NOT NULL,  " +
            "    round_number INTEGER NOT NULL,  " +
            "    score INTEGER NOT NULL,       " +
            "    cumulative_score INTEGER NOT NULL,  " +
            "    create_time BIGINT NOT NULL," +
            "    update_time BIGINT NOT NULL," +
            "    FOREIGN KEY (match_id) REFERENCES matches(match_id)," +
            "    FOREIGN KEY (participant_id) REFERENCES match_participants(id)," +
            "    UNIQUE(match_id, participant_id, round_number)" +
            ")",
            
            "CREATE TABLE IF NOT EXISTS match_settlements (" +
            "    settlement_id SERIAL PRIMARY KEY," +
            "    match_id BIGINT NOT NULL,     " +
            "    multiplier DECIMAL(10,2) NOT NULL,  " +
            "    settlement_time BIGINT NOT NULL,  " +
            "    notes TEXT,                  " +
            "    create_time BIGINT NOT NULL," +
            "    update_time BIGINT NOT NULL," +
            "    FOREIGN KEY (match_id) REFERENCES matches(match_id)," +
            "    UNIQUE(match_id)" +
            ")"
        };
    }
    
    /**
     * 获取创建索引的SQL语句
     */
    private static String[] getCreateIndexesStatements() {
        return new String[] {
            "CREATE INDEX idx_matches_room_id ON matches(room_id)",
            "CREATE INDEX idx_matches_status ON matches(status)",
            "CREATE INDEX idx_matches_start_time ON matches(start_time)",
            "CREATE INDEX idx_match_participants_match_id ON match_participants(match_id)",
            "CREATE INDEX idx_match_participants_user_id ON match_participants(user_id)",
            "CREATE INDEX idx_match_participants_is_quit ON match_participants(is_quit)",
            "CREATE INDEX idx_round_scores_match_id ON round_scores(match_id)",
            "CREATE INDEX idx_round_scores_participant_id ON round_scores(participant_id)",
            "CREATE INDEX idx_round_scores_round_number ON round_scores(round_number)",
            "CREATE INDEX idx_round_scores_match_round ON round_scores(match_id, round_number)",
            "CREATE INDEX idx_match_settlements_match_id ON match_settlements(match_id)"
        };
    }
    
    /**
     * 获取创建触发器函数的SQL语句
     */
    private static String[] getCreateTriggerFunctionsStatements() {
        return new String[] {
            "CREATE OR REPLACE FUNCTION update_match_total_rounds() RETURNS TRIGGER AS $$" +
            "BEGIN" +
            "    UPDATE matches" +
            "    SET total_rounds = GREATEST(COALESCE(total_rounds, 0), NEW.round_number)," +
            "        current_round = GREATEST(COALESCE(current_round, 0), NEW.round_number)," +
            "        update_time = EXTRACT(EPOCH FROM NOW())::bigint" +
            "    WHERE match_id = NEW.match_id;" +
            "    RETURN NEW;" +
            "END;" +
            "$$ LANGUAGE plpgsql",
            
            "CREATE OR REPLACE FUNCTION update_participant_total_score() RETURNS TRIGGER AS $$" +
            "BEGIN" +
            "    UPDATE match_participants" +
            "    SET total_score = (SELECT COALESCE(SUM(score), 0) FROM round_scores WHERE participant_id = NEW.participant_id)," +
            "        update_time = EXTRACT(EPOCH FROM NOW())::bigint" +
            "    WHERE id = NEW.participant_id;" +
            "    RETURN NEW;" +
            "END;" +
            "$$ LANGUAGE plpgsql",
            
            "CREATE OR REPLACE FUNCTION update_final_scores() RETURNS TRIGGER AS $$" +
            "BEGIN" +
            "    UPDATE matches" +
            "    SET status = '已完成'," +
            "        end_time = NEW.settlement_time," +
            "        settlement_multiplier = NEW.multiplier," +
            "        update_time = EXTRACT(EPOCH FROM NOW())::bigint" +
            "    WHERE match_id = NEW.match_id;" +
            "    " +
            "    UPDATE match_participants" +
            "    SET final_score = total_score * NEW.multiplier," +
            "        update_time = EXTRACT(EPOCH FROM NOW())::bigint" +
            "    WHERE match_id = NEW.match_id;" +
            "    " +
            "    RETURN NEW;" +
            "END;" +
            "$$ LANGUAGE plpgsql"
        };
    }
    
    /**
     * 获取创建触发器的SQL语句
     */
    private static String[] getCreateTriggersStatements() {
        return new String[] {
            "CREATE TRIGGER trg_update_match_total_rounds " +
            "AFTER INSERT ON round_scores " +
            "FOR EACH ROW EXECUTE PROCEDURE update_match_total_rounds()",
            
            "CREATE TRIGGER trg_update_participant_total_score " +
            "AFTER INSERT OR UPDATE ON round_scores " +
            "FOR EACH ROW EXECUTE PROCEDURE update_participant_total_score()",
            
            "CREATE TRIGGER trg_update_final_scores " +
            "AFTER INSERT ON match_settlements " +
            "FOR EACH ROW EXECUTE PROCEDURE update_final_scores()"
        };
    }
    
    /**
     * 获取创建视图的SQL语句
     */
    private static String[] getCreateViewsStatements() {
        return new String[] {
            "CREATE OR REPLACE VIEW v_match_details AS " +
            "SELECT " +
            "    m.match_id," +
            "    m.room_id," +
            "    m.room_name," +
            "    m.start_time," +
            "    m.end_time," +
            "    m.status," +
            "    m.total_rounds," +
            "    m.current_round," +
            "    m.settlement_multiplier," +
            "    COUNT(mp.id) as participant_count," +
            "    SUM(mp.total_score) as total_match_score " +
            "FROM matches m " +
            "LEFT JOIN match_participants mp ON m.match_id = mp.match_id " +
            "GROUP BY m.match_id",
            
            "CREATE OR REPLACE VIEW v_match_participant_details AS " +
            "SELECT " +
            "    mp.id as participant_record_id," +
            "    m.match_id," +
            "    m.room_name," +
            "    m.status," +
            "    m.settlement_multiplier," +
            "    mp.user_id," +
            "    COALESCE(u.username, mp.nickname) as participant_name," +
            "    mp.avatar," +
            "    mp.total_score," +
            "    mp.final_score," +
            "    mp.is_quit," +
            "    mp.quit_time " +
            "FROM match_participants mp " +
            "JOIN matches m ON mp.match_id = m.match_id " +
            "LEFT JOIN users u ON mp.user_id = u.id",
            
            "CREATE OR REPLACE VIEW v_round_score_details AS " +
            "SELECT " +
            "    rs.id," +
            "    m.match_id," +
            "    m.room_name," +
            "    m.status," +
            "    rs.round_number," +
            "    mp.id as participant_record_id," +
            "    COALESCE(u.username, mp.nickname) as participant_name," +
            "    rs.score," +
            "    rs.cumulative_score," +
            "    rs.create_time " +
            "FROM round_scores rs " +
            "JOIN matches m ON rs.match_id = m.match_id " +
            "JOIN match_participants mp ON rs.participant_id = mp.id " +
            "LEFT JOIN users u ON mp.user_id = u.id " +
            "ORDER BY m.match_id, rs.round_number, rs.id"
        };
    }
}