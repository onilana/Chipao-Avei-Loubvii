# ==============================================================================
# Chipao Avei Loubvu -- Automated Release Helper Script
# ==============================================================================
# Run this script whenever you want to release a new version:
# .\release.bat  (or powershell -ExecutionPolicy Bypass -File .\release_helper.ps1)
# ==============================================================================

Write-Host "==================================================" -ForegroundColor Cyan
Write-Host "  Chipao Avei Loubvu -- App Release Helper" -ForegroundColor Cyan
Write-Host "==================================================" -ForegroundColor Cyan
Write-Host ""

# 1. Read current version from app/build.gradle.kts
$gradleFile = "app\build.gradle.kts"
$content = Get-Content $gradleFile -Raw

$currentCodeMatch = [regex]::Match($content, 'versionCode\s*=\s*(\d+)')
$currentNameMatch = [regex]::Match($content, 'versionName\s*=\s*"([^"]+)"')

$currentCode = if ($currentCodeMatch.Success) { [int]$currentCodeMatch.Groups[1].Value } else { 4 }
$currentName = if ($currentNameMatch.Success) { $currentNameMatch.Groups[1].Value } else { "1.3" }

Write-Host "Current App Version: " -NoNewline
Write-Host "v$currentName" -ForegroundColor Yellow
Write-Host ""

# 2. Prompt user for new version name
$inputName = Read-Host "Enter new Version Name [press Enter for v$currentName or type e.g. 1.4]"
$newVersionName = if ([string]::IsNullOrWhiteSpace($inputName)) { $currentName } else { $inputName.TrimStart('v').Trim() }

# Automatically increment internal Android versionCode in background
$newVersionCode = $currentCode + 1

$releaseNotes = Read-Host "Enter Release Notes summary [e.g. Hymnal updates and bug fixes]"
if ([string]::IsNullOrWhiteSpace($releaseNotes)) {
    $releaseNotes = "Hymnal improvements and bug fixes."
}

Write-Host ""
Write-Host "Preparing Release: v$newVersionName..." -ForegroundColor Green

# 3. Update app/build.gradle.kts
$newContent = [regex]::Replace($content, 'versionCode\s*=\s*\d+', "versionCode = $newVersionCode")
$newContent = [regex]::Replace($newContent, 'versionName\s*=\s*"[^"]+"', ('versionName = "' + $newVersionName + '"'))
Set-Content -Path $gradleFile -Value $newContent -Encoding UTF8
Write-Host "  [OK] Updated $gradleFile" -ForegroundColor DarkGreen

# 4. Update version.json
$versionJsonObj = [PSCustomObject]@{
    latestVersionCode = $newVersionCode
    latestVersionName = $newVersionName
    releaseNotes      = $releaseNotes
    downloadUrl       = "https://github.com/acch4ck/Chipao-Avei-Loubvii/releases/latest/download/app-release.apk"
}
$versionJsonStr = $versionJsonObj | ConvertTo-Json -Depth 4
Set-Content -Path "version.json" -Value $versionJsonStr -Encoding UTF8
Write-Host "  [OK] Updated version.json" -ForegroundColor DarkGreen

# 5. Build signed release APK
Write-Host ""
Write-Host "Building signed release APK..." -ForegroundColor Cyan
& .\gradlew.bat :app:assembleRelease

if ($LASTEXITCODE -ne 0) {
    Write-Host "Build failed! Please check the errors above." -ForegroundColor Red
    exit 1
}

$apkPath = "app\build\outputs\apk\release\app-release.apk"
if (-not (Test-Path $apkPath)) {
    Write-Host "APK not found at $apkPath!" -ForegroundColor Red
    exit 1
}

$apkItem = Get-Item $apkPath
$apkSizeMB = [math]::Round($apkItem.Length / 1MB, 2)
Write-Host "  [OK] Built signed APK successfully! ($apkSizeMB MB)" -ForegroundColor Green

# 6. Commit and Push to GitHub
Write-Host ""
Write-Host "Pushing updates to GitHub..." -ForegroundColor Cyan
git add app/build.gradle.kts version.json
git commit -m ("Release v" + $newVersionName)
git push origin main
Write-Host "  [OK] Pushed code to GitHub origin main" -ForegroundColor Green

# 7. Open GitHub Release page and highlight APK in Explorer
Write-Host ""
Write-Host "==================================================" -ForegroundColor Cyan
Write-Host "  Final Step: Publish on GitHub!" -ForegroundColor Yellow
Write-Host "==================================================" -ForegroundColor Cyan
Write-Host "1. Tag version: v$newVersionName"
Write-Host "2. Release title: Chipao Avei Loubvu v$newVersionName"
Write-Host "3. Drag and drop app-release.apk into the upload box"
Write-Host "4. Click 'Publish release'"
Write-Host "==================================================" -ForegroundColor Cyan

# Open folder and browser
Start-Process explorer.exe "/select,$apkPath"
Start-Process "https://github.com/acch4ck/Chipao-Avei-Loubvii/releases/new"

Write-Host "Done!" -ForegroundColor Green
