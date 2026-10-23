#!/usr/bin/env pwsh
# scripts/build-release.ps1 — PowerShell script for Windows
# Builds and signs the SkyBox TV release APK
#
# Prerequisites:
#   - JDK 17 installed (JAVA_HOME set)
#   - Android SDK installed (ANDROID_HOME set)
#   - Signing config: SKYBOX_KEYSTORE_PATH and SKYBOX_KEYSTORE_PASSWORD env vars
#   - OR local keystore at android/keystore.jks

# --- Configuration ---
$javaHome = $env:JAVA_HOME
$androidHome = $env:ANDROID_HOME
$gradlew = "app/gradlew"

if (-not (Test-Path $javaHome)) {
    Write-Error "JAVA_HOME not found: $javaHome"
    exit 1
}

if (-not (Test-Path $androidHome)) {
    Write-Error "ANDROID_HOME not found: $androidHome"
    exit 1
}

if (-not (Test-Path $gradlew)) {
    Write-Error "gradlew not found at $gradlew"
    exit 1
}

# --- Set up environment ---
Write-Host "=== SkyBox TV Release Build ===" -ForegroundColor Cyan
Write-Host "JAVA_HOME: $javaHome" -ForegroundColor Yellow
Write-Host "ANDROID_HOME: $androidHome" -ForegroundColor Yellow

# Ensure Java is on PATH
$oldPath = $env:PATH
$env:PATH = "$javaHome\bin;$androidHome\tools;$androidHome\toolsbuild;$androidHome\platform-tools;$env:PATH"

# --- Build release APK ---
Write-Host "`n--- Building release APK ---" -ForegroundColor Cyan

$gradlewResult = & &"$gradlew" "assembleRelease" `
    --no-daemon `
    --write-logs 2>&1

$gradlewExitCode = $LASTEXITCODE

if ($gradlewExitCode -ne 0) {
    Write-Error "Gradle assembleRelease failed with exit code $gradlewExitCode"
    Write-Error "Gradle output:`n$gradlewResult"
    exit $gradlewExitCode
}

Write-Host "Release APK built successfully." -ForegroundColor Green

# --- Sign APK with apksigner ---
# Determine the unsigned release APK path
$unsignedApk = "app/build/outputs/apk/release/app-release-unsigned.apk"
$signedApk = "app/build/outputs/apk/release/app-release.apk"

if (-not (Test-Path $unsignedApk)) {
    Write-Error "Unsigned APK not found at $unsignedApk"
    Write-Error "Checking possible locations..."
    Get-ChildItem -Path "app/build/outputs/apk/" -Recurse -Force 2>$null
    exit 1
}

Write-Host "Signing APK with apksigner..." -ForegroundColor Cyan

# Get signing credentials from environment
$keystorePath = $env:SKYBOX_KEYSTORE_PATH
$keystorePassword = $env:SKYBOX_KEYSTORE_PASSWORD

if (-not $keystorePath) {
    Write-Error "SKYBOX_KEYSTORE_PATH environment variable not set"
    Write-Error "Set it to the path of your release keystore, or place a keystore at android/keystore.jks"
    exit 1
}

if (-not $keystorePassword) {
    Write-Error "SKYBOX_KEYSTORE_PASSWORD environment variable not set"
    exit 1
}

# Build apksigner command
$alias = "skybox-tv"
$keystoreFile = [IO.File]::Exists($keystorePath) ? $keystorePath : Resolve-Path $keystorePath

$apksignerCmd = @(
    "apksigner",
    "sign",
    "--ks", $keystoreFile.Path,
    "--ks-pass", "pass:$keystorePassword",
    "--ks-key-alias", $alias,
    "--key-pass", "pass:$keystorePassword",
    "--out", $signedApk,
    $unsignedApk
) -join " "

Write-Host "Running: $apksignerCmd" -ForegroundColor Yellow

$signResult = & $apksignerCmd 2>&1
$signExitCode = $LASTEXITCODE

if ($signExitCode -ne 0) {
    Write-Error "apksigner failed with exit code $signExitCode"
    Write-Error "apksigner output:`n$signResult"
    exit $signExitCode
}

Write-Host "APK signed successfully." -ForegroundColor Green

# --- Output results ---
Write-Host "`n=== Build Complete ===" -ForegroundColor Cyan
Write-Host "Signed APK: $signedApk" -ForegroundColor Green
Write-Host "Size: $(Get-Item $signedApk).Length bytes ($(("{0:N2}") -f ($Get-Item $signedApk).Length / 1MB)) MB" -ForegroundColor Yellow

# Verify the signed APK
Write-Host "`nVerifying signed APK..." -ForegroundColor Cyan
& apksigner verify $signedApk
$verifyExitCode = $LASTEXITCODE

if ($verifyExitCode -eq 0) {
    Write-Host "Verification: PASSED" -ForegroundColor Green
} else {
    Write-Host "Verification: FAILED" -ForegroundColor Red
}

exit 0