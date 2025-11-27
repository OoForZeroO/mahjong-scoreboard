# Simple Maven Installation Script

$MAVEN_VERSION = "3.9.6"
$MAVEN_HOME = "d:\YH\apache-maven-$MAVEN_VERSION"

Clear-Host
Write-Host "Installing Maven $MAVEN_VERSION..."

# Create temp directory if not exists
if (-not (Test-Path -Path "d:\YH\temp")) {
    New-Item -ItemType Directory -Path "d:\YH\temp" | Out-Null
}

# Download Maven
Write-Host "Downloading Maven..."
try {
    Invoke-WebRequest -Uri "https://archive.apache.org/dist/maven/maven-3/$MAVEN_VERSION/binaries/apache-maven-$MAVEN_VERSION-bin.zip" -OutFile "d:\YH\temp\maven.zip" -UseBasicParsing
    Write-Host "Download completed."
} catch {
    Write-Host "Download failed: $_"
    exit 1
}

# Extract Maven
Write-Host "Extracting Maven..."
try {
    # Remove existing directory if it exists
    if (Test-Path -Path $MAVEN_HOME) {
        Remove-Item -Path $MAVEN_HOME -Recurse -Force
    }
    
    Expand-Archive -Path "d:\YH\temp\maven.zip" -DestinationPath "d:\YH"
    Write-Host "Extraction completed."
} catch {
    Write-Host "Extraction failed: $_"
    exit 1
}

# Clean up downloaded file
Remove-Item -Path "d:\YH\temp\maven.zip"

# Create simple start script
$startScript = @"
@echo off
set MAVEN_HOME=$MAVEN_HOME
set PATH=%MAVEN_HOME%\bin;%PATH%
echo Maven environment set up
mvn -version
"@

try {
    $startScript | Out-File -FilePath "d:\YH\scripts\start_maven.bat" -Encoding ASCII
    Write-Host "Start script created."
} catch {
    Write-Host "Failed to create start script: $_"
}

Write-Host ""
Write-Host "Maven installation completed successfully!"
Write-Host "Installation path: $MAVEN_HOME"
Write-Host "Start script: d:\YH\scripts\start_maven.bat"
Write-Host ""
Write-Host "To use Maven, run start_maven.bat"