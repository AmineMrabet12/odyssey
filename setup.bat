@echo off
setlocal enabledelayedexpansion
chcp 65001 >nul 2>&1
title Odyssey — Setup

cls
echo.
echo  ================================================================
echo     _____  _____  _____  _____ _____ _____ __  __
echo    ^|  _  ^|^|  _  ^|^|  _  ^|^|   __^|  ___^|_   _^|  \/  ^|
echo    ^| ^|_^| ^|^| ^| ^| ^|^| ^|_^| ^|^|__   ^|^|___  ^| ^| ^| ^|\/^| ^|
echo    ^|_____^|^|_^| ^|_^|^|___  ^||_____^||_____^| ^|_^| ^|_^|  ^|_^|
echo                  ^|_____^|
echo.
echo    RPG Life Quest Platform — Windows Setup
echo  ================================================================
echo.

set "ROOT=%~dp0"
set "TOOLS=%ROOT%.tools"
set "JAVA_DIR=%TOOLS%\java"
set "MVN_DIR=%TOOLS%\maven"
set "LOG=%ROOT%setup.log"

if not exist "%TOOLS%" mkdir "%TOOLS%"

REM ================================================================
REM  STEP 1 — Detect or install Java 21+
REM ================================================================
echo  [1/4] Checking Java...

set "JAVA_EXE="
set "JAVA_OK=0"

REM Check system Java first
where java >nul 2>&1
if %errorlevel% == 0 (
    for /f "tokens=*" %%v in ('java -version 2^>^&1') do (
        set "JVER=%%v"
        goto :check_jver
    )
    :check_jver
    echo !JVER! | findstr /r "version \"2[1-9]\|version \"[3-9][0-9]" >nul 2>&1
    if !errorlevel! == 0 (
        set "JAVA_EXE=java"
        set "JAVA_OK=1"
        echo  [OK] Java 21+ found in system PATH
    )
)

REM Check common install locations
if "%JAVA_OK%"=="0" (
    for %%d in (
        "C:\Program Files\Amazon Corretto\jdk21"
        "C:\Program Files\Eclipse Adoptium\jdk-21"
        "C:\Program Files\Microsoft\jdk-21"
        "C:\Program Files\Java\jdk-21"
        "%JAVA_DIR%"
    ) do (
        if exist "%%~d\bin\java.exe" (
            set "JAVA_EXE=%%~d\bin\java.exe"
            set "JAVA_OK=1"
            echo  [OK] Java 21 found at %%~d
            goto :java_found
        )
    )
    REM Check expanded glob paths
    for /d %%d in ("C:\Program Files\Amazon Corretto\jdk21*") do (
        if exist "%%d\bin\java.exe" (
            set "JAVA_EXE=%%d\bin\java.exe"
            set "JAVA_OK=1"
            echo  [OK] Java 21 found at %%d
            goto :java_found
        )
    )
    for /d %%d in ("C:\Program Files\Eclipse Adoptium\jdk-21*") do (
        if exist "%%d\bin\java.exe" (
            set "JAVA_EXE=%%d\bin\java.exe"
            set "JAVA_OK=1"
            echo  [OK] Java 21 found at %%d
            goto :java_found
        )
    )
)

:java_found
if "%JAVA_OK%"=="0" (
    echo  [!!] Java 21 not found. Downloading Amazon Corretto 21...
    echo       This may take a few minutes depending on your connection.
    echo.
    set "JDK_ZIP=%TOOLS%\corretto21.zip"
    set "JDK_URL=https://corretto.aws/downloads/latest/amazon-corretto-21-x64-windows-jdk.zip"

    powershell -NoProfile -Command ^
        "[Net.ServicePointManager]::SecurityProtocol = [Net.SecurityProtocolType]::Tls12; ^
        $ProgressPreference = 'SilentlyContinue'; ^
        Invoke-WebRequest -Uri '%JDK_URL%' -OutFile '%JDK_ZIP%'" >nul 2>&1

    if not exist "%JDK_ZIP%" (
        echo.
        echo  [ERROR] Failed to download Java. Check your internet connection.
        echo          Download manually from: https://corretto.aws/downloads/latest/amazon-corretto-21-x64-windows-jdk.msi
        pause
        exit /b 1
    )

    echo  [..] Extracting Java...
    powershell -NoProfile -Command ^
        "$ProgressPreference = 'SilentlyContinue'; ^
        Expand-Archive -Path '%JDK_ZIP%' -DestinationPath '%TOOLS%\java_tmp' -Force; ^
        $inner = (Get-ChildItem '%TOOLS%\java_tmp' -Directory ^| Select-Object -First 1).FullName; ^
        if (Test-Path '%JAVA_DIR%') { Remove-Item '%JAVA_DIR%' -Recurse -Force }; ^
        Move-Item $inner '%JAVA_DIR%'; ^
        Remove-Item '%TOOLS%\java_tmp' -Recurse -Force; ^
        Remove-Item '%JDK_ZIP%' -Force" >nul 2>&1

    if not exist "%JAVA_DIR%\bin\java.exe" (
        echo  [ERROR] Java extraction failed.
        pause
        exit /b 1
    )

    set "JAVA_EXE=%JAVA_DIR%\bin\java.exe"
    set "JAVA_OK=1"
    echo  [OK] Java 21 installed to .tools\java\
)

