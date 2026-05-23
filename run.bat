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

REM Rebuild if JAR missing
if not exist "%ROOT%target\odyssey-1.0.0.jar" (
    echo  [..] JAR not found. Building...
    set "JAVA_HOME=%ODYSSEY_JAVA_HOME%"
    set "PATH=%ODYSSEY_JAVA_HOME%\bin;%PATH%"
    call "%ODYSSEY_MVN%" clean package -DskipTests -q
    if !errorlevel! neq 0 (
        echo  [ERROR] Build failed. Run setup.bat again.
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
start "" cmd /c "ping -n 7 127.0.0.1 >/dev/null 2>&1 && start http://localhost:8080"

REM Run the app
"%ODYSSEY_JAVA%" -jar "%ROOT%target\odyssey-1.0.0.jar" --spring.profiles.active=dev

echo.
echo  Server stopped. Press any key to close.
pause
