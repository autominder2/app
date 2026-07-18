@echo off
REM ============================================================
REM  AutoMinder Release Gate — run from anywhere, results saved
REM  to gate_result.txt in the project root.
REM
REM  AFTER IT FINISHES: open gate_result.txt, copy the LAST 40
REM  lines, and paste them into the Claude chat. That's all.
REM ============================================================
cd /d "%~dp0.."
echo Running release gate... this takes several minutes.
call gradlew.bat clean assembleDebug testDebugUnitTest lintDebug compileReleaseKotlin > gate_result.txt 2>&1
echo.
echo ============ GATE FINISHED ============
echo Result file: %CD%\gate_result.txt
findstr /C:"BUILD SUCCESSFUL" /C:"BUILD FAILED" gate_result.txt
echo.
echo Copy the LAST 40 lines of gate_result.txt into the chat.
echo.
REM Optional (needs a connected phone or running emulator):
REM call gradlew.bat connectedDebugAndroidTest >> gate_result.txt 2>&1
pause
