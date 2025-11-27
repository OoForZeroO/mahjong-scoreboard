# Maven安装PowerShell脚本

$MAVEN_VERSION = "3.9.6"
$MAVEN_ZIP = "maven-${MAVEN_VERSION}-bin.zip"
$MAVEN_URL = "https://archive.apache.org/dist/maven/maven-3/${MAVEN_VERSION}/binaries/${MAVEN_ZIP}"
$MAVEN_HOME = "d:\YH\apache-maven-${MAVEN_VERSION}"
$DOWNLOAD_DIR = "d:\YH\temp"
$MAVEN_LOCAL_REPO = "${env:USERPROFILE}\.m2\repository"

# 清屏
Clear-Host

Write-Host "=================================================" -ForegroundColor Cyan
Write-Host "            Maven安装脚本                    " -ForegroundColor Cyan
Write-Host "=================================================" -ForegroundColor Cyan
Write-Host "  "
Write-Host "  正在安装Maven $MAVEN_VERSION" -ForegroundColor Green
Write-Host "  目标路径: $MAVEN_HOME" -ForegroundColor Green
Write-Host "  "

# 检查Java是否已安装
try {
    java -version | Out-Null
    Write-Host "✓ Java环境已检测到" -ForegroundColor Green
} catch {
    Write-Host "✗ 错误: 未检测到Java环境，请先安装JDK！" -ForegroundColor Red
    Read-Host "按Enter键退出..."
    exit 1
}

# 创建下载目录
if (-not (Test-Path -Path $DOWNLOAD_DIR)) {
    New-Item -ItemType Directory -Path $DOWNLOAD_DIR | Out-Null
    Write-Host "✓ 创建下载目录: $DOWNLOAD_DIR" -ForegroundColor Green
}

# 下载Maven
Write-Host "正在下载Maven..." -ForegroundColor Yellow
try {
    Invoke-WebRequest -Uri $MAVEN_URL -OutFile "${DOWNLOAD_DIR}\${MAVEN_ZIP}" -UseBasicParsing
    Write-Host "✓ Maven下载成功" -ForegroundColor Green
} catch {
    Write-Host "✗ 错误: 下载Maven失败！" -ForegroundColor Red
    Write-Host "错误详情: $_" -ForegroundColor Red
    Read-Host "按Enter键退出..."
    exit 1
}

# 清理旧的Maven目录
if (Test-Path -Path $MAVEN_HOME) {
    Write-Host "清理旧的Maven目录..." -ForegroundColor Yellow
    Remove-Item -Path $MAVEN_HOME -Recurse -Force
}

# 解压Maven
Write-Host "正在解压Maven..." -ForegroundColor Yellow
try {
    Expand-Archive -Path "${DOWNLOAD_DIR}\${MAVEN_ZIP}" -DestinationPath "d:\YH"
    Write-Host "✓ Maven解压成功" -ForegroundColor Green
} catch {
    Write-Host "✗ 错误: 解压Maven失败！" -ForegroundColor Red
    Write-Host "错误详情: $_" -ForegroundColor Red
    Read-Host "按Enter键退出..."
    exit 1
}

# 清理下载的文件
Remove-Item -Path "${DOWNLOAD_DIR}\${MAVEN_ZIP}"
Write-Host "✓ 清理临时文件" -ForegroundColor Green

# 创建本地仓库目录
if (-not (Test-Path -Path $MAVEN_LOCAL_REPO)) {
    New-Item -ItemType Directory -Path "${env:USERPROFILE}\.m2" -Force | Out-Null
    New-Item -ItemType Directory -Path $MAVEN_LOCAL_REPO -Force | Out-Null
    Write-Host "✓ 创建本地仓库目录: $MAVEN_LOCAL_REPO" -ForegroundColor Green
}

# 创建settings.xml文件
$MAVEN_SETTINGS = "${env:USERPROFILE}\.m2\settings.xml"
if (-not (Test-Path -Path $MAVEN_SETTINGS)) {
    Write-Host "创建默认的settings.xml文件..." -ForegroundColor Yellow
    $settingsContent = @"
<?xml version="1.0" encoding="UTF-8"?>
<settings xmlns="http://maven.apache.org/SETTINGS/1.0.0"
          xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
          xsi:schemaLocation="http://maven.apache.org/SETTINGS/1.0.0 http://maven.apache.org/xsd/settings-1.0.0.xsd">
  <localRepository>$MAVEN_LOCAL_REPO</localRepository>
  <interactiveMode>true</interactiveMode>
  <offlineMode>false</offlineMode>
</settings>
"@
    $settingsContent | Out-File -FilePath $MAVEN_SETTINGS -Encoding UTF8
    Write-Host "✓ 创建settings.xml成功" -ForegroundColor Green
}

# 创建批处理文件以便快速启动Maven项目
Write-Host "创建Maven启动脚本..." -ForegroundColor Yellow
$mavenRunnerContent = @"
@echo off

REM Maven快速启动脚本
set MAVEN_HOME=$MAVEN_HOME
set PATH=%MAVEN_HOME%\bin;%PATH%

echo Maven环境已配置
set /p project_name="请输入项目名称: "
set project_dir=d:\YH\java\%project_name%

if exist "%project_dir%" (
    echo 警告: 项目目录已存在，请使用其他名称
    pause
    exit
)

mkdir "%project_dir%"
cd "%project_dir%"

REM 创建Maven项目
echo 创建Maven项目 %project_name%...
mvn archetype:generate -DgroupId=com.example -DartifactId=%project_name% -DarchetypeArtifactId=maven-archetype-quickstart -DinteractiveMode=false

echo 项目创建完成！
echo 项目路径: %project_dir%
cd /d "%project_dir%"

echo 当前目录: %cd%
echo.  
echo 使用以下命令运行Maven项目:
echo 1. cd %project_dir%
echo 2. mvn compile
echo 3. mvn package
echo 4. mvn exec:java -Dexec.mainClass="com.example.App"

echo.  
echo 是否现在构建项目? (y/n)
set /p build_choice=
if /i "%build_choice%"=="y" (
    mvn clean compile package
    echo 构建完成！
)

pause
"@
$mavenRunnerContent | Out-File -FilePath "d:\YH\scripts\maven_project_creator.bat" -Encoding UTF8
Write-Host "✓ 创建Maven项目创建脚本成功" -ForegroundColor Green

Write-Host "  "
Write-Host "=================================================" -ForegroundColor Cyan
Write-Host "            Maven安装完成！                    " -ForegroundColor Cyan
Write-Host "  "
Write-Host "Maven版本: $MAVEN_VERSION" -ForegroundColor Green
Write-Host "Maven安装路径: $MAVEN_HOME" -ForegroundColor Green
Write-Host "Maven本地仓库: $MAVEN_LOCAL_REPO" -ForegroundColor Green
Write-Host "Maven项目创建脚本: d:\YH\scripts\maven_project_creator.bat" -ForegroundColor Green
Write-Host "  "
Write-Host "使用方法:" -ForegroundColor Yellow
Write-Host "1. 手动设置环境变量:" -ForegroundColor White
Write-Host "   - MAVEN_HOME = $MAVEN_HOME" -ForegroundColor White
Write-Host "   - 将 %MAVEN_HOME%\bin 添加到系统PATH" -ForegroundColor White
Write-Host "2. 或直接使用提供的批处理脚本" -ForegroundColor White
Write-Host "=================================================" -ForegroundColor Cyan
Write-Host "  "
Write-Host "按Enter键退出..." -ForegroundColor Gray
Read-Host