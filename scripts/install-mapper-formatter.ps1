$ErrorActionPreference = "Stop"

$workspaceRoot = Resolve-Path (Join-Path $PSScriptRoot "..\..")
$source = Join-Path $workspaceRoot ".vscode\extensions\asak-mybatis-mapper-formatter"

if (-not (Test-Path $source)) {
    throw "Extension source not found: $source"
}

$targetRoot = Join-Path $env:USERPROFILE ".cursor\extensions\asak.mybatis-mapper-formatter-0.0.1"

if (Test-Path $targetRoot) {
    Remove-Item -Recurse -Force $targetRoot
}

New-Item -ItemType Directory -Path $targetRoot | Out-Null
Copy-Item -Path (Join-Path $source "*") -Destination $targetRoot -Recurse -Force

Write-Host "Installed ASAK MyBatis mapper formatter to:"
Write-Host $targetRoot
Write-Host "Reload Cursor window (Ctrl+Shift+P -> Developer: Reload Window)."
