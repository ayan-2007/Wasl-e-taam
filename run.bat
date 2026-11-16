@echo off
setlocal
cd /d "%~dp0"

where mvn >nul 2>nul
if errorlevel 1 (
    echo Maven was not found on PATH. Install Maven 3.9+ and JDK 21 first.
    exit /b 1
)

echo Starting Wasl-e-Ta'am...
call mvn -B javafx:run
