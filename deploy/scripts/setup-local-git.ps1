param(
    [Parameter(Mandatory = $true)]
    [string]$GitLabUrl
)

$ErrorActionPreference = "Stop"
Set-Location (Split-Path (Split-Path $PSScriptRoot -Parent) -Parent)

if (-not (Get-Command git -ErrorAction SilentlyContinue)) {
    Write-Error "git not found in PATH"
}

$remote = git remote get-url origin 2>$null
if ($LASTEXITCODE -ne 0) {
    git remote add origin $GitLabUrl
    Write-Host "Added remote origin: $GitLabUrl"
} else {
    Write-Host "Remote origin already exists: $remote"
    $answer = Read-Host "Replace with $GitLabUrl ? (y/N)"
    if ($answer -eq "y" -or $answer -eq "Y") {
        git remote set-url origin $GitLabUrl
    }
}

$branch = git branch --show-current
if ($branch -ne "develop") {
    $exists = git show-ref --verify --quiet refs/heads/develop
    if ($LASTEXITCODE -eq 0) {
        git checkout develop
    } else {
        git checkout -b develop
    }
}

Write-Host ""
Write-Host "Next steps:"
Write-Host "  git add ."
Write-Host "  git commit -m ""ci: add GitLab CI/CD pipeline"""
Write-Host "  git push -u origin develop"
Write-Host ""
Write-Host "After push, open GitLab → CI/CD → Pipelines"
Write-Host "Demo URL (after deploy): http://192.168.204.111:8088"
