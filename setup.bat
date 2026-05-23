@echo off
setlocal enabledelayedexpansion
title Odyssey Setup

cls
echo.
echo  ================================================================
echo    ODYSSEY - RPG Life Quest Platform - Windows Setup
echo  ================================================================
echo.

set "ROOT=%~dp0"
set "TOOLS=%ROOT%.tools"
set "JAVA_DIR=%TOOLS%\java"
set "MVN_DIR=%TOOLS%\maven"

if not exist "%TOOLS%" mkdir "%TOOLS%"

REM ================================================================
REM  STEP 1 - Detect or install Java 21+
REM ================================================================
echo  [1/4] Checking Java 21...

set "JAVA_EXE="
set "JAVA_HOME_SET="

REM Check bundled Java first (.tools\java)
if exist "%JAVA_DIR%\bin\java.exe" (
    set "JAVA_EXE=%JAVA_DIR%\bin\java.exe"
    set "JAVA_HOME_SET=%JAVA_DIR%"
    echo  [OK] Java found in .tools\java\
    goto :check_maven
)

REM Check Amazon Corretto 21
for /d %%d in ("C:\Program Files\Amazon Corretto\jdk21*") do (
    if exist "%%~d\bin\java.exe" (
        set "JAVA_EXE=%%~d\bin\java.exe"
        set "JAVA_HOME_SET=%%~d"
        echo  [OK] Corretto 21 found at %%~d
        goto :check_maven
    )
)

REM Check Eclipse Adoptium / Temurin 21
for /d %%d in ("C:\Program Files\Eclipse Adoptium\jdk-21*") do (
    if exist "%%~d\bin\java.exe" (
        set "JAVA_EXE=%%~d\bin\java.exe"
        set "JAVA_HOME_SET=%%~d"
        echo  [OK] Temurin 21 found at %%~d
        goto :check_maven
    )
)

REM Check Microsoft OpenJDK 21
for /d %%d in ("C:\Program Files\Microsoft\jdk-21*") do (
    if exist "%%~d\bin\java.exe" (
        set "JAVA_EXE=%%~d\bin\java.exe"
        set "JAVA_HOME_SET=%%~d"
        echo  [OK] Microsoft JDK 21 found at %%~d
        goto :check_maven
    )
)

REM Check Oracle Java 21 (default install path)
for /d %%d in ("C:\Program Files\Java\jdk-21*") do (
    if exist "%%~d\bin\java.exe" (
        set "JAVA_EXE=%%~d\bin\java.exe"
        set "JAVA_HOME_SET=%%~d"
        echo  [OK] Oracle JDK 21 found at %%~d
        goto :check_maven
    )
)

REM Check every java.exe on PATH (not just the first one)
where java >nul 2>&1
if %errorlevel% == 0 (
    for /f "delims=" %%p in ('where java') do (
        if not defined JAVA_EXE (
            "%%p" -version >"%TOOLS%\jver.tmp" 2>&1
            findstr /i /r "21\." "%TOOLS%\jver.tmp" >nul 2>&1
            if !errorlevel! == 0 (
                set "JAVA_EXE=%%p"
                REM Ask the JVM itself where its home is (handles Oracle javapath shims)
                "%%p" -XshowSettings:properties -version >"%TOOLS%\jprop.tmp" 2>&1
            )
        )
    )
    if defined JAVA_EXE (
        for /f "tokens=1,* delims==" %%a in ('findstr /c:"java.home" "%TOOLS%\jprop.tmp"') do (
            for /f "tokens=*" %%t in ("%%b") do set "JAVA_HOME_SET=%%t"
        )
        del "%TOOLS%\jver.tmp" >nul 2>&1
        del "%TOOLS%\jprop.tmp" >nul 2>&1
        echo  [OK] Java 21 found in system PATH
        goto :check_maven
    )
    del "%TOOLS%\jver.tmp" >nul 2>&1
)

REM ---- Download Corretto 21 ZIP (no admin needed) ----
echo  [!!] Java 21 not found. Downloading Amazon Corretto 21...
echo       Please wait, this may take a few minutes...
echo.

set "JDK_ZIP=%TOOLS%\corretto21.zip"
set "JDK_URL=https://corretto.aws/downloads/latest/amazon-corretto-21-x64-windows-jdk.zip"

powershell -NoProfile -Command "$ProgressPreference='SilentlyContinue'; [Net.ServicePointManager]::SecurityProtocol=[Net.SecurityProtocolType]::Tls12; Invoke-WebRequest -Uri '%JDK_URL%' -OutFile '%JDK_ZIP%'"

if not exist "%JDK_ZIP%" (
    echo.
    echo  [ERROR] Download failed. Check your internet connection.
    echo.
    echo  You can download Java 21 manually from:
    echo  https://corretto.aws/downloads/latest/amazon-corretto-21-x64-windows-jdk.msi
    echo.
    pause
    exit /b 1
)

echo  [..] Extracting Java...
powershell -NoProfile -Command "$ProgressPreference='SilentlyContinue'; Expand-Archive -Path '%JDK_ZIP%' -DestinationPath '%TOOLS%\jtmp' -Force; $d=(Get-ChildItem '%TOOLS%\jtmp' -Directory | Select -First 1).FullName; if(Test-Path '%JAVA_DIR%'){Remove-Item '%JAVA_DIR%' -Recurse -Force}; Move-Item $d '%JAVA_DIR%'; Remove-Item '%TOOLS%\jtmp' -Recurse -Force; Remove-Item '%JDK_ZIP%' -Force"

