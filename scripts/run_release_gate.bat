@echo off
setlocal EnableDelayedExpansion
REM ============================================================
REM  AutoMinder Release Gate v2 — double-click and wait.
REM
REM  When it finishes it prints PASS or FAIL and creates:
REM    gate_summary.txt  <-- ATTACH THIS FILE IN THE CLAUDE CHAT
REM    gate_full_log.txt (full log, only if asked for)
REM
REM  This script never modifies your SDK/Java configuration and
REM  never prints passwords, tokens, or environment secrets.
REM ============================================================
cd /d "%~dp0.."
set "RESULT=FAIL"
set "FAILTASK="
set "JAVA_EXE="

REM ---------- 1. Find Java 17 or newer ----------
call :try_java "%JAVA_HOME%"
call :try_java "%ProgramFiles%\Android\Android Studio\jbr"
call :try_java "%LOCALAPPDATA%\Programs\Android Studio\jbr"
for /d %%D in ("%ProgramFiles%\Eclipse Adoptium\jdk-*") do call :try_java "%%~D"
for /d %%D in ("%ProgramFiles%\Java\jdk-*") do call :try_java "%%~D"
for /d %%D in ("%ProgramFiles%\Microsoft\jdk-*") do call :try_java "%%~D"
if not defined JAVA_EXE (
    echo RESULT: FAIL
    echo REASON: No Java 17+ found. Easiest fix: install Android Studio,
    echo         then run this script again ^(it finds Studio's bundled Java^).
    echo RESULT: FAIL — no Java 17+ found on this machine > gate_summary.txt
    pause
    exit /b 1
)
echo Using Java !JAVA_MAJOR! (found automatically)

REM ---------- 2. Find the Android SDK ----------
set "SDK_SOURCE="
if exist "local.properties" (
    findstr /B /C:"sdk.dir" local.properties >nul 2>&1 && set "SDK_SOURCE=local.properties"
)
if not defined SDK_SOURCE if defined ANDROID_HOME if exist "%ANDROID_HOME%\platforms" set "SDK_SOURCE=ANDROID_HOME"
if not defined SDK_SOURCE if defined ANDROID_SDK_ROOT if exist "%ANDROID_SDK_ROOT%\platforms" (
    set "ANDROID_HOME=%ANDROID_SDK_ROOT%"
    set "SDK_SOURCE=ANDROID_SDK_ROOT"
)
if not defined SDK_SOURCE if exist "%LOCALAPPDATA%\Android\Sdk\platforms" (
    set "ANDROID_HOME=%LOCALAPPDATA%\Android\Sdk"
    set "SDK_SOURCE=default install path"
)
if not defined SDK_SOURCE (
    echo RESULT: FAIL
    echo REASON: Android SDK not found. Easiest fix: install Android Studio
    echo         and open this project once, then rerun this script.
    echo RESULT: FAIL — Android SDK not found > gate_summary.txt
    pause
    exit /b 1
)
echo Android SDK located via: %SDK_SOURCE%

REM ---------- 3. Choose tasks ----------
REM assembleDebug covers manifest merge + dependency resolution;
REM compileReleaseKotlin proves release code compiles;
REM bundleRelease runs only when the production AdMob ID is configured,
REM because the project's own safety gate intentionally fails without it.
set "TASKS=clean assembleDebug compileReleaseKotlin testDebugUnitTest lintDebug"
set "ADMOB_OK="
if defined RELEASE_ADMOB_ID set "ADMOB_OK=1"
if exist "local.properties" (
    findstr /B /C:"RELEASE_ADMOB_ID" local.properties >nul 2>&1 && set "ADMOB_OK=1"
)
if defined ADMOB_OK (
    set "TASKS=!TASKS! bundleRelease"
) else (
    echo NOTE: bundleRelease skipped — RELEASE_ADMOB_ID not configured yet ^(fine before production^).
)

echo.
echo Running the gate — this takes several minutes. Do not close this window.
echo Tasks: !TASKS!
call gradlew.bat !TASKS! > gate_full_log.txt 2>&1
set "BUILDERR=%ERRORLEVEL%"

REM ---------- 4. Optional: migration tests if a phone/emulator is attached ----------
set "DEVICE="
if exist "%ANDROID_HOME%\platform-tools\adb.exe" (
    for /f "skip=1 tokens=1,2" %%a in ('"%ANDROID_HOME%\platform-tools\adb.exe" devices 2^>nul') do (
        if "%%b"=="device" set "DEVICE=1"
    )
)
if defined DEVICE (
    echo Device detected — running Room migration tests on it...
    call gradlew.bat connectedDebugAndroidTest >> gate_full_log.txt 2>&1
    if errorlevel 1 set "BUILDERR=1"
) else (
    echo NOTE: no phone/emulator attached — migration tests skipped this run.
)

REM ---------- 5. Verdict + summary file ----------
if "%BUILDERR%"=="0" (set "RESULT=PASS") else (set "RESULT=FAIL")
for /f "delims=" %%L in ('findstr /C:"FAILED" gate_full_log.txt ^| findstr /C:"> Task"') do (
    if not defined FAILTASK set "FAILTASK=%%L"
)

> gate_summary.txt (
    echo RESULT: %RESULT%
    if defined FAILTASK echo FIRST FAILING TASK: !FAILTASK!
    if defined DEVICE (echo MIGRATION TESTS: ran on attached device) else (echo MIGRATION TESTS: skipped - no device)
    if defined ADMOB_OK (echo RELEASE BUNDLE: attempted) else (echo RELEASE BUNDLE: skipped - RELEASE_ADMOB_ID not set)
    echo ----- last 60 lines of the build log -----
)
powershell -NoProfile -Command "Get-Content gate_full_log.txt -Tail 60" >> gate_summary.txt 2>nul
if errorlevel 1 type gate_full_log.txt >> gate_summary.txt

echo.
echo ==================================================
echo   RESULT: %RESULT%
if defined FAILTASK echo   FAILING TASK: !FAILTASK!
echo.
echo   ATTACH THIS FILE IN THE CLAUDE CHAT:
echo   %CD%\gate_summary.txt
echo ==================================================
pause
exit /b %BUILDERR%

REM ---------- helper: accept a JDK root if java is 17+ ----------
:try_java
if defined JAVA_EXE goto :eof
if "%~1"=="" goto :eof
if not exist "%~1\bin\java.exe" goto :eof
set "CAND_VER="
for /f tokens^=2^ delims^=^" %%v in ('"%~1\bin\java.exe" -fullversion 2^>^&1') do set "CAND_VER=%%v"
if not defined CAND_VER goto :eof
for /f "delims=." %%m in ("!CAND_VER!") do set "CAND_MAJOR=%%m"
if "!CAND_MAJOR!"=="1" goto :eof
if !CAND_MAJOR! GEQ 17 (
    set "JAVA_EXE=%~1\bin\java.exe"
    set "JAVA_HOME=%~1"
    set "JAVA_MAJOR=!CAND_MAJOR!"
)
goto :eof
