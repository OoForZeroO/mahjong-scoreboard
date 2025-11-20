# Java环境配置指南

本目录提供Java开发环境的配置指南和必要文件。

## 步骤1：下载并安装JDK

推荐使用JDK 11或JDK 17（长期支持版本）：

1. 访问Oracle JDK下载页面或使用OpenJDK
2. 下载适合Windows的JDK安装包
3. 运行安装程序并按照提示完成安装

## 步骤2：配置环境变量

1. 右键点击「此电脑」→「属性」→「高级系统设置」→「环境变量」

2. 在「系统变量」中，点击「新建」，创建以下变量：
   - 变量名：`JAVA_HOME`
   - 变量值：JDK安装路径（例如：`C:\Program Files\Java\jdk-11.0.16`）

3. 编辑「Path」变量，添加以下内容：
   - `%JAVA_HOME%\bin`

4. 可选：创建CLASSPATH环境变量
   - 变量名：`CLASSPATH`
   - 变量值：`.;%JAVA_HOME%\lib\dt.jar;%JAVA_HOME%\lib\tools.jar`

## 步骤3：验证安装

打开命令提示符，输入以下命令验证Java安装：

```bash
java -version
javac -version
```

如果显示JDK版本信息，则表示安装成功。

## 推荐的Java开发工具

- **IntelliJ IDEA**：功能强大的Java IDE
- **Eclipse**：开源的Java IDE
- **VS Code**：轻量级编辑器，配合Java插件使用

## Maven/Gradle配置

### Maven配置

1. 下载Maven（https://maven.apache.org/download.cgi）
2. 解压到指定目录
3. 配置M2_HOME环境变量
4. 在Path中添加`%M2_HOME%\bin`

### Gradle配置

1. 下载Gradle（https://gradle.org/releases/）
2. 解压到指定目录
3. 配置GRADLE_HOME环境变量
4. 在Path中添加`%GRADLE_HOME%\bin`

## 常用Java框架

- Spring Boot：快速开发Java应用
- Spring MVC：Web应用开发
- Hibernate/MyBatis：ORM框架，用于数据库操作
- Spring Security：安全框架

## 示例项目

后续可以在该目录下创建示例Java项目，演示如何连接MySQL数据库。