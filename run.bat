@echo off
setlocal EnableExtensions
cd /d "%~dp0"
title Wasl-e-Ta'am launcher

echo ============================================================
echo   Wasl-e-Ta'am - Smart Food Surplus Redistribution
echo ============================================================
echo.

if not exist "pom.xml" (
    echo [ERROR] pom.xml was not found in "%CD%".
    echo Place run.bat in the project root and try again.
    goto :fail
)

rem --- JDK 21 check ---------------------------------------------------------
where java >nul 2>nul
if errorlevel 1 (
    echo [ERROR] Java was not found on PATH.
    echo Install JDK 21 and make sure "java -version" works in a new terminal.
    goto :fail
)

set "JAVA_VERSION="
for /f "tokens=3" %%v in ('java -version 2^>^&1 ^| findstr /i "version"') do if not defined JAVA_VERSION set "JAVA_VERSION=%%~v"
for /f "tokens=1 delims=." %%m in ("%JAVA_VERSION%") do set "JAVA_MAJOR=%%m"
if not defined JAVA_MAJOR set "JAVA_MAJOR=0"
if %JAVA_MAJOR% LSS 21 (
    echo [ERROR] Java 21 or newer is required. Detected: %JAVA_VERSION%
    echo Update your JDK and make sure "java -version" reports 21 or newer.
    goto :fail
)

rem --- Maven check ----------------------------------------------------------
where mvn >nul 2>nul
if errorlevel 1 (
    echo [ERROR] Maven was not found on PATH.
    echo Install Maven 3.9+ and make sure "mvn -version" works.
    goto :fail
)

echo Demo accounts (password: demo1234):
echo   donor@wasl.demo        Donor
echo   bakery@wasl.demo       Donor
echo   recipient@wasl.demo    Recipient
echo   shelter@wasl.demo      Recipient
echo   volunteer@wasl.demo    Volunteer
echo   courier@wasl.demo      Volunteer
echo   admin@wasl.demo        Full administrator
echo   ops@wasl.demo          Limited administrator
echo.
echo Accounts database: data\wasl-e-taam.db
echo Delete that file to reset accounts; demo data is re-seeded on launch.
echo.
echo Starting Wasl-e-Ta'am...
echo.

call mvn -B javafx:run
set "EXIT_CODE=%ERRORLEVEL%"

if not "%EXIT_CODE%"=="0" (
    echo.
    echo [ERROR] Wasl-e-Ta'am exited with code %EXIT_CODE%.
    echo Check logs\waslataam.log for details.
    goto :fail_with_code
)

echo.
echo Wasl-e-Ta'am closed normally.
endlocal & exit /b 0

:fail
set "EXIT_CODE=1"

:fail_with_code
echo.
pause
endlocal & exit /b %EXIT_CODE%
