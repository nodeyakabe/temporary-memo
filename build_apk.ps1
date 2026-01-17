$env:JAVA_HOME = "C:\Program Files\Android\Android Studio\jbr"
$env:PATH = "$env:JAVA_HOME\bin;$env:PATH"

Write-Host "Building APK with Android Studio Java..."
Write-Host "JAVA_HOME: $env:JAVA_HOME"

Set-Location "C:\Users\seizo\Desktop\App\temporary-memo"
& .\gradlew.bat assembleRelease

if ($LASTEXITCODE -eq 0) {
    Write-Host ""
    Write-Host "Build successful!"
    Write-Host "APK location: app\build\outputs\apk\release\"
    Get-ChildItem "app\build\outputs\apk\release\*.apk"
} else {
    Write-Host ""
    Write-Host "Build failed with error code $LASTEXITCODE"
}
