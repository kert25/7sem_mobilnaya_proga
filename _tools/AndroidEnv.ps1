# Общие функции для скриптов запуска лабораторных.
# Подключается через: . (Join-Path $PSScriptRoot '..\_tools\AndroidEnv.ps1')

function Get-LabGradleJdk {
    param([string]$ProjectRoot)

    $configFile = Join-Path $ProjectRoot '.gradle\config.properties'
    if (Test-Path -LiteralPath $configFile) {
        $line = Get-Content -LiteralPath $configFile |
            Where-Object { $_ -match '^\s*java\.home\s*=' } |
            Select-Object -First 1
        if ($line) {
            $value = (($line -replace '^\s*java\.home\s*=\s*', '').Trim()) -replace '/', '\'
            if (Test-Path -LiteralPath (Join-Path $value 'bin\java.exe')) { return $value }
        }
    }

    $fallback = Join-Path $env:LOCALAPPDATA 'MobileLabs\jdk-17'
    if (Test-Path -LiteralPath (Join-Path $fallback 'bin\java.exe')) { return $fallback }
    return $null
}

function Get-LabAndroidSdk {
    param([string]$ProjectRoot)

    $localFile = Join-Path $ProjectRoot 'local.properties'
    if (Test-Path -LiteralPath $localFile) {
        $line = Get-Content -LiteralPath $localFile |
            Where-Object { $_ -match '^\s*sdk\.dir\s*=' } |
            Select-Object -First 1
        if ($line) {
            $value = ($line -replace '^\s*sdk\.dir\s*=\s*', '').Trim()
            $value = ($value -replace '\\\\', '\') -replace '\\:', ':'
            if (Test-Path -LiteralPath $value) { return $value }
        }
    }

    foreach ($candidate in @($env:ANDROID_HOME, $env:ANDROID_SDK_ROOT, (Join-Path $env:LOCALAPPDATA 'Android\Sdk'))) {
        if ($candidate -and (Test-Path -LiteralPath $candidate)) { return $candidate }
    }
    return $null
}

function Set-LabEnvironment {
    param([string]$ProjectRoot)

    $javaHome = Get-LabGradleJdk -ProjectRoot $ProjectRoot
    if (-not $javaHome) {
        throw "JDK 17 is not prepared. Run setup-android.ps1 in $ProjectRoot first."
    }

    $env:JAVA_HOME = $javaHome
    $env:Path = "$(Join-Path $javaHome 'bin');$env:Path"

    $sdkDir = Get-LabAndroidSdk -ProjectRoot $ProjectRoot
    if ($sdkDir) {
        $env:ANDROID_HOME = $sdkDir
        $env:ANDROID_SDK_ROOT = $sdkDir
        $env:Path = "$(Join-Path $sdkDir 'platform-tools');$env:Path"
    }

    return [pscustomobject]@{ JavaHome = $javaHome; AndroidSdk = $sdkDir }
}
