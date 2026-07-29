# Setup Maven for Butterfly project on Windows
# Usage: powershell -ExecutionPolicy Bypass -File setup-maven.ps1

Write-Host ""
Write-Host "===================================================" -ForegroundColor Cyan
Write-Host "  Butterfly Maven Setup for Windows" -ForegroundColor Cyan
Write-Host "===================================================" -ForegroundColor Cyan
Write-Host ""

# Check Java
try {
    $javaVersion = java -version 2>&1 | Select-String "version" | Select-Object -First 1
    Write-Host "[OK] Found Java: $javaVersion" -ForegroundColor Green
} catch {
    Write-Host "[ERROR] Java not found. Please install Java 8 or later." -ForegroundColor Red
    exit 1
}

# Find JAVA_HOME
$javaBin = (Get-Command java).Source
$javaDir = Split-Path $javaBin
$javaHome = Split-Path $javaDir
$env:JAVA_HOME = $javaHome
Write-Host "[OK] Set JAVA_HOME = $javaHome" -ForegroundColor Green

# Maven setup
$mavenVersion = "3.9.6"
$mavenHome = "$env:USERPROFILE\.m2\apache-maven-$mavenVersion"

if (Test-Path "$mavenHome\bin\mvn.cmd") {
    Write-Host "[OK] Maven already installed at $mavenHome" -ForegroundColor Green
} else {
    Write-Host "[*] Downloading Maven $mavenVersion..." -ForegroundColor Yellow
    
    # Ensure .m2 directory exists
    if (-not (Test-Path "$env:USERPROFILE\.m2")) {
        New-Item -ItemType Directory -Path "$env:USERPROFILE\.m2" -Force | Out-Null
    }
    
    # Download Maven
    $mavenZip = "$env:USERPROFILE\.m2\maven.zip"
    $mavenUrl = "https://archive.apache.org/dist/maven/maven-3/$mavenVersion/binaries/apache-maven-$mavenVersion-bin.zip"
    
    [Net.ServicePointManager]::SecurityProtocol = [Net.SecurityProtocolType]::Tls12
    (New-Object Net.WebClient).DownloadFile($mavenUrl, $mavenZip)
    
    Write-Host "[*] Extracting Maven..." -ForegroundColor Yellow
    Expand-Archive -Path $mavenZip -DestinationPath "$env:USERPROFILE\.m2" -Force
    Remove-Item $mavenZip -Force
    
    Write-Host "[OK] Maven $mavenVersion installed" -ForegroundColor Green
}

# Add to PATH
$mavenBin = "$mavenHome\bin"
if ($env:PATH -notlike "*$mavenBin*") {
    $env:PATH = "$mavenBin;$env:PATH"
    Write-Host "[OK] Added Maven to PATH" -ForegroundColor Green
}

# Test Maven
Write-Host ""
Write-Host "===================================================" -ForegroundColor Cyan
Write-Host "  Testing Maven Installation" -ForegroundColor Cyan
Write-Host "===================================================" -ForegroundColor Cyan
Write-Host ""

mvn -version

Write-Host ""
Write-Host "===================================================" -ForegroundColor Cyan
Write-Host "[SUCCESS] Maven setup complete!" -ForegroundColor Green
Write-Host "===================================================" -ForegroundColor Cyan
Write-Host ""
Write-Host "You can now use Maven commands:" -ForegroundColor White
Write-Host "  mvn clean package -DskipTests" -ForegroundColor Cyan
Write-Host "  mvn compile" -ForegroundColor Cyan
Write-Host "  mvn test" -ForegroundColor Cyan
Write-Host ""
Write-Host "To make Maven permanent, add to your PowerShell profile:" -ForegroundColor Yellow
Write-Host '  `$env:JAVA_HOME = "' + $javaHome + '"' -ForegroundColor White
Write-Host '  `$env:PATH = "' + $mavenBin + ';" + `$env:PATH' -ForegroundColor White
Write-Host ""
Write-Host "Profile location:" -ForegroundColor Gray
Write-Host "  $PROFILE" -ForegroundColor Gray
Write-Host ""
