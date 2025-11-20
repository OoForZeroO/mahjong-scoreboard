# 麻将积分器服务端系统

## 项目简介

这是一个基于 Spring Boot 开发的麻将积分器服务端系统，提供完整的用户管理、棋牌室管理和麻将计分记录管理功能。系统采用 RESTful API 设计，支持数据的增删改查操作。

## 技术栈

- **后端框架**：Spring Boot 3.2.0
- **ORM框架**：Spring Data JPA
- **数据库**：PostgreSQL
- **构建工具**：Maven
- **开发语言**：Java 17+

## 系统功能模块

### 1. 用户管理模块
- 用户注册与信息管理
- 根据手机号查询用户
- 用户信息更新与删除

### 2. 棋牌室管理模块
- 棋牌室信息的增删改查
- 棋牌室名称唯一性验证

### 3. 计分记录管理模块
- 创建和管理麻将对局计分记录
- 支持按用户ID、棋牌室ID、状态等条件查询计分记录
- 支持查询用户近期的计分记录

## 数据库设计

### 用户表 (users)
- `id` (主键)：用户ID，自动生成
- `username`：用户名称
- `phone`：手机号，唯一键
- `avatar`：用户头像URL
- `create_time`：创建时间
- `update_time`：更新时间

### 棋牌室表 (rooms)
- `id` (主键)：棋牌室ID，自动生成
- `name`：棋牌室名称，唯一键
- `logo`：棋牌室LOGO URL
- `create_time`：创建时间
- `update_time`：更新时间

### 计分记录表 (score_records)
- `match_id` (主键)：对局ID，自动生成
- `user_id`：用户ID
- `rounds`：对局圈数
- `score`：对局分数
- `total_score`：对局总分
- `status`：对局状态（进行中、已完成、已取消）
- `user_status`：对局用户状态
- `room_name`：对局棋牌室名称
- `room_id`：棋牌室ID
- `create_time`：创建时间
- `update_time`：更新时间

## API接口列表

### 用户相关接口
- `POST /api/users` - 创建新用户
- `GET /api/users` - 获取所有用户列表
- `GET /api/users/{id}` - 根据ID获取用户信息
- `GET /api/users/phone/{phone}` - 根据手机号获取用户信息
- `PUT /api/users/{id}` - 更新用户信息
- `DELETE /api/users/{id}` - 删除用户
- `GET /api/users/exists/phone/{phone}` - 检查手机号是否已存在

### 棋牌室相关接口
- `POST /api/rooms` - 创建新棋牌室
- `GET /api/rooms` - 获取所有棋牌室列表
- `GET /api/rooms/{id}` - 根据ID获取棋牌室信息
- `PUT /api/rooms/{id}` - 更新棋牌室信息
- `DELETE /api/rooms/{id}` - 删除棋牌室
- `GET /api/rooms/exists/name/{name}` - 检查棋牌室名称是否已存在

### 计分记录相关接口
- `POST /api/score-records` - 创建新计分记录
- `GET /api/score-records` - 获取所有计分记录
- `GET /api/score-records/{matchId}` - 根据对局ID获取计分记录
- `GET /api/score-records/user/{userId}` - 根据用户ID获取计分记录
- `GET /api/score-records/user/{userId}/recent` - 获取用户最近的计分记录
- `GET /api/score-records/room/{roomId}` - 根据棋牌室ID获取计分记录
- `GET /api/score-records/status/{status}` - 根据状态获取计分记录
- `GET /api/score-records/user/{userId}/status/{status}` - 根据用户ID和状态获取计分记录
- `PUT /api/score-records/{matchId}` - 更新计分记录
- `DELETE /api/score-records/{matchId}` - 删除计分记录

## 快速开始

### 环境要求
- JDK 17 或更高版本
- Maven 3.6+ 或使用项目自带的Maven
- PostgreSQL 12+ 数据库

### 配置步骤
1. 确保 PostgreSQL 数据库已启动
2. 修改 `../mahjong-scoreboard-start/src/main/resources/application.properties` 文件中的数据库连接信息
3. 使用仓库根目录下 `database/migrations/db-init.sql` 初始脚本创建基础表结构

### 运行项目
1. Windows 环境执行 `../scripts/windows/run.bat`
2. 或在仓库根目录下运行：`mvn -pl mahjong-scoreboard-start -am clean package && java -jar mahjong-scoreboard-start/target/mahjong-scoreboard-start-1.0-SNAPSHOT.jar`

## 注意事项
- 系统默认运行在8080端口
- 数据库连接配置在 `application.properties` 文件中
- 项目使用Spring Boot自动建表功能，首次运行会自动创建表结构
- 建议在生产环境中关闭自动建表功能，使用手动创建表结构

## 项目结构

```
├── src/main/java/com/mahjong/
│   ├── controller/          # 控制器层，处理 HTTP 请求
│   ├── dto/                 # 数据传输对象
│   ├── model/               # 数据模型实体
│   ├── repository/          # 数据访问接口
│   └── service/             # 业务逻辑与实现
│       └── impl/            # 服务实现类
├── pom.xml                  # 模块 Maven 描述文件
└── README.md                # 模块说明文档
```

## 许可证

MIT License