# 简单的Maven安装脚本

$MAVEN_VERSION = "3.9.6"
$MAVEN_HOME = "d:\YH\apache-maven-$MAVEN_VERSION"

Clear-Host
Write-Host "正在安装Maven $MAVEN_VERSION..."

# 下载Maven
Write-Host "下载Maven..."
Invoke-WebRequest -Uri "https://archive.apache.org/dist/maven/maven-3/$MAVEN_VERSION/binaries/apache-maven-$MAVEN_VERSION-bin.zip" -OutFile "d:\YH\maven.zip" -UseBasicParsing

# 解压Maven
Write-Host "解压Maven..."
Expand-Archive -Path "d:\YH\maven.zip" -DestinationPath "d:\YH"

# 清理下载文件
Remove-Item -Path "d:\YH\maven.zip"

# 创建简单的启动脚本
$startScript = @"
@echo off
set MAVEN_HOME=$MAVEN_HOME
set PATH=%MAVEN_HOME%\bin;%PATH%
mvn -version
"@

$startScript | Out-File -FilePath "d:\YH\scripts\start_maven.bat" -Encoding ASCII

Write-Host ""
Write-Host "✅ Maven安装完成！"
Write-Host "安装路径: $MAVEN_HOME"
Write-Host "启动脚本: d:\YH\scripts\start_maven.bat"
Write-Host ""
Write-Host "请运行 start_maven.bat 来启动Maven环境"