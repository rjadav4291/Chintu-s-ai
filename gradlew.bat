@echo off
setlocal

set "VER=8.10.2"

where gradle >nul 2>nul && (
    gradle %*
    exit /b %errorlevel%
)

set "CACHE=%USERPROFILE%\.gradle\chintu-wrapper\%VER%"
set "DIST=%CACHE%\gradle-%VER%\bin\gradle.bat"

if not exist "%DIST%" (
    powershell -NoProfile -Command "$ErrorActionPreference='Stop'; New-Item -ItemType Directory -Force -Path '%CACHE%' | Out-Null; Invoke-WebRequest -Uri 'https://services.gradle.org/distributions/gradle-%VER%-bin.zip' -OutFile '%CACHE%\gradle.zip'; Expand-Archive -Force '%CACHE%\gradle.zip' '%CACHE%'"
)

call "%DIST%" %*
