@echo off
REM Maven setup wrapper for Butterfly on Windows
REM This batch file sets up Java and Maven paths, then runs Maven

setlocal enabledelayedexpansion

REM Set Java home
set JAVA_HOME=C:\Program Files\Java\jdk-24
set PATH=C:\Users\%USERNAME%\.m2\apache-maven-3.9.6\bin;%PATH%

REM Run Maven with all arguments passed through
mvn %*

endlocal