REM ================================================================
REM  STEP 2 — Detect or install Maven
REM ================================================================
echo.
echo  [2/4] Checking Maven...

set "MVN_EXE="
set "MVN_OK=0"

where mvn >nul 2>&1
if %errorlevel% == 0 (
    set "MVN_EXE=mvn"
    set "MVN_OK=1"
    echo  [OK] Maven found in system PATH
)

if "%MVN_OK%"=="0" (
    if exist "%MVN_DIR%\bin\mvn.cmd" (
        set "MVN_EXE=%MVN_DIR%\bin\mvn.cmd"
        set "MVN_OK=1"
        echo  [OK] Maven found at .tools\maven\
    )
)

if "%MVN_OK%"=="0" (
    echo  [!!] Maven not found. Downloading Apache Maven 3.9.6...
    set "MVN_ZIP=%TOOLS%\maven.zip"
    set "MVN_URL=https://dlcdn.apache.org/maven/maven-3/3.9.6/binaries/apache-maven-3.9.6-bin.zip"

    powershell -NoProfile -Command ^
        "[Net.ServicePointManager]::SecurityProtocol = [Net.SecurityProtocolType]::Tls12; ^
        $ProgressPreference = 'SilentlyContinue'; ^
        Invoke-WebRequest -Uri '%MVN_URL%' -OutFile '%MVN_ZIP%'" >nul 2>&1

    if not exist "%MVN_ZIP%" (
        REM Try alternate mirror
        set "MVN_URL2=https://archive.apache.org/dist/maven/maven-3/3.9.6/binaries/apache-maven-3.9.6-bin.zip"
        powershell -NoProfile -Command ^
            "[Net.ServicePointManager]::SecurityProtocol = [Net.SecurityProtocolType]::Tls12; ^
            $ProgressPreference = 'SilentlyContinue'; ^
            Invoke-WebRequest -Uri '%MVN_URL2%' -OutFile '%MVN_ZIP%'" >nul 2>&1
    )

    if not exist "%MVN_ZIP%" (
        echo  [ERROR] Failed to download Maven. Check your internet connection.
        pause
        exit /b 1
    )

    echo  [..] Extracting Maven...
    powershell -NoProfile -Command ^
        "$ProgressPreference = 'SilentlyContinue'; ^
        Expand-Archive -Path '%MVN_ZIP%' -DestinationPath '%TOOLS%\maven_tmp' -Force; ^
        $inner = (Get-ChildItem '%TOOLS%\maven_tmp' -Directory ^| Select-Object -First 1).FullName; ^
        if (Test-Path '%MVN_DIR%') { Remove-Item '%MVN_DIR%' -Recurse -Force }; ^
        Move-Item $inner '%MVN_DIR%'; ^
        Remove-Item '%TOOLS%\maven_tmp' -Recurse -Force; ^
        Remove-Item '%MVN_ZIP%' -Force" >nul 2>&1

    if not exist "%MVN_DIR%\bin\mvn.cmd" (
        echo  [ERROR] Maven extraction failed.
        pause
        exit /b 1
    )

    set "MVN_EXE=%MVN_DIR%\bin\mvn.cmd"
    set "MVN_OK=1"
    echo  [OK] Maven 3.9.6 installed to .tools\maven\
)

REM ================================================================
REM  STEP 3 — Set JAVA_HOME and build
REM ================================================================
echo.
echo  [3/4] Building Odyssey...

REM Resolve JAVA_HOME from the java executable path
for %%f in ("%JAVA_EXE%") do set "JAVA_BIN_DIR=%%~dpf"
set "JAVA_BIN_DIR=%JAVA_BIN_DIR:~0,-1%"
for %%d in ("%JAVA_BIN_DIR%") do set "JAVA_HOME_LOCAL=%%~dpd"
set "JAVA_HOME_LOCAL=%JAVA_HOME_LOCAL:~0,-1%"

set "JAVA_HOME=%JAVA_HOME_LOCAL%"
set "PATH=%JAVA_HOME%\bin;%PATH%"
if defined MVN_DIR set "PATH=%MVN_DIR%\bin;%PATH%"

cd /d "%ROOT%"
call "%MVN_EXE%" clean package -DskipTests -q 2>"%LOG%"

if %errorlevel% neq 0 (
    echo.
    echo  [ERROR] Build failed! See setup.log for details:
    echo.
    type "%LOG%" | findstr /i "ERROR"
    echo.
    pause
    exit /b 1
)

echo  [OK] Build successful!

REM ================================================================
REM  STEP 4 — Save environment config for run.bat
REM ================================================================
echo.
echo  [4/4] Saving configuration...

echo @echo off> "%ROOT%\.env.bat"
echo set "ODYSSEY_JAVA=%JAVA_EXE%">> "%ROOT%\.env.bat"
echo set "ODYSSEY_JAVA_HOME=%JAVA_HOME_LOCAL%">> "%ROOT%\.env.bat"
echo set "ODYSSEY_MVN=%MVN_EXE%">> "%ROOT%\.env.bat"

echo  [OK] Configuration saved.

echo.
echo  ================================================================
echo.
echo    Setup complete! Odyssey is ready to launch.
echo.
echo    To start the app, double-click:  run.bat
echo    Then open your browser at:       http://localhost:8080
echo.
echo  ================================================================
echo.
pause
