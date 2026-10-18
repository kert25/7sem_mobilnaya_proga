[CmdletBinding()]
param(
    [string]$Avd = 'LR6_Pixel_6',
    [switch]$List
)

$ErrorActionPreference = 'Stop'
$sdkDir = Join-Path $env:LOCALAPPDATA 'Android\Sdk'
$emulator = Join-Path $sdkDir 'emulator\emulator.exe'
$adb = Join-Path $sdkDir 'platform-tools\adb.exe'

if (-not (Test-Path $emulator)) { throw "Android Emulator was not found at $emulator." }
if (-not (Test-Path $adb)) { throw "Android SDK Platform Tools were not found at $adb." }

if ($List) {
    & $emulator -list-avds
    exit $LASTEXITCODE
}

$available = @(& $emulator -list-avds)
if ($available -notcontains $Avd) {
    throw "AVD '$Avd' was not found. Run .\start-emulator.ps1 -List to see available devices."
}

$alreadyRunning = (& $adb devices) | Where-Object { $_ -match '^emulator-\d+\s+device$' }
if ($alreadyRunning.Count -eq 0) {
    Start-Process -FilePath $emulator -ArgumentList @('-avd', $Avd)
    Write-Host "Starting emulator '$Avd'..."
} else {
    Write-Host 'An emulator is already running.'
}

& $adb wait-for-device
& $adb shell getprop sys.boot_completed | Out-Null
Write-Host 'Emulator is ready.'
