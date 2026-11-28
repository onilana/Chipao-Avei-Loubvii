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

# 1. Sync with GitHub & read current version from app/build.gradle.kts
Write-Host "Checking for remote updates..." -ForegroundColor Cyan
git pull origin main
if ($LASTEXITCODE -ne 0) {
    Write-Host "Warning: Could not sync with remote GitHub repository. Continuing with local files..." -ForegroundColor Yellow
}
Write-Host ""

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

# Automatically increment internal Android versionCode only if version changed
$newVersionCode = if ($newVersionName -eq $currentName) { $currentCode } else { $currentCode + 1 }

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
    downloadUrl       = "https://github.com/onilana/Chipao-Avei-Loubvii/releases/latest/download/app-release.apk"
}
$versionJsonStr = $versionJsonObj | ConvertTo-Json -Depth 4
Set-Content -Path "version.json" -Value $versionJsonStr -Encoding UTF8
Write-Host "  [OK] Updated version.json" -ForegroundColor DarkGreen

# 5. Build signed release APK and App Bundle (.aab)
Write-Host ""
Write-Host "Building signed release APK & App Bundle (.aab)..." -ForegroundColor Cyan
& .\gradlew.bat :app:assembleRelease :app:bundleRelease

if ($LASTEXITCODE -ne 0) {
    Write-Host "Build failed! Please check the errors above." -ForegroundColor Red
    exit 1
}

$apkPath = "app\build\outputs\apk\release\app-release.apk"
$aabPath = "app\build\outputs\bundle\release\app-release.aab"

if (-not (Test-Path $apkPath)) {
    Write-Host "APK not found at $apkPath!" -ForegroundColor Red
    exit 1
}

$apkItem = Get-Item $apkPath
$apkSizeMB = [math]::Round($apkItem.Length / 1MB, 2)
Write-Host "  [OK] Built signed APK successfully! ($apkSizeMB MB)" -ForegroundColor Green

if (Test-Path $aabPath) {
    $aabItem = Get-Item $aabPath
    $aabSizeMB = [math]::Round($aabItem.Length / 1MB, 2)
    Write-Host "  [OK] Built signed App Bundle (.aab) successfully! ($aabSizeMB MB)" -ForegroundColor Green
}

# 6. Commit and Push to Private & Public Repositories
Write-Host ""
Write-Host "Saving release commit..." -ForegroundColor Cyan
git add -A
git commit -m ("Release v" + $newVersionName)

# 6a. Push full code to Private repository
$remotes = (git remote) -split "`r?`n"
if ($remotes -contains "private") {
    Write-Host "Backing up full release code to PRIVATE repository..." -ForegroundColor Cyan
    git push private source:main
    if ($LASTEXITCODE -eq 0) {
        Write-Host "  [OK] Private repository updated!" -ForegroundColor Green
    }
}

# 6b. Safely push release metadata to Public repository (excluding app/src)
Write-Host "Pushing release metadata to PUBLIC repository (excluding app/src)..." -ForegroundColor Cyan
$tempIndex = [System.IO.Path]::GetTempFileName()
try {
    $env:GIT_INDEX_FILE = $tempIndex
    git read-tree source
    git rm --cached -r app/src 2>$null | Out-Null
    $publicTree = (git write-tree).Trim()
    $parentCommit = (git rev-parse refs/heads/main).Trim()
    $newCommit = (git commit-tree $publicTree -p $parentCommit -m ("Release v" + $newVersionName)).Trim()
    git update-ref refs/heads/main $newCommit
    git push origin main
    if ($LASTEXITCODE -eq 0) {
        Write-Host "  [OK] Public repository updated with release metadata!" -ForegroundColor Green
    } else {
        Write-Host "  [ERROR] Failed to push to Public repo." -ForegroundColor Red
    }
} finally {
    Remove-Item env:GIT_INDEX_FILE -ErrorAction SilentlyContinue
    Remove-Item $tempIndex -ErrorAction SilentlyContinue
}

# 7. Open GitHub Release page and highlight artifacts in Explorer
Write-Host ""
Write-Host "==================================================" -ForegroundColor Cyan
Write-Host "  Final Step: Publish Release!" -ForegroundColor Yellow
Write-Host "==================================================" -ForegroundColor Cyan
Write-Host "1. Tag version: v$newVersionName"
Write-Host "2. Release title: Chipao Avei Loubvu v$newVersionName"
Write-Host "3. Upload artifacts:"
Write-Host "   - app-release.apk  (GitHub Release / In-App Direct Updater)"
Write-Host "   - app-release.aab  (Google Play Console / App Bundle)"
Write-Host "4. Click 'Publish release' on GitHub"
Write-Host "==================================================" -ForegroundColor Cyan

# Open folder and browser
Start-Process explorer.exe "/select,$apkPath"
if (Test-Path $aabPath) {
    $aabFolder = (Get-Item $aabPath).DirectoryName
    Start-Process explorer.exe $aabFolder
}
Start-Process "https://github.com/onilana/Chipao-Avei-Loubvii/releases/new"

Write-Host "Done!" -ForegroundColor Green
