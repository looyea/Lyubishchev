@echo off
setlocal enabledelayedexpansion

REM ============================================================
REM  Lyubishchev Web launcher
REM  - Starts the Vite dev server
REM  - Opens the default browser on the home page once ready
REM ============================================================

set "APP_DIR=%~dp0lyubishchev-web"
set "PORT=5173"
set "URL=http://localhost:%PORT%/"

if not exist "%APP_DIR%\" (
    echo [ERROR] Directory not found: %APP_DIR%
    pause
    exit /b 1
)
cd /d "%APP_DIR%"

REM Check node_modules, run npm install if missing
if not exist "node_modules\" (
    echo [INFO] node_modules not found, running "npm install" ...
    call npm install
    if errorlevel 1 (
        echo [ERROR] npm install failed. Please check your Node.js environment.
        pause
        exit /b 1
    )
)

REM If the port is already in use, assume the server is running
netstat -ano | findstr /r /c:":!PORT! .*LISTENING" >nul 2>&1
if !errorlevel! equ 0 (
    echo [INFO] Server already running on port !PORT!, opening browser...
    start "" "!URL!"
    goto :eof
)

echo [INFO] Starting dev server on port !PORT! ...
start "Lyubishchev-DevServer" /min cmd /c "cd /d "%APP_DIR%" && npm run dev"

REM Wait until the port is listening (max ~30s), then open browser
set "RETRIES=0"
:waitloop
timeout /t 1 /nobreak >nul
netstat -ano | findstr /r /c:":!PORT! .*LISTENING" >nul 2>&1
if !errorlevel! equ 0 goto openbrowser
set /a RETRIES+=1
if !RETRIES! geq 30 (
    echo [WARN] Server did not start within 30 seconds.
    echo        Check the "Lyubishchev-DevServer" window for details.
    pause
    exit /b 1
)
goto waitloop

:openbrowser
echo [INFO] Server is ready, opening !URL!
start "" "!URL!"
goto :eof
