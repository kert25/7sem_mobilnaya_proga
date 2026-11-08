[CmdletBinding()]
param(
    # 1..3 — консольные лабораторные, 4..7 — Android-приложения.
    [Parameter(Mandatory = $true)]
    [ValidateSet('1', '2', '3', '4', '5', '6', '7')]
    [string]$Lab,
    # Для ЛР1–ЛР3: запустить дополнительное задание вместо основного.
    [switch]$Additional,
    [switch]$BuildOnly,
    [switch]$Clean
)

$ErrorActionPreference = 'Stop'
$root = $PSScriptRoot
. (Join-Path $root '_tools\AndroidEnv.ps1')

$envInfo = Set-LabEnvironment -ProjectRoot $root
$sdkDir = $envInfo.AndroidSdk

$gradlew = Join-Path $root 'gradlew.bat'
if (-not (Test-Path -LiteralPath $gradlew)) { throw "Gradle Wrapper is missing: $gradlew" }

$packages = @{
    '4' = 'ru.tulsu.mobileprogramming.businesscard'
    '5' = 'ru.tulsu.mobileprogramming.mooddialog'
    '6' = 'ru.tulsu.mobileprogramming.imagecounter'
    '7' = 'ru.tulsu.mobileprogramming.fontsize'
}

Push-Location $root
try {
    if ($Lab -in @('1', '2', '3')) {
        # Консоль Windows по умолчанию не в UTF-8: без этого русский текст
        # в выводе программы превращается в мусор.
        try { [Console]::OutputEncoding = [System.Text.UTF8Encoding]::new() } catch { }

        $module = if ($Additional) { ":lr$Lab`:additional" } else { ":lr$Lab`:main" }

        if ($Clean) {
            & $gradlew "$module`:clean" '--console=plain'
            if ($LASTEXITCODE -ne 0) { throw "Gradle clean failed with exit code $LASTEXITCODE." }
        }
        if ($BuildOnly) {
            & $gradlew "$module`:classes" '--console=plain'
        }
        else {
            & $gradlew "$module`:run" '--console=plain' '-q'
        }
        if ($LASTEXITCODE -ne 0) { throw "Gradle task failed with exit code $LASTEXITCODE." }
        return
    }

    $module = ":lr$Lab`:app"

    if ($Clean) {
        & $gradlew "$module`:clean" '--console=plain'
        if ($LASTEXITCODE -ne 0) { throw "Gradle clean failed with exit code $LASTEXITCODE." }
    }

    & $gradlew "$module`:assembleDebug" '--console=plain'
    if ($LASTEXITCODE -ne 0) { throw "Gradle build failed with exit code $LASTEXITCODE." }

    $apkDir = Join-Path $root "ЛР$Lab\app\build\outputs\apk\debug"
    $apk = Get-ChildItem -LiteralPath $apkDir -Filter '*.apk' -ErrorAction SilentlyContinue |
        Sort-Object LastWriteTime -Descending |
        Select-Object -First 1
    if (-not $apk) { throw "APK was not created in $apkDir." }
    Write-Host "APK built: $($apk.FullName)"

    if ($BuildOnly) { return }

    if (-not $sdkDir) { throw 'Android SDK was not found. Run .\setup-android.ps1 first.' }
    $adb = Join-Path $sdkDir 'platform-tools\adb.exe'
    if (-not (Test-Path -LiteralPath $adb)) { throw "Android SDK Platform Tools were not found at $adb." }

    $devices = (& $adb devices) | Where-Object { $_ -match '^[^\s]+\s+device$' }
    if ($devices.Count -eq 0) {
        Write-Warning 'No Android device or emulator is connected. APK was built, but not installed.'
        Write-Host 'Start the emulator with .\start-emulator.ps1, or connect a phone with USB debugging enabled.'
        return
    }

    & $adb install --no-streaming -r $apk.FullName
    if ($LASTEXITCODE -ne 0) {
        throw "APK installation failed with adb exit code $LASTEXITCODE. Check the adb output above for the Android error."
    }

    $packageName = $packages[$Lab]
    & $adb shell am start -n "$packageName/.MainActivity"
    if ($LASTEXITCODE -ne 0) { throw "Application launch failed with adb exit code $LASTEXITCODE." }
    Write-Host "Started $packageName/.MainActivity"
}
finally {
    Pop-Location
}
