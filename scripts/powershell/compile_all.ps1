# 编译所有Java源文件的PowerShell脚本

Write-Host "开始编译所有Java源文件..."

# 创建输出目录
if (-not (Test-Path "target\classes")) {
    New-Item -ItemType Directory -Force -Path "target\classes"
}

# 获取所有Java文件
$javaFiles = Get-ChildItem -Path "src\main\java" -Recurse -Filter "*.java" | Select-Object -ExpandProperty FullName

# 构建类路径
$libPath = "lib\*"

# 编译所有文件
Write-Host "开始编译 $($javaFiles.Count) 个Java文件..."
javac -d "target\classes" -cp "$libPath" $javaFiles

if ($LASTEXITCODE -eq 0) {
    Write-Host "编译成功！"
    # 检查生成的类文件数量
    $classFiles = Get-ChildItem -Path "target\classes" -Recurse -Filter "*.class" | Measure-Object
    Write-Host "生成了 $($classFiles.Count) 个类文件"
} else {
    Write-Host "编译失败，请检查错误信息"
    exit 1
}