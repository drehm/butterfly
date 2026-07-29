@echo off
REM Setup Maven for Butterfly project
REM Run this once to set up Maven for development

setlocal enabledelayedexpansion

echo.
echo ==================================================
echo  Butterfly Maven Setup for Windows
echo ==================================================
echo.

REM Check Java
for /f "tokens=3" %%i in ('java -version 2^>^&1 ^| find "version"') do set JAVA_VER=%%i
if "%JAVA_VER%"=="" (
    echo Error: Java is not installed or not in PATH
    echo Please install Java 8 or later
    exit /b 1
)
echo [OK] Found Java: %JAVA_VER%

REM Set JAVA_HOME
for /f "delims=" %%i in ('where java') do set JAVA_BIN=%%i
for %%I in ("%JAVA_BIN%") do set JAVA_BIN_DIR=%%~dpI
for %%I in ("%JAVA_BIN_DIR%..") do set JAVA_HOME=%%~fI
echo [OK] Set JAVA_HOME=%JAVA_HOME%

REM Download Maven if not exists
set MAVEN_HOME=%USERPROFILE%\.m2\apache-maven-3.9.6
if exist "%MAVEN_HOME%\bin\mvn.cmd" (
    echo [OK] Maven already installed at %MAVEN_HOME%
) else (
    echo [*] Downloading Maven 3.9.6...
    if not exist "%USERPROFILE%\.m2" mkdir "%USERPROFILE%\.m2"
    
    powershell -Command ^
        "$ProgressPreference = 'SilentlyContinue'; ^
        [Net.ServicePointManager]::SecurityProtocol = [Net.SecurityProtocolType]::Tls12; ^
        (New-Object Net.WebClient).DownloadFile('https://archive.apache.org/dist/maven/maven-3/3.9.6/binaries/apache-maven-3.9.6-bin.zip', '%USERPROFILE%\.m2\maven.zip')"
    
    echo [*] Extracting Maven...
    powershell -Command "Expand-Archive -Path '%USERPROFILE%\.m2\maven.zip' -DestinationPath '%USERPROFILE%\.m2' -Force; Remove-Item '%USERPROFILE%\.m2\maven.zip'"
    
    echo [OK] Maven installed
)

REM Update PATH
set PATH=%MAVEN_HOME%\bin;%PATH%

echo.
echo ==================================================
echo  Setup complete! Running: mvn -version
echo ==================================================
echo.

call mvn -version

echo.
echo [OK] You can now use 'mvn' and 'mvnw.cmd' commands
echo.
echo To make Maven available in future PowerShell sessions, add this to your PowerShell profile:
echo   $env:JAVA_HOME = '%JAVA_HOME%'
echo   $env:PATH = '%MAVEN_HOME%\bin;' + $env:PATH
echo.

endlocal
