[CmdletBinding()]
param(
    [switch]$Force
)

$ErrorActionPreference = 'Stop'
$root = $PSScriptRoot
$toolsDir = Join-Path $root '_android-tools'
$jdkDir = Join-Path $toolsDir 'jdk-17'
$java = Join-Path $jdkDir 'bin\java.exe'
$archive = Join-Path $toolsDir 'temurin-17.zip'
$tempDir = Join-Path $toolsDir 'jdk-extract'
$jdkUrl = 'https://api.adoptium.net/v3/binary/latest/17/ga/windows/x64/jdk/hotspot/normal/eclipse'

New-Item -ItemType Directory -Force -Path $toolsDir | Out-Null

if ($Force -or -not (Test-Path $java)) {
    Remove-Item -Recurse -Force -ErrorAction SilentlyContinue $tempDir
    Remove-Item -Force -ErrorAction SilentlyContinue $archive

    Write-Host 'Downloading portable Temurin JDK 17...'
    Invoke-WebRequest -Uri $jdkUrl -OutFile $archive

    Write-Host 'Extracting JDK 17...'
    Expand-Archive -Path $archive -DestinationPath $tempDir -Force
    $extractedJdk = Get-ChildItem -Path $tempDir -Directory | Select-Object -First 1
    if ($null -eq $extractedJdk -or -not (Test-Path (Join-Path $extractedJdk.FullName 'bin\java.exe'))) {
        throw 'The downloaded archive does not contain a usable JDK.'
    }

    Remove-Item -Recurse -Force -ErrorAction SilentlyContinue $jdkDir
    Move-Item -Path $extractedJdk.FullName -Destination $jdkDir
    Remove-Item -Recurse -Force $tempDir
    Remove-Item -Force $archive
}

$env:JAVA_HOME = $jdkDir
$env:Path = "$(Join-Path $jdkDir 'bin');$env:Path"

Write-Host "JDK 17 is ready at $jdkDir"
Write-Host 'Android command-line environment is ready.'
