@echo off
REM Butterfly Launcher Wrapper
REM Handles update installation before launching the app

setlocal enabledelayedexpansion

REM Get the directory where this script is located
set "SCRIPT_DIR=%~dp0"
set "APP_JAR=%SCRIPT_DIR%butterfly-app.jar"
set "PENDING_JAR=%SCRIPT_DIR%butterfly-app.jar.pending"
set "BACKUP_JAR=%SCRIPT_DIR%butterfly-app.jar.backup"

REM Check if there's a pending update to install
if exist "%PENDING_JAR%" (
    echo [LAUNCHER] Pending update found, installing...
    
    REM Backup current JAR
    if exist "%APP_JAR%" (
        echo [LAUNCHER] Backing up current JAR...
        if exist "%BACKUP_JAR%" del "%BACKUP_JAR%"
        move "%APP_JAR%" "%BACKUP_JAR%" >nul 2>&1
        if errorlevel 1 (
            echo [LAUNCHER] ERROR: Could not backup JAR
            exit /b 1
        )
    )
    
    REM Install new JAR
    echo [LAUNCHER] Installing new version...
    move "%PENDING_JAR%" "%APP_JAR%" >nul 2>&1
    if errorlevel 1 (
        echo [LAUNCHER] ERROR: Could not install new JAR, restoring backup
        if exist "%BACKUP_JAR%" move "%BACKUP_JAR%" "%APP_JAR%" >nul 2>&1
        exit /b 1
    )
    
    echo [LAUNCHER] Update installed successfully
)

REM Launch the application
if not exist "%APP_JAR%" (
    echo [LAUNCHER] ERROR: Application JAR not found at %APP_JAR%
    exit /b 1
)

echo [LAUNCHER] Launching Butterfly...
java -jar "%APP_JAR%" %*
