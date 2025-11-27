# 简化的收盘接口逻辑说明

## 概述

经过重新梳理，收盘接口已经简化为纯粹的对局结束逻辑，移除了所有棋牌室管理相关的复杂逻辑。

## 主要变化

### 1. 移除的功能
- ❌ `roomId` 字段和相关逻辑
- ❌ `Room` 实体类和 `RoomRepository`
- ❌ `ensureRoomIdIsSet()` 方法
- ❌ 房间创建和查找逻辑
- ❌ 外键约束和数据库关联

### 2. 保留的功能
- ✅ `roomName` 字段（仅作为显示名称）
- ✅ 对局状态管理
- ✅ 参与者得分计算
- ✅ 对局结果统计
- ✅ 收盘倍率设置

## 收盘接口逻辑

### 接口1：简单结束对局
```http
PUT /api/v1/matches/{matchId}/end
```

**逻辑流程：**
1. 查找对局记录
2. 设置对局状态为"已完成"
3. 设置结束时间
4. 计算对局结果（获胜者、最高分、最低分）
5. 保存对局结果记录
6. 保存对局更新

### 接口2：带参数结束对局
```http
PUT /api/v1/matches/{matchId}/end
Content-Type: application/json

{
  "roomName": "房间名称",
  "multiplier": 1.5
}
```

**逻辑流程：**
1. 查找对局记录
2. 设置对局状态为"已完成"
3. 设置结束时间
4. 更新房间名称（如果提供）
5. 设置收盘倍率（如果提供）
6. 计算对局结果（获胜者、最高分、最低分）
7. 保存对局结果记录
8. 保存对局更新

## 核心代码结构

### Match实体类
```java
@Entity
@Table(name = "matches")
public class Match {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "match_id")
    private Long matchId;
    
    @Column(name = "room_name", nullable = true, length = 100)
    private String roomName;  // 仅作为显示字段
    
    @Column(name = "start_time", nullable = false)
    private Long startTime;
    
    @Column(name = "end_time")
    private Long endTime;
    
    @Column(nullable = false)
    private Integer status = 0; // 0:进行中, 1:已完成
    
    @Column(name = "total_rounds", nullable = false)
    private Integer totalRounds = 0;
    
    @Column(name = "settlement_multiplier")
    private Double settlementMultiplier;
    
    // ... 其他字段
}
```

### 简化的endMatch方法
```java
@Override
@Transactional
public Match endMatch(Long id) {
    Optional<Match> e = dao.findById(id);
    if (e.isPresent()) {
        Match u = e.get();
        u.setStatus(1); // 1:已完成
        u.setEndTime(System.currentTimeMillis());
        
        // 计算并保存对局结果
        MatchParticipant w = getMatchWinner(id);
        MatchResult r = new MatchResult();
        r.setMatchId(id);
        r.setMatch(u);
        r.setWinner(w);
        
        // 计算最高分和最低分
        List<MatchParticipant> ps = pdao.findByMatch(u);
        Integer h = null, l = null;
        for (MatchParticipant p : ps) {
            Integer s = p.getTotalScore();
            if (h == null || s > h) h = s;
            if (l == null || s < l) l = s;
        }
        
        r.setHighestScore(h);
        r.setLowestScore(l);
        
        mdao.save(r);
        return dao.save(u);
    }
    return null;
}
```

## 数据库表结构

### matches表（简化版）
```sql
CREATE TABLE matches (
    match_id SERIAL PRIMARY KEY,
    room_name VARCHAR(100),       -- 仅作为显示字段
    start_time BIGINT NOT NULL,
    end_time BIGINT,
    status INTEGER NOT NULL DEFAULT 0,
    total_rounds INTEGER NOT NULL DEFAULT 0,
    settlement_multiplier DECIMAL(10,2),
    create_time BIGINT NOT NULL,
    update_time BIGINT NOT NULL
);
```

## 优势

1. **简化逻辑**：移除了复杂的房间管理逻辑
2. **减少错误**：不再有roomId为null的约束错误
3. **提高性能**：减少了数据库查询和关联操作
4. **易于维护**：代码结构更清晰，逻辑更简单
5. **灵活性强**：roomName可以作为任意字符串存储

## 使用建议

1. **数据库迁移**：执行 `simplified_database_schema.sql` 更新表结构
2. **测试验证**：确保所有收盘相关功能正常工作
3. **数据清理**：清理不再需要的rooms表和相关数据
4. **文档更新**：更新API文档，移除房间管理相关接口

## 注意事项

- `roomName` 字段现在仅作为显示名称，不进行任何验证或关联
- 对局结果计算逻辑保持不变
- 所有现有的对局数据需要迁移到新的表结构
