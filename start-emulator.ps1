[CmdletBinding()]
param(
    [string]$Avd = 'LR6_Pixel_6',
    # Сколько секунд ждать полной загрузки Android.
    [int]$TimeoutSeconds = 420,
    [switch]$List
)

$ErrorActionPreference = 'Stop'
$root = $PSScriptRoot
. (Join-Path $root '_tools\AndroidEnv.ps1')

$envInfo = Set-LabEnvironment -ProjectRoot $root
$sdkDir = $envInfo.AndroidSdk
if (-not $sdkDir) { throw 'Android SDK was not found. Run .\setup-android.ps1 first.' }

$emulator = Join-Path $sdkDir 'emulator\emulator.exe'
$adb = Join-Path $sdkDir 'platform-tools\adb.exe'
if (-not (Test-Path -LiteralPath $emulator)) { throw "Android Emulator was not found at $emulator." }
if (-not (Test-Path -LiteralPath $adb)) { throw "Android SDK Platform Tools were not found at $adb." }

if ($List) {
    & $emulator -list-avds
    exit $LASTEXITCODE
}

$available = @(& $emulator -list-avds)
if ($available -notcontains $Avd) {
    throw "AVD '$Avd' was not found. Run .\start-emulator.ps1 -List to see available devices."
}

$running = (& $adb devices) | Where-Object { $_ -match '^emulator-\d+\s+device$' }
if ($running.Count -eq 0) {
    Write-Host "Starting emulator '$Avd'..."
    Start-Process -FilePath $emulator -ArgumentList @('-avd', $Avd, '-no-boot-anim') | Out-Null
}
else {
    Write-Host 'An emulator is already running.'
}

Write-Host 'Waiting for the device to appear...'
& $adb wait-for-device

# wait-for-device возвращается, как только ADB увидел устройство, но Android
# ещё загружается: установка APK в этот момент падает. Поэтому ждём признак
# завершённой загрузки.
$deadline = (Get-Date).AddSeconds($TimeoutSeconds)
$booted = $false
while ((Get-Date) -lt $deadline) {
    $state = (& $adb shell getprop sys.boot_completed 2>$null | Out-String).Trim()
    if ($state -eq '1') { $booted = $true; break }
    Start-Sleep -Seconds 2
}

if (-not $booted) {
    throw "Android did not finish booting in $TimeoutSeconds seconds. Try 'adb emu kill' and start the emulator again."
}

Write-Host 'Emulator is ready.'
