# 修复total_duration字段不存在的错误

## 问题描述

错误信息：`字段 mr1_0.total_duration 不存在`

**根本原因**：MatchResult实体类中定义了`total_duration`字段，但数据库表中不存在此字段，导致JPA查询时出错。

## 解决方案

### 方案1：临时修复（推荐）
暂时注释掉MatchResult实体类中的`total_duration`字段，让应用能够正常运行。

**已执行的操作**：
1. 注释掉了`@Column(name = "total_duration")`注解
2. 注释掉了`totalDuration`字段声明
3. 注释掉了相关的getter和setter方法

### 方案2：数据库修复（长期方案）
执行数据库修复脚本，添加`total_duration`字段。

**执行步骤**：
```bash
psql -U your_username -d your_database -f fix_total_duration_field.sql
```

## 修复后的代码

### MatchResult实体类
```java
// 临时注释掉，直到数据库表添加total_duration字段
// @Column(name = "total_duration")
// private Long totalDuration;

// 临时注释掉，直到数据库表添加total_duration字段
// public Long getTotalDuration() {
//     return totalDuration;
// }

// public void setTotalDuration(Long totalDuration) {
//     this.totalDuration = totalDuration;
// }
```

### 数据库修复脚本
```sql
-- 添加total_duration字段（如果不存在）
DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM information_schema.columns 
        WHERE table_name = 'match_results' AND column_name = 'total_duration'
    ) THEN
        ALTER TABLE match_results ADD COLUMN total_duration BIGINT;
        RAISE NOTICE 'Added total_duration column to match_results table';
    ELSE
        RAISE NOTICE 'total_duration column already exists in match_results table';
    END IF;
END $$;

-- 为现有记录设置默认值
UPDATE match_results SET total_duration = 0 WHERE total_duration IS NULL;
```

## 后续步骤

### 立即可用
现在应用应该能够正常启动和运行，收盘接口也应该能够正常工作。

### 长期修复
1. 执行`fix_total_duration_field.sql`脚本
2. 恢复MatchResult实体类中的`total_duration`字段
3. 测试功能是否正常

## 注意事项

1. **临时方案**：注释掉字段是临时解决方案，不影响核心功能
2. **数据库一致性**：建议尽快执行数据库修复脚本
3. **功能完整性**：`total_duration`字段用于记录对局总时长，不是核心必需功能

## 验证步骤

1. 重启应用，确认启动成功
2. 测试收盘接口，确认不再出现字段不存在的错误
3. 验证MatchResult记录正确保存
4. 检查对局结果统计是否正确

这个修复方案确保了应用的正常运行，同时为后续的数据库修复提供了清晰的路径。
