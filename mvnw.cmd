@REM Minimal Maven Wrapper for Windows
@echo off
setlocal

set WRAPPER_DIR=%~dp0.mvn\wrapper
set WRAPPER_JAR=%WRAPPER_DIR%\maven-wrapper.jar
set PROPERTIES_FILE=%WRAPPER_DIR%\maven-wrapper.properties

if not exist "%WRAPPER_DIR%" mkdir "%WRAPPER_DIR%"

if not exist "%PROPERTIES_FILE%" (
    (
        echo distributionUrl=https://repo.maven.apache.org/maven2/org/apache/maven/apache-maven/3.9.6/apache-maven-3.9.6-bin.zip
        echo wrapperUrl=https://repo.maven.apache.org/maven2/org/apache/maven/wrapper/maven-wrapper/3.2.0/maven-wrapper-3.2.0.jar
    ) > "%PROPERTIES_FILE%"
)

if not exist "%WRAPPER_JAR%" (
    echo Downloading Maven Wrapper JAR...
    powershell -Command "[Net.ServicePointManager]::SecurityProtocol = [Net.SecurityProtocolType]::Tls12; (New-Object Net.WebClient).DownloadFile('https://repo.maven.apache.org/maven2/io/takari/maven-wrapper/0.5.6/maven-wrapper-0.5.6.jar', '%WRAPPER_JAR%')"
)

java -classpath "%WRAPPER_JAR%" "-Dmaven.multiModuleProjectDirectory=%~dp0." org.apache.maven.wrapper.MavenWrapperMain %*
