[CmdletBinding()]
param(
    # JDK для Gradle. Путь намеренно вне папки проекта: он не содержит
    # кириллицы, и его понимают и командная строка, и Android Studio.
    [string]$JdkDir = (Join-Path $env:LOCALAPPDATA 'MobileLabs\jdk-17'),
    [switch]$Force
)

$ErrorActionPreference = 'Stop'
$root = $PSScriptRoot
$java = Join-Path $JdkDir 'bin\java.exe'
$jdkUrl = 'https://api.adoptium.net/v3/binary/latest/17/ga/windows/x64/jdk/hotspot/normal/eclipse'

if ($Force -or -not (Test-Path -LiteralPath $java)) {
    $work = Join-Path ([System.IO.Path]::GetTempPath()) ('mobilelabs-jdk-' + [guid]::NewGuid().ToString('N'))
    New-Item -ItemType Directory -Force -Path $work | Out-Null
    try {
        $archive = Join-Path $work 'temurin-17.zip'
        Write-Host "Downloading Temurin JDK 17 to $archive ..."
        Invoke-WebRequest -Uri $jdkUrl -OutFile $archive

        Write-Host 'Extracting JDK 17...'
        $extract = Join-Path $work 'extract'
        Expand-Archive -LiteralPath $archive -DestinationPath $extract -Force
        $extracted = Get-ChildItem -LiteralPath $extract -Directory | Select-Object -First 1
        if ($null -eq $extracted -or -not (Test-Path -LiteralPath (Join-Path $extracted.FullName 'bin\java.exe'))) {
            throw 'The downloaded archive does not contain a usable JDK.'
        }

        if (Test-Path -LiteralPath $JdkDir) { Remove-Item -LiteralPath $JdkDir -Recurse -Force }
        New-Item -ItemType Directory -Force -Path (Split-Path -Parent $JdkDir) | Out-Null
        Move-Item -LiteralPath $extracted.FullName -Destination $JdkDir
    }
    finally {
        Remove-Item -LiteralPath $work -Recurse -Force -ErrorAction SilentlyContinue
    }
}

if (-not (Test-Path -LiteralPath $java)) { throw "JDK was not found at $JdkDir." }

# Android Studio берёт JDK для Gradle из этого файла, когда в настройках
# Gradle JDK выбрано "Gradle local Java home" (#GRADLE_LOCAL_JAVA_HOME).
# Поэтому синхронизация запускается на JDK 17 без ручной правки настроек IDE.
$configDir = Join-Path $root '.gradle'
New-Item -ItemType Directory -Force -Path $configDir | Out-Null
$configFile = Join-Path $configDir 'config.properties'
Set-Content -LiteralPath $configFile -Encoding utf8 -Value @(
    '# Создано setup-android.ps1. JDK, на которой Android Studio запускает Gradle.',
    ('java.home=' + ($JdkDir -replace '\\', '/'))
)

# Путь к Android SDK — тот же, что пишет Android Studio в local.properties.
$sdk = $env:ANDROID_HOME
if (-not $sdk -or -not (Test-Path -LiteralPath $sdk)) {
    $sdk = Join-Path $env:LOCALAPPDATA 'Android\Sdk'
}
if (Test-Path -LiteralPath $sdk) {
    Set-Content -LiteralPath (Join-Path $root 'local.properties') -Encoding utf8 -Value @(
        '## Локальная конфигурация машины, в git не попадает.',
        '## Путь к Android SDK.',
        ('sdk.dir=' + ($sdk -replace '\\', '\\'))
    )
}
else {
    Write-Warning 'Android SDK was not found. Install it from Android Studio, then run this script again.'
}

Write-Host ''
Write-Host "JDK 17:       $JdkDir"
Write-Host "Gradle JDK:   $configFile"
Write-Host "Android SDK:  $sdk"
Write-Host ''
Write-Host 'Готово. Дальше: File -> Open в Android Studio, выбрать корень проекта.'
