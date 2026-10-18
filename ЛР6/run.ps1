[CmdletBinding()]
param(
    [switch]$BuildOnly,
    [switch]$Clean
)

& (Join-Path $PSScriptRoot '..\run-android.ps1') -Lab '6' -BuildOnly:$BuildOnly -Clean:$Clean
exit $LASTEXITCODE
