# Chipao Avei Loubvü — App Release Guide

This document outlines the exact, step-by-step procedure to release a new version of the **Chipao Avei Loubvü** Android app so all users receive the update automatically within the app.

---

## Quick Reference Summary

| Step | Action | Files / Commands Involved |
| :--- | :--- | :--- |
| **1** | Bump version code & name | [`app/build.gradle.kts`](file:///c:/Users/MEC-Oni/Downloads/chipao-avei-loubvü/app/build.gradle.kts) |
| **2** | Update version metadata | [`version.json`](file:///c:/Users/MEC-Oni/Downloads/chipao-avei-loubvü/version.json) |
| **3** | Build signed release APK | `.\gradlew.bat :app:assembleRelease` |
| **4** | Commit & push to GitHub | `git add . && git commit -m "..." && git push origin main` |
| **5** | Attach APK to GitHub Release | [`GitHub Releases`](https://github.com/onilana/Chipao-Avei-Loubvii/releases/new) |

---

## Detailed Step-by-Step Instructions

### Step 1: Bump the App Version

Open [`app/build.gradle.kts`](file:///c:/Users/MEC-Oni/Downloads/chipao-avei-loubvü/app/build.gradle.kts) and look at the `defaultConfig` block (around lines 15–18):

```kotlin
defaultConfig {
    applicationId = "org.pnba.hymnal"
    minSdk = 24
    targetSdk = 35
    versionCode = 4       // <-- Increment this integer by 1 (e.g. 3 -> 4)
    versionName = "1.3"   // <-- Set your new version name (e.g. "1.2" -> "1.3")
    ...
}
```

> [!IMPORTANT]
> `versionCode` must always be a strictly higher number than the previous build (e.g., `1` -> `2` -> `3` -> `4`). Android will reject an update if the new `versionCode` is equal to or lower than the currently installed version.

---

### Step 2: Update `version.json`

Open [`version.json`](file:///c:/Users/MEC-Oni/Downloads/chipao-avei-loubvü/version.json) in the project root:

```json
{
  "latestVersionCode": 4,
  "latestVersionName": "1.3",
  "releaseNotes": "Corrected hymn typos, updated Solfa key signatures, and improved in-app updater.",
  "downloadUrl": "https://github.com/onilana/Chipao-Avei-Loubvii/releases/latest/download/app-release.apk"
}
```

- Set `latestVersionCode` to the **exact same number** as `versionCode` in Step 1.
- Set `latestVersionName` to the **exact same string** as `versionName` in Step 1.
- Write a short summary of changes in `releaseNotes`.
- Keep `downloadUrl` exactly as shown (it automatically points to the latest GitHub release).

---

### Step 3: Build the Signed Release APK

Open PowerShell or Terminal in the project root and run:

```powershell
.\gradlew.bat :app:assembleRelease
```

Once the build finishes (`BUILD SUCCESSFUL`), the signed APK is located at:
```
app\build\outputs\apk\release\app-release.apk
```

---

### Step 4: Commit and Push to GitHub

Stage the updated files, commit them, and push to GitHub:

```powershell
git add app/build.gradle.kts version.json app/src/main/assets/hymns.json
git commit -m "Release v1.3 (Build 4)"
git push origin main
```

---

### Step 5: Publish the Release on GitHub

1. Open your browser and go to:
   👉 **[https://github.com/onilana/Chipao-Avei-Loubvii/releases/new](https://github.com/onilana/Chipao-Avei-Loubvii/releases/new)**
2. In **"Choose a tag"**, type your tag name (e.g., `v1.3`) and click **"Create new tag: v1.3 on main"**.
3. In **"Release title"**, enter:
   `Chipao Avei Loubvü v1.3`
4. In the description box, paste your release notes (e.g., what changes were made).
5. In the box that says **"Attach binaries by dropping them here or selecting them"**:
   - Drag and drop the file:
     `app\build\outputs\apk\release\app-release.apk`
   *(Ensure the uploaded file name is exactly `app-release.apk`)*
6. Ensure **"Set as the latest release"** is checked.
7. Click the green button **"Publish release"**.

---

## What Happens on Users' Phones After You Publish

1. **Automatic Detection**: When users open the app or tap **"Check Updates"** in the About screen, the app reads the new `version.json` from GitHub.
2. **Notification & Prompt**: A notification is sent to the user's notification bar, and the About screen displays:
   `New release v1.3 is available!`
3. **In-App Download**: Users tap **"Download & Install Release (v1.3)"**.
   - The APK downloads directly inside the app with a live progress bar.
   - The user never leaves to an external browser.
4. **Seamless Update**: Once downloaded, the Android package installer pops up over the app. The user taps **"Update"**, and all personal notes, favorites, and recent history are preserved.
5. **Auto-Hide**: When the new version opens, it displays `Version 1.3`, and the download button automatically vanishes.

---

## Optional: 1-Click Automated Script

For maximum convenience, you can also run the bundled release script:

```bat
.\release.bat
```
*(or double-click `release.bat` in File Explorer)*

It prompts you for the new version number, automatically updates both files, compiles the release APK, pushes to GitHub, and opens the GitHub release page ready for upload!
