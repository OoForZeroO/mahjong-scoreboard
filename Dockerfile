# 多阶段构建 - 构建阶段
FROM maven:3.9-eclipse-temurin-21 AS build

WORKDIR /app

# 复制 pom.xml 文件（利用 Docker 缓存层）
COPY pom.xml .
COPY mahjong-scoreboard-service/pom.xml ./mahjong-scoreboard-service/
COPY mahjong-scoreboard-start/pom.xml ./mahjong-scoreboard-start/

# 下载依赖（利用 Docker 缓存）
RUN mvn dependency:go-offline -B

# 复制源代码
COPY mahjong-scoreboard-service/src ./mahjong-scoreboard-service/src
COPY mahjong-scoreboard-start/src ./mahjong-scoreboard-start/src

# 构建应用
RUN mvn clean package -DskipTests

# 运行阶段
FROM eclipse-temurin:21-jre-alpine

WORKDIR /app

# 从构建阶段复制 JAR 文件
COPY --from=build /app/mahjong-scoreboard-start/target/mahjong-scoreboard-start-*.jar app.jar

# 安装 wget 用于健康检查
RUN apk add --no-cache wget

# 创建日志目录并赋权，再创建非 root 用户
RUN mkdir -p /app/logs && addgroup -S spring && adduser -S spring -G spring && chown -R spring:spring /app
USER spring:spring

# 暴露端口
EXPOSE 8080

# 健康检查
HEALTHCHECK --interval=30s --timeout=3s --start-period=40s --retries=3 \
  CMD wget --no-verbose --tries=1 --spider http://localhost:8080/actuator/health || exit 1

# 启动应用
ENTRYPOINT ["java", "-jar", "app.jar"]

