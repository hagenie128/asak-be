$ErrorActionPreference = "Stop"

$scriptsDir = $PSScriptRoot
$workspaceRoot = Resolve-Path (Join-Path $scriptsDir "..\..")
$source = Join-Path $workspaceRoot ".vscode\extensions\asak-mybatis-mapper-formatter"
$venvPath = Join-Path $scriptsDir ".venv"
$venvPython = Join-Path $venvPath "Scripts\python.exe"
$requirements = Join-Path $scriptsDir "requirements.txt"

if (-not (Test-Path $source)) {
    throw "Extension source not found: $source"
}

if (-not (Test-Path $venvPython)) {
    Write-Host "Creating formatter venv at: $venvPath"
    py -m venv $venvPath
}

& $venvPython -m pip install --upgrade pip | Out-Null
& $venvPython -m pip install -r $requirements

$targetRoot = Join-Path $env:USERPROFILE ".cursor\extensions\asak.mybatis-mapper-formatter-0.0.1"

if (Test-Path $targetRoot) {
    Remove-Item -Recurse -Force $targetRoot
}

New-Item -ItemType Directory -Path $targetRoot | Out-Null
Copy-Item -Path (Join-Path $source "*") -Destination $targetRoot -Recurse -Force

Write-Host "Formatter venv: $venvPath"
Write-Host "Installed ASAK MyBatis mapper formatter to:"
Write-Host $targetRoot
Write-Host "Reload Cursor window (Ctrl+Shift+P -> Developer: Reload Window)."