if not exist "%JAVA_DIR%\bin\java.exe" (
    echo  [ERROR] Java extraction failed. Try running as Administrator.
    pause
    exit /b 1
)

set "JAVA_EXE=%JAVA_DIR%\bin\java.exe"
set "JAVA_HOME_SET=%JAVA_DIR%"
echo  [OK] Java 21 installed to .tools\java\

REM ================================================================
REM  STEP 2 - Detect or install Maven
REM ================================================================
:check_maven
echo.
echo  [2/4] Checking Maven...

set "MVN_EXE="

if exist "%MVN_DIR%\bin\mvn.cmd" (
    set "MVN_EXE=%MVN_DIR%\bin\mvn.cmd"
    echo  [OK] Maven found in .tools\maven\
    goto :build
)

where mvn >nul 2>&1
if %errorlevel% == 0 (
    for /f "delims=" %%p in ('where mvn') do (
        if not defined MVN_EXE set "MVN_EXE=%%p"
    )
    echo  [OK] Maven found in system PATH
    goto :build
)

set "MVN_VERSION=3.9.9"
echo  [^!^!] Maven not found. Downloading Apache Maven !MVN_VERSION!...

set "MVN_ZIP=%TOOLS%\maven.zip"

REM Try current mirror first, then archive (which always hosts old releases)
call :download_maven "https://dlcdn.apache.org/maven/maven-3/!MVN_VERSION!/binaries/apache-maven-!MVN_VERSION!-bin.zip"
if not exist "%MVN_ZIP%" call :download_maven "https://archive.apache.org/dist/maven/maven-3/!MVN_VERSION!/binaries/apache-maven-!MVN_VERSION!-bin.zip"
if not exist "%MVN_ZIP%" (
    set "MVN_VERSION=3.9.6"
    call :download_maven "https://archive.apache.org/dist/maven/maven-3/!MVN_VERSION!/binaries/apache-maven-!MVN_VERSION!-bin.zip"
)

if not exist "%MVN_ZIP%" (
    echo  [ERROR] Maven download failed. Check your internet connection.
    pause
    exit /b 1
)
goto :extract_maven

:download_maven
powershell -NoProfile -Command "$ProgressPreference='SilentlyContinue'; [Net.ServicePointManager]::SecurityProtocol=[Net.SecurityProtocolType]::Tls12; try { Invoke-WebRequest -Uri '%~1' -OutFile '%MVN_ZIP%' } catch { exit 1 }"
exit /b 0

:extract_maven

echo  [..] Extracting Maven...
powershell -NoProfile -Command "$ProgressPreference='SilentlyContinue'; Expand-Archive -Path '%MVN_ZIP%' -DestinationPath '%TOOLS%\mtmp' -Force; $d=(Get-ChildItem '%TOOLS%\mtmp' -Directory | Select -First 1).FullName; if(Test-Path '%MVN_DIR%'){Remove-Item '%MVN_DIR%' -Recurse -Force}; Move-Item $d '%MVN_DIR%'; Remove-Item '%TOOLS%\mtmp' -Recurse -Force; Remove-Item '%MVN_ZIP%' -Force"

if not exist "%MVN_DIR%\bin\mvn.cmd" (
    echo  [ERROR] Maven extraction failed.
    pause
    exit /b 1
)

set "MVN_EXE=%MVN_DIR%\bin\mvn.cmd"
echo  [OK] Maven !MVN_VERSION! installed to .tools\maven\

REM ================================================================
REM  STEP 3 - Build
REM ================================================================
:build
echo.
echo  [3/4] Building Odyssey (first time may take ~2 min to download deps)...

REM Set JAVA_HOME and PATH for Maven
set "JAVA_HOME=!JAVA_HOME_SET!"
set "PATH=!JAVA_HOME!\bin;%PATH%"

echo  [..] JAVA_HOME = !JAVA_HOME!
echo  [..] Java    = !JAVA_EXE!
echo  [..] Maven   = !MVN_EXE!
echo.

cd /d "%ROOT%"
call "!MVN_EXE!" clean package -DskipTests >"%ROOT%\build.log" 2>&1

if %errorlevel% neq 0 (
    echo.
    echo  [ERROR] Build failed! Showing build.log:
    echo  ----------------------------------------------------------------
    type "%ROOT%\build.log"
    echo  ----------------------------------------------------------------
    echo.
    pause
    exit /b 1
)

echo  [OK] Build successful!

REM ================================================================
REM  STEP 4 - Save config for run.bat
REM ================================================================
echo.
echo  [4/4] Saving environment config...

(
    echo @echo off
    echo set "ODYSSEY_JAVA=!JAVA_EXE!"
    echo set "ODYSSEY_JAVA_HOME=!JAVA_HOME_SET!"
    echo set "ODYSSEY_MVN=!MVN_EXE!"
) > "%ROOT%\.env.bat"

echo  [OK] Config saved to .env.bat

echo.
echo  ================================================================
echo.
echo    Setup complete!
echo.
echo    To launch Odyssey:  double-click run.bat
echo    Browser will open:  http://localhost:8080
echo.
echo  ================================================================
echo.
pause
