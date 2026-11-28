param(
    [string]$CommitMessage = ""
)

$ErrorActionPreference = "Stop"

Write-Host "==================================================" -ForegroundColor Cyan
Write-Host "  Chipao Avei Loubvu -- Dual Repo Sync" -ForegroundColor Cyan
Write-Host "==================================================" -ForegroundColor Cyan
Write-Host "  Private Repo : Full source code (including app/src)" -ForegroundColor DarkGray
Write-Host "  Public Repo  : Public files only (app/src safely excluded)" -ForegroundColor DarkGray
Write-Host "==================================================" -ForegroundColor Cyan
Write-Host ""

# Ensure we are on the source branch
$currentBranch = (git branch --show-current).Trim()
if ($currentBranch -ne "source") {
    Write-Host "Switching to 'source' branch..." -ForegroundColor Yellow
    git checkout source
}

# 1. Check for local changes or get commit message
$status = git status --porcelain
$hasUncommitted = [bool]$status

if ($hasUncommitted) {
    if ([string]::IsNullOrWhiteSpace($CommitMessage)) {
        $CommitMessage = Read-Host "Enter commit message"
        if ([string]::IsNullOrWhiteSpace($CommitMessage)) {
            $CommitMessage = "Update project: $(Get-Date -Format 'yyyy-MM-dd HH:mm')"
        }
    }
    Write-Host "Staging and committing local changes..." -ForegroundColor Cyan
    git add -A
    git commit -m $CommitMessage
    Write-Host "  [OK] Local changes committed." -ForegroundColor Green
} else {
    Write-Host "Working tree clean. Syncing latest commits..." -ForegroundColor Cyan
    if ([string]::IsNullOrWhiteSpace($CommitMessage)) {
        $CommitMessage = (git log -1 --pretty=%B).Trim()
    }
}

# 2. Push full code to Private Repository
Write-Host ""
Write-Host "[1/2] Pushing full code (with app/src) to PRIVATE repository..." -ForegroundColor Cyan
git push private source:main
if ($LASTEXITCODE -eq 0) {
    Write-Host "  [OK] Successfully pushed to Private repo!" -ForegroundColor Green
} else {
    Write-Host "  [ERROR] Failed to push to Private repo." -ForegroundColor Red
}

# 3. Create a public commit excluding app/src and private files without touching local working files
Write-Host ""
Write-Host "[2/2] Preparing & pushing to PUBLIC repository (excluding app/src & private files)..." -ForegroundColor Cyan

$tempIndex = [System.IO.Path]::GetTempFileName()
try {
    $env:GIT_INDEX_FILE = $tempIndex
    git read-tree source
    git rm --cached -r --ignore-unmatch app/src local.properties release-key.jks FIREBASE_ANNOUNCEMENT_GUIDE.md sync.bat sync_helper.ps1 release.bat release_helper.ps1 RELEASE_GUIDE.md 2>$null | Out-Null
    $publicTree = (git write-tree).Trim()

    $parentCommit = (git rev-parse refs/heads/main).Trim()
    $parentTree = (git rev-parse "${parentCommit}^{tree}").Trim()

    if ($publicTree -eq $parentTree) {
        Write-Host "  [INFO] Public repo is already up to date with non-source changes." -ForegroundColor Yellow
    } else {
        $newCommit = (git commit-tree $publicTree -p $parentCommit -m $CommitMessage).Trim()
        git update-ref refs/heads/main $newCommit
        git push origin main
        if ($LASTEXITCODE -eq 0) {
            Write-Host "  [OK] Successfully pushed to Public repo (app/src safely excluded)!" -ForegroundColor Green
        } else {
            Write-Host "  [ERROR] Failed to push to Public repo." -ForegroundColor Red
        }
    }
} finally {
    Remove-Item env:GIT_INDEX_FILE -ErrorAction SilentlyContinue
    Remove-Item $tempIndex -ErrorAction SilentlyContinue
}

Write-Host ""
Write-Host "==================================================" -ForegroundColor Cyan
Write-Host "  Sync Complete!" -ForegroundColor Green
Write-Host "==================================================" -ForegroundColor Cyan
