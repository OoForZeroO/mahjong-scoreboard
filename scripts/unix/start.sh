#!/bin/bash

# 麻将计分板服务启动脚本

echo "正在启动麻将计分板服务..."

# 检查 Java 环境
if ! command -v java &> /dev/null; then
    echo "错误: 未找到 Java 环境，请先安装 JDK 21 或更高版本"
    exit 1
fi

# 检查 Maven 环境
if ! command -v mvn &> /dev/null; then
    echo "警告: 未找到 Maven，尝试使用项目自带的 Maven..."
    if [ -f "tools/maven/bin/mvn" ]; then
        export PATH="$(pwd)/tools/maven/bin:$PATH"
    else
        echo "错误: 未找到 Maven，请先安装 Maven 3.6+"
        exit 1
    fi
fi

# 进入项目根目录
cd "$(dirname "$0")/../.." || exit 1

# 编译并运行
echo "正在编译项目..."
mvn -pl mahjong-scoreboard-start -am clean package -DskipTests

if [ $? -ne 0 ]; then
    echo "错误: 项目编译失败"
    exit 1
fi

echo "编译成功，正在启动应用..."
java -jar mahjong-scoreboard-start/target/mahjong-scoreboard-start-1.0-SNAPSHOT.jar

