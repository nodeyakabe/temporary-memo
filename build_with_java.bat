@echo off
set "JAVA_HOME=C:\Program Files\Android\Android Studio\jbr"
set "PATH=%JAVA_HOME%\bin;%PATH%"
cd /d "C:\Users\seizo\Desktop\App\temporary-memo"
call "%~dp0gradlew.bat" clean assembleDebug
