@echo off
setlocal enabledelayedexpansion
title Odyssey

set "ROOT=%~dp0"

REM Load config saved by setup.bat
if not exist "%ROOT%.env.bat" (
    echo.
    echo  [ERROR] Not configured. Please run setup.bat first!
    echo.
    pause
    exit /b 1
)
call "%ROOT%.env.bat"

REM Locate the built JAR (any version)
set "ODYSSEY_JAR="
for %%f in ("%ROOT%target\odyssey-*.jar") do (
    if not defined ODYSSEY_JAR set "ODYSSEY_JAR=%%~ff"
)

REM Rebuild if JAR missing
if not defined ODYSSEY_JAR (
    echo  [..] JAR not found. Building...
    set "JAVA_HOME=%ODYSSEY_JAVA_HOME%"
    set "PATH=%ODYSSEY_JAVA_HOME%\bin;%PATH%"
    call "%ODYSSEY_MVN%" clean package -DskipTests -q
    if !errorlevel! neq 0 (
        echo  [ERROR] Build failed. Run setup.bat again.
        pause
        exit /b 1
    )
    for %%f in ("%ROOT%target\odyssey-*.jar") do (
        if not defined ODYSSEY_JAR set "ODYSSEY_JAR=%%~ff"
    )
    if not defined ODYSSEY_JAR (
        echo  [ERROR] Build succeeded but no odyssey-*.jar found in target\.
        pause
        exit /b 1
    )
)

cls
echo.
echo  ================================================================
echo    ODYSSEY - RPG Life Quest Platform
echo    Starting on http://localhost:8080
echo    Press Ctrl+C to stop the server
echo  ================================================================
echo.

REM Open browser after 6 seconds
start "" cmd /c "ping -n 7 127.0.0.1 >nul 2>&1 && start http://localhost:8080"

REM Run the app
"%ODYSSEY_JAVA%" -jar "%ODYSSEY_JAR%" --spring.profiles.active=dev

echo.
echo  Server stopped. Press any key to close.
pause
