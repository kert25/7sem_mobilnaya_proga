[CmdletBinding()]
param(
    [switch]$BuildOnly,
    [switch]$Clean
)

& (Join-Path $PSScriptRoot '..\run-android.ps1') -Lab '5' -BuildOnly:$BuildOnly -Clean:$Clean
exit $LASTEXITCODE
