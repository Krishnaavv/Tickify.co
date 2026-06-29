@echo off
setlocal

set GRADLE_VERSION=8.10.2
set GRADLE_DIR=%USERPROFILE%\.gradle\wrapper\dists\gradle-%GRADLE_VERSION%
set GRADLE_ZIP=%GRADLE_DIR%\gradle-%GRADLE_VERSION%-bin.zip
set GRADLE_HOME=%GRADLE_DIR%\gradle-%GRADLE_VERSION%

if not exist "%GRADLE_DIR%" mkdir "%GRADLE_DIR%"

if not exist "%GRADLE_HOME%\bin\gradle.bat" (
    if not exist "%GRADLE_ZIP%" (
        echo Downloading Gradle %GRADLE_VERSION%...
        powershell -Command "[Net.ServicePointManager]::SecurityProtocol = [Net.SecurityProtocolType]::Tls12; (New-Object Net.WebClient).DownloadFile('https://services.gradle.org/distributions/gradle-%GRADLE_VERSION%-bin.zip', '%GRADLE_ZIP%')"
    )
    echo Extracting Gradle...
    powershell -Command "Expand-Archive -Path '%GRADLE_ZIP%' -DestinationPath '%GRADLE_DIR%'"
    if exist "%GRADLE_ZIP%" del "%GRADLE_ZIP%"
)

"%GRADLE_HOME%\bin\gradle.bat" %*
