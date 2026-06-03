# 本地运行与 CI 相同的门禁（Windows PowerShell）
$ErrorActionPreference = "Stop"
$Root = Split-Path (Split-Path $PSScriptRoot -Parent) -Parent
Set-Location $Root

Write-Host "==> lint-frontend"
Set-Location "$Root/front-end/sdp-app-vue"
npm ci
npm run build

Write-Host "==> test-backend"
Set-Location "$Root/back-end/sdp-admin"
mvn -pl sdp-server -am test -B

Write-Host ""
Write-Host "All CI gates passed locally."
