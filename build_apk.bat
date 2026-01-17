@echo off
set JAVA_HOME=C:\Program Files\Android\Android Studio\jbr
set PATH=%JAVA_HOME%\bin;%PATH%

echo Building APK with Android Studio Java...
echo JAVA_HOME: %JAVA_HOME%

call gradlew.bat assembleRelease

if %ERRORLEVEL% EQU 0 (
    echo.
    echo Build successful!
    echo APK location: app\build\outputs\apk\release\
    dir app\build\outputs\apk\release\*.apk
) else (
    echo.
    echo Build failed with error code %ERRORLEVEL%
)
