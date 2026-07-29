@REM Apache Maven Wrapper script for Windows
@REM This allows the project to be built without Maven installed
@echo off
setlocal

if "%JAVA_HOME%"=="" (
    echo Error: JAVA_HOME is not set. Please set JAVA_HOME to your Java installation.
    exit /b 1
)

set MAVEN_VERSION=3.9.6
set MAVEN_URL=https://archive.apache.org/dist/maven/maven-3/%MAVEN_VERSION%/binaries/apache-maven-%MAVEN_VERSION%-bin.zip
set MAVEN_HOME=%USERPROFILE%\.m2\wrapper\maven-%MAVEN_VERSION%

if exist "%MAVEN_HOME%\bin\mvn.cmd" (
    call "%MAVEN_HOME%\bin\mvn.cmd" %*
) else (
    echo Downloading Maven %MAVEN_VERSION%...
    if not exist "%USERPROFILE%\.m2\wrapper" mkdir "%USERPROFILE%\.m2\wrapper"
    
    powershell -Command "[Net.ServicePointManager]::SecurityProtocol = [Net.SecurityProtocolType]::Tls12; (New-Object Net.WebClient).DownloadFile('%MAVEN_URL%', '%USERPROFILE%\.m2\wrapper\maven.zip')"
    
    echo Extracting Maven...
    powershell -Command "Expand-Archive -Path '%USERPROFILE%\.m2\wrapper\maven.zip' -DestinationPath '%USERPROFILE%\.m2\wrapper' -Force"
    
    echo Cleaning up...
    del "%USERPROFILE%\.m2\wrapper\maven.zip"
    
    echo Running Maven...
    call "%MAVEN_HOME%\bin\mvn.cmd" %*
)
endlocal
