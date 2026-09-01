# ASAK-back boot script - stop daemon, remove build lock, bootRun
param(
    [switch]$Clean
)

$ErrorActionPreference = "Stop"
$root = Resolve-Path (Join-Path $PSScriptRoot "..")
Set-Location $root

function Invoke-GradleQuiet {
    param(
        [Parameter(ValueFromRemainingArguments = $true)]
        [string[]]$GradleArgs
    )

    $previous = $ErrorActionPreference
    $ErrorActionPreference = "SilentlyContinue"
    & .\gradlew @GradleArgs *> $null
    $exitCode = $LASTEXITCODE
    $ErrorActionPreference = $previous

    if ($exitCode -ne 0) {
        throw "gradlew $($GradleArgs -join ' ') failed with exit code $exitCode"
    }
}

function Invoke-Gradle {
    param(
        [Parameter(ValueFromRemainingArguments = $true)]
        [string[]]$GradleArgs
    )

    $previous = $ErrorActionPreference
    $ErrorActionPreference = "Continue"
    & .\gradlew @GradleArgs
    $exitCode = $LASTEXITCODE
    $ErrorActionPreference = $previous
    exit $exitCode
}

function Stop-GradleDaemon {
    Invoke-GradleQuiet --stop
}

function Remove-BuildDirectory {
    param([string]$Path)

    if (-not (Test-Path $Path)) {
        return $true
    }

    for ($attempt = 1; $attempt -le 5; $attempt++) {
        try {
            Remove-Item -Recurse -Force $Path -ErrorAction Stop
            return $true
        }
        catch {
            Write-Host "build/ delete failed (attempt $attempt/5). Stopping daemon and retrying..."
            Stop-GradleDaemon
            Start-Sleep -Seconds 2
        }
    }

    return $false
}

Write-Host "Stopping Gradle daemon..."
Stop-GradleDaemon

if ($Clean) {
    $buildPath = Join-Path $root "build"
    if (-not (Remove-BuildDirectory $buildPath)) {
        Write-Host ""
        Write-Host "build/ is still locked."
        Write-Host "1) Ctrl+Shift+P -> Developer: Reload Window"
        Write-Host "2) Run again: .\scripts\boot-run.ps1 -Clean"
        Write-Host ""
        Write-Host "See: ASAK/docs/operations/setup/troubleshooting-backend.md"
        exit 1
    }
    if (Test-Path $buildPath) {
        Write-Host "Removed build/"
    }
}

Write-Host "Starting bootRun..."
Invoke-Gradle bootRun @args
