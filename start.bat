@echo off
cd /d "%~dp0"
title MAE Server
call gradlew.bat bootRun
echo.
echo Server stopped.
pause
