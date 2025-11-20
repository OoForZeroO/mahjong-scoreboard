# PostgreSQL手动配置指南

## 环境检查

根据测试，您的系统上：
- PostgreSQL 9.3服务已安装并运行
- Java环境可能未正确配置（java/javac命令不可用）

## 1. 配置Java环境

### 安装JDK

1. 下载JDK 11或更高版本：[Oracle JDK下载](https://www.oracle.com/java/technologies/downloads/) 或 [OpenJDK下载](https://adoptium.net/)

2. 安装时请记住安装路径（例如：`C:\Program Files\Java\jdk-17`）

3. 配置环境变量：
   - 右键点击「此电脑」→「属性」→「高级系统设置」→「环境变量」
   - 在「系统变量」中找到`Path`，点击「编辑」
   - 添加JDK的bin目录路径（例如：`C:\Program Files\Java\jdk-17\bin`）
   - 点击「确定」保存

4. 验证安装：打开新的命令提示符窗口，运行：
   ```
   java -version
   javac -version
   ```
   如果显示版本信息，则配置成功。

## 2. 测试PostgreSQL连接

### 使用psql命令行工具

1. 打开命令提示符（以管理员身份运行）

2. 连接到PostgreSQL（使用Windows搜索找到并运行「SQL Shell (psql)」）

3. 输入以下连接信息：
   - Server: localhost
   - Database: postgres
   - Port: 5432
   - Username: postgres
   - Password: 您的PostgreSQL密码（默认为空或'postgres'）

4. 连接成功后，运行以下命令：
   ```sql
   -- 创建开发数据库
   CREATE DATABASE devdb;
   
   -- 创建开发用户
   CREATE USER devuser WITH PASSWORD 'devpassword';
   
   -- 授予权限
   GRANT ALL PRIVILEGES ON DATABASE devdb TO devuser;
   ```

## 3. 手动创建数据库和用户

如果上述方法不工作，请按照以下步骤手动创建：

1. 打开「SQL Shell (psql)」并以postgres用户登录

2. 运行以下SQL命令：
   ```sql
   -- 创建数据库
   CREATE DATABASE devdb;
   
   -- 创建用户
   CREATE USER devuser WITH PASSWORD 'devpassword';
   
   -- 切换到新数据库
   \c devdb
   
   -- 在devdb数据库中授予权限
   GRANT ALL ON SCHEMA public TO devuser;
   ```

## 4. 配置文件说明

### 主要配置文件

1. **Spring Boot配置**：`java/demo/src/main/resources/application.properties`
   ```properties
   spring.datasource.url=jdbc:postgresql://localhost:5432/devdb
   spring.datasource.username=devuser
   spring.datasource.password=devpassword
   spring.datasource.driver-class-name=org.postgresql.Driver
   spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.PostgreSQLDialect
   ```

2. **PostgreSQL配置**：`config/postgresql_config.properties`
   - 包含完整的数据库连接参数

## 5. 手动运行Java程序

当Java环境配置好后，可以按以下步骤运行示例：

1. 确保PostgreSQL JDBC驱动已下载：
   - 已下载到：`d:\YH\lib\postgresql-42.5.4.jar`

2. 编译程序：
   ```bash
   cd d:\YH\java\demo
   javac -cp "d:\YH\lib\postgresql-42.5.4.jar" src/main/java/com/example/demo/PostgreSQLConnectionExample.java -d target/classes
   ```

3. 运行程序：
   ```bash
   java -cp "target/classes;d:\YH\lib\postgresql-42.5.4.jar" com.example.demo.PostgreSQLConnectionExample
   ```

## 6. 常见问题解决方案

### 连接问题

1. **密码错误**
   - 正确密码：`cch815566`
   - 如果需要重置密码，请参考PostgreSQL快速入门指南

2. **权限被拒绝**
   - 修改`C:\Program Files\PostgreSQL\9.3\data\pg_hba.conf`文件
   - 将认证方式从'peer'或'ident'改为'md5'
   - 重启PostgreSQL服务

3. **服务未启动**
   - 在Windows服务管理器中启动`postgresql-x64-9.3`服务
   - 或运行命令：`net start postgresql-x64-9.3`

### Java环境问题

1. **java/javac命令不可用**
   - 确保JDK已正确安装
   - 验证环境变量Path中包含JDK的bin目录
   - 重新打开命令提示符窗口

2. **驱动找不到**
   - 确保使用正确的classpath包含PostgreSQL JDBC驱动
   - 驱动文件位置：`d:\YH\lib\postgresql-42.5.4.jar`

## 7. 总结

您的PostgreSQL数据库环境和Maven构建工具已成功配置完成！

### 主要配置信息
- **PostgreSQL配置**
  - **超级用户**：postgres
  - **超级用户密码**：cch815566
  - **开发数据库**：devdb
  - **开发用户**：devuser
  - **开发用户密码**：devpassword
  - **端口**：5432
  - **主机**：localhost

- **Java配置**
  - **JDK版本**：24.0.1 (Eclipse Adoptium) 和 21.0.9
  - **JDBC驱动**：postgresql-42.5.4.jar (位于 d:\YH\lib\)

- **Maven配置**
  - **Maven版本**：3.9.6
  - **安装路径**：d:\YH\apache-maven-3.9.6
  - **启动脚本**：d:\YH\test_maven.bat

### 已完成的配置
1. ✅ PostgreSQL服务正常运行
2. ✅ Java环境配置正确
3. ✅ Maven构建工具安装完成
4. ✅ 成功创建开发数据库和用户
5. ✅ 创建了用户表和产品表
6. ✅ 成功插入和查询测试数据

### 使用Maven的方法
1. **启动Maven环境**：运行 `d:\YH\test_maven.bat`
2. **验证安装**：脚本会自动显示Maven版本信息
3. **创建Maven项目**：
   ```
   cd d:\YH\java
   mvn archetype:generate -DgroupId=com.example -DartifactId=yourproject -DarchetypeArtifactId=maven-archetype-quickstart -DinteractiveMode=false
   ```
4. **构建项目**：
   ```
   cd d:\YH\java\yourproject
   mvn clean compile package
   ```

### 后续操作
您现在拥有一个完整的Java服务端开发环境，包括PostgreSQL数据库、Java JDK和Maven构建工具。所有必要的组件都已正确配置，可以立即开始开发工作。

如果需要更多帮助，请参考完整的PostgreSQL文档、Java文档或Maven文档。