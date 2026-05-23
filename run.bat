@echo off
setlocal enabledelayedexpansion
chcp 65001 >nul 2>&1
title Odyssey — Running

set "ROOT=%~dp0"

REM Load environment saved by setup.bat
if exist "%ROOT%.env.bat" (
    call "%ROOT%.env.bat"
) else (
    echo  [!!] Environment not configured. Please run setup.bat first.
    pause
    exit /b 1
)

REM Verify JAR exists
if not exist "%ROOT%target\odyssey-1.0.0.jar" (
    echo  [!!] JAR not found. Rebuilding...
    set "PATH=%ODYSSEY_JAVA_HOME%\bin;%PATH%"
    call "%ODYSSEY_MVN%" clean package -DskipTests -q
    if %errorlevel% neq 0 (
        echo  [ERROR] Build failed. Run setup.bat again.
        pause
        exit /b 1
    )
)

cls
echo.
echo  ================================================================
echo     _____  _____  _____  _____ _____ _____ __  __
echo    ^|  _  ^|^|  _  ^|^|  _  ^|^|   __^|  ___^|_   _^|  \/  ^|
echo    ^| ^|_^| ^|^| ^| ^| ^|^| ^|_^| ^|^|__   ^|^|___  ^| ^| ^| ^|\/^| ^|
echo    ^|_____^|^|_^| ^|_^|^|___  ^||_____^||_____^| ^|_^| ^|_^|  ^|_^|
echo                  ^|_____^|
echo.
echo    Your Life as an RPG  -  Starting on port 8080
echo  ================================================================
echo.
echo  Open your browser at: http://localhost:8080
echo  Press Ctrl+C to stop the server.
echo.

REM Open browser after a short delay (in background)
start "" cmd /c "ping -n 5 127.0.0.1 >nul && start http://localhost:8080"

REM Start the app
"%ODYSSEY_JAVA%" -jar "%ROOT%target\odyssey-1.0.0.jar" --spring.profiles.active=dev

echo.
echo  Server stopped.
pause
