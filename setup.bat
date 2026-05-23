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
set "JAVA_OK=0"

REM Check bundled Java first (.tools\java)
if exist "%JAVA_DIR%\bin\java.exe" (
    set "JAVA_EXE=%JAVA_DIR%\bin\java.exe"
    set "JAVA_OK=1"
    echo  [OK] Java found in .tools\java\
    goto :check_maven
)

REM Check Amazon Corretto 21
for /d %%d in ("C:\Program Files\Amazon Corretto\jdk21*") do (
    if exist "%%d\bin\java.exe" (
        set "JAVA_EXE=%%d\bin\java.exe"
        set "JAVA_OK=1"
        echo  [OK] Corretto 21 found at %%d
        goto :check_maven
    )
)

REM Check Eclipse Adoptium / Temurin 21
for /d %%d in ("C:\Program Files\Eclipse Adoptium\jdk-21*") do (
    if exist "%%d\bin\java.exe" (
        set "JAVA_EXE=%%d\bin\java.exe"
        set "JAVA_OK=1"
        echo  [OK] Temurin 21 found at %%d
        goto :check_maven
    )
)

REM Check Microsoft OpenJDK 21
for /d %%d in ("C:\Program Files\Microsoft\jdk-21*") do (
    if exist "%%d\bin\java.exe" (
        set "JAVA_EXE=%%d\bin\java.exe"
        set "JAVA_OK=1"
        echo  [OK] Microsoft JDK 21 found at %%d
        goto :check_maven
    )
)

REM Check system PATH
where java >/dev/null 2>&1
if %errorlevel% == 0 (
    java -version >"%TOOLS%\jver.tmp" 2>&1
    findstr /i "version \"21\." "%TOOLS%\jver.tmp" >/dev/null 2>&1
    if !errorlevel! == 0 (
        set "JAVA_EXE=java"
        set "JAVA_OK=1"
        echo  [OK] Java 21 found in system PATH
        del "%TOOLS%\jver.tmp" >/dev/null 2>&1
        goto :check_maven
    )
    del "%TOOLS%\jver.tmp" >/dev/null 2>&1
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
set "JAVA_OK=1"
echo  [OK] Java 21 installed to .tools\java\

REM ================================================================
REM  STEP 2 - Detect or install Maven
REM ================================================================
:check_maven
echo.
echo  [2/4] Checking Maven...

set "MVN_EXE="
set "MVN_OK=0"

if exist "%MVN_DIR%\bin\mvn.cmd" (
    set "MVN_EXE=%MVN_DIR%\bin\mvn.cmd"
    set "MVN_OK=1"
    echo  [OK] Maven found in .tools\maven\
    goto :build
)

where mvn >/dev/null 2>&1
if %errorlevel% == 0 (
    set "MVN_EXE=mvn"
    set "MVN_OK=1"
    echo  [OK] Maven found in system PATH
    goto :build
)

echo  [!!] Maven not found. Downloading Apache Maven 3.9.6...

set "MVN_ZIP=%TOOLS%\maven.zip"
set "MVN_URL=https://dlcdn.apache.org/maven/maven-3/3.9.6/binaries/apache-maven-3.9.6-bin.zip"

powershell -NoProfile -Command "$ProgressPreference='SilentlyContinue'; [Net.ServicePointManager]::SecurityProtocol=[Net.SecurityProtocolType]::Tls12; Invoke-WebRequest -Uri '%MVN_URL%' -OutFile '%MVN_ZIP%'"

if not exist "%MVN_ZIP%" (
    set "MVN_URL=https://archive.apache.org/dist/maven/maven-3/3.9.6/binaries/apache-maven-3.9.6-bin.zip"
    powershell -NoProfile -Command "$ProgressPreference='SilentlyContinue'; [Net.ServicePointManager]::SecurityProtocol=[Net.SecurityProtocolType]::Tls12; Invoke-WebRequest -Uri '%MVN_URL%' -OutFile '%MVN_ZIP%'"
)

if not exist "%MVN_ZIP%" (
    echo  [ERROR] Maven download failed. Check your internet connection.
    pause
    exit /b 1
)

echo  [..] Extracting Maven...
powershell -NoProfile -Command "$ProgressPreference='SilentlyContinue'; Expand-Archive -Path '%MVN_ZIP%' -DestinationPath '%TOOLS%\mtmp' -Force; $d=(Get-ChildItem '%TOOLS%\mtmp' -Directory | Select -First 1).FullName; if(Test-Path '%MVN_DIR%'){Remove-Item '%MVN_DIR%' -Recurse -Force}; Move-Item $d '%MVN_DIR%'; Remove-Item '%TOOLS%\mtmp' -Recurse -Force; Remove-Item '%MVN_ZIP%' -Force"

if not exist "%MVN_DIR%\bin\mvn.cmd" (
    echo  [ERROR] Maven extraction failed.
    pause
    exit /b 1
)

set "MVN_EXE=%MVN_DIR%\bin\mvn.cmd"
echo  [OK] Maven 3.9.6 installed to .tools\maven\

REM ================================================================
REM  STEP 3 - Build
REM ================================================================
:build
echo.
echo  [3/4] Building Odyssey (first time may take ~2 min to download deps)...

REM Derive JAVA_HOME from JAVA_EXE
if "%JAVA_EXE%"=="java" (
    for /f "delims=" %%p in ('where java') do set "JAVA_BIN=%%p" & goto :got_jbin
    :got_jbin
    for %%f in ("!JAVA_BIN!") do set "JAVA_HOME_SET=%%~dpf"
    set "JAVA_HOME_SET=!JAVA_HOME_SET:~0,-4!"
) else (
    for %%f in ("%JAVA_EXE%") do set "JAVA_HOME_SET=%%~dpf"
    set "JAVA_HOME_SET=!JAVA_HOME_SET:~0,-5!"
)

set "JAVA_HOME=!JAVA_HOME_SET!"
set "PATH=!JAVA_HOME!\bin;%MVN_DIR%\bin;%PATH%"

cd /d "%ROOT%"
call "%MVN_EXE%" clean package -DskipTests 2>"%ROOT%\build.log"

if %errorlevel% neq 0 (
    echo.
    echo  [ERROR] Build failed! Check build.log for details.
    echo.
    type "%ROOT%\build.log" | findstr /i "ERROR BUILD"
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
    echo set "ODYSSEY_JAVA=%JAVA_EXE%"
    echo set "ODYSSEY_JAVA_HOME=!JAVA_HOME_SET!"
    echo set "ODYSSEY_MVN=%MVN_EXE%"
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
