[CmdletBinding()]
param(
    [Parameter(Mandatory = $true)]
    [ValidateSet('4', '5', '6')]
    [string]$Lab,
    [switch]$BuildOnly,
    [switch]$Clean
)

$ErrorActionPreference = 'Stop'
$root = $PSScriptRoot
$projectDirectory = Get-ChildItem -LiteralPath $root -Directory | Where-Object { $_.Name -like "*$Lab" } | Select-Object -First 1
if ($null -eq $projectDirectory) {
    throw "Laboratory directory ending in '$Lab' was not found in $root."
}
$projectDir = $projectDirectory.FullName
$javaHome = Join-Path $root '_android-tools\\jdk-17'
$java = Join-Path $javaHome 'bin\java.exe'

$packages = @{
    '4' = 'ru.tulsu.mobileprogramming.businesscard'
    '5' = 'ru.tulsu.mobileprogramming.mooddialog'
    '6' = 'ru.tulsu.mobileprogramming.imagecounter'
}
$packageName = $packages[$Lab]

if (-not (Test-Path $java)) {
    throw "JDK 17 is not prepared. Run .\setup-android.ps1 from $root first."
}
if (-not (Test-Path (Join-Path $projectDir 'gradlew.bat'))) {
    throw "Gradle Wrapper is missing in $projectDir."
}

$sdkDir = Join-Path $env:LOCALAPPDATA 'Android\Sdk'
$adb = Join-Path $sdkDir 'platform-tools\adb.exe'
if (-not (Test-Path $adb)) {
    throw "Android SDK Platform Tools were not found at $adb."
}

$env:JAVA_HOME = $javaHome
$env:ANDROID_HOME = $sdkDir
$env:ANDROID_SDK_ROOT = $sdkDir
$env:Path = "$(Join-Path $javaHome 'bin');$(Join-Path $sdkDir 'platform-tools');$env:Path"


Push-Location $projectDir
try {
    if ($Clean) {
        & .\gradlew.bat clean
        if ($LASTEXITCODE -ne 0) { throw "Gradle clean failed with exit code $LASTEXITCODE." }
    }

    & .\gradlew.bat assembleDebug
    if ($LASTEXITCODE -ne 0) { throw "Gradle build failed with exit code $LASTEXITCODE." }

    $apk = Join-Path $projectDir 'app\build\outputs\apk\debug\app-debug.apk'
    if (-not (Test-Path $apk)) { throw "APK was not created: $apk" }
    Write-Host "APK built: $apk"

    if ($BuildOnly) { exit 0 }

    $devices = (& $adb devices) | Where-Object { $_ -match '^[^\s]+\s+device$' }
    if ($devices.Count -eq 0) {
        Write-Warning 'No Android device or emulator is connected. APK was built, but not installed.'
        Write-Host 'Start one with .\start-emulator.ps1, or connect a phone with USB debugging enabled.'
        exit 0
    }

    & $adb install --no-streaming -r $apk
    if ($LASTEXITCODE -ne 0) {
        throw "APK installation failed with adb exit code $LASTEXITCODE. Check the adb output above for the Android error."
    }

    & $adb shell am start -n "$packageName/.MainActivity"
    if ($LASTEXITCODE -ne 0) {
        throw "Application launch failed with adb exit code $LASTEXITCODE."
    }
    Write-Host "Started $packageName/.MainActivity"
}
finally {
    Pop-Location
}
