# 📖 Chipao Avei Loubvü [POULA HYMN]

[![Platform](https://img.shields.io/badge/Platform-Android-3DDC84.svg?logo=android&logoColor=white)](https://www.android.com/)
[![Language](https://img.shields.io/badge/Language-Kotlin%202.0-7F52FF.svg?logo=kotlin&logoColor=white)](https://kotlinlang.org/)
[![UI Toolkit](https://img.shields.io/badge/UI-Jetpack%20Compose%20%7C%20Material%203-4285F4.svg?logo=jetpackcompose&logoColor=white)](https://developer.android.com/jetpack/compose)
[![Min SDK](https://img.shields.io/badge/Min%20SDK-24%20(Android%207.0)-informational.svg)](https://developer.android.com)
[![Target SDK](https://img.shields.io/badge/Target%20SDK-35%20(Android%2015)-blue.svg)](https://developer.android.com)
[![License](https://img.shields.io/badge/License-Proprietary%20%2F%20PNBA-lightgrey.svg)](#-license--attribution)
[![Latest Release](https://img.shields.io/github/v/release/onilana/Chipao-Avei-Loubvii?color=success&logo=github)](https://github.com/onilana/Chipao-Avei-Loubvii/releases/latest)

> The official digital hymnal companion for the **Poumai Naga Baptist Association (PNBA)**, designed for church worship services, choir rehearsals, family devotions, and personal Bible study.

---

## 🌟 Key Features

### 🎶 Complete Hymnal Catalog (1–520)
* **All 520 Hymns Included**: Every hymn preserved with Poula lyrics, stanzas, choruses, and choral part labels (Unison, Parts, Alto, Tenor, Bass).
* **Solfa & Musical Metadata**: Key signatures, time signatures, meter, tune references, author, and composer details displayed for every hymn.
* **Choir Pitch Pipe**: Built-in tone generator to chime starting pitches and choir keynote cues directly inside the app.

### 🔍 Swift & Typo-Tolerant Search
* **Deep Index Search**: Query instantly across hymn numbers (`#1` to `#520`), Poula titles, English hymn titles, stanzas, tunes, and authors.
* **Smart Filter Chips**: Filter search results by Title, Verse, Author, Tune, or Theological Theme.
* **Sticky Alphabetical Scrubber**: Jump through songs instantly by first letter in Page View.

### 📱 Responsive & Adaptive Design
* **Portrait & Landscape Modes**: Specially tailored landscape view that automatically streamlines the header and navigation bar, freeing up screen real estate for choir stands and podium tablets.
* **Adaptive Number Grid**: Fast, high-density 5-column (portrait) and 9-column (landscape) quick-jump keypad.
* **Topical Index (1–31)**: Browse hymns organized into all 31 canonical topics from the printed hymnal (Praise & Adoration, Worship, Faith & Trust, Dedication & Church Foundation Day, and more).

### 🛠️ Personalization & Reading Tools
* **Reader Themes**: Choose from Light, Sepia/Parchment, and AMOLED Pure Dark mode.
* **Custom Typography**: Adjust font size dynamically and toggle Serif / Sans-Serif typefaces.
* **Choir & Personal Notes**: Attach personal reflections, tune annotations, and choir reminders to specific hymns.
* **Favorites & Recents**: Bookmark hymns and revisit recently sung songs.
* **Keep Screen Awake**: Optional wake-lock prevents the display from timing out while reading or performing on stage.

### 🔄 In-App Updates & Cloud Readiness
* **Built-in GitHub Updater**: Automatically notifies users of new hymnal revisions and downloads/installs APK updates without third-party app stores.
* **Firebase Architecture**: Powered by Cloud Firestore (offline persistence), Firebase Crashlytics (stability & crash monitoring), and Remote Config.

---

## 🏗️ Architecture & Tech Stack

* **Language**: [Kotlin](https://kotlinlang.org/) (JVM target 1.8 / Kotlin 2.0.21)
* **UI**: [Jetpack Compose](https://developer.android.com/jetpack/compose) with Material Design 3
* **State Management**: Android Architecture Components (`ViewModel`, `StateFlow`, `Coroutines`)
* **Audio Synthesis**: Native Android AudioTrack PCM pitch chime generation
* **Backend & Monitoring**:
  * **Firebase Crashlytics**: Crash diagnostics & ANR telemetry
  * **Firebase Remote Config**: Dynamic parameters & operational killswitches
  * **Cloud Firestore**: Dynamic content sync with offline caching
* **Distribution & CI/CD**: Direct APK releases via GitHub Releases with semantic versioning

---

## 📲 Download & Install

The latest signed Android APK is published on [GitHub Releases](https://github.com/onilana/Chipao-Avei-Loubvii/releases/latest):

1. Open the [**Latest Release**](https://github.com/onilana/Chipao-Avei-Loubvii/releases/latest) page on your Android device.
2. Download **`app-release.apk`** from the **Assets** section.
3. Open the downloaded file and tap **Install** (or **Update** if upgrading from an earlier version).

> [!NOTE]
> Once installed, the app automatically checks [`version.json`](version.json) and notifies you whenever a newer version is available.

---

## 📁 Repository Structure

This public repository hosts official APK releases, [`version.json`](version.json) update metadata, and hymn builder utilities. The Android application source code (`app/src`), signing keys, and internal release scripts are maintained in a separate private repository.

---

## 🤝 Community & Support

* **Publisher**: Poumai Naga Baptist Association (PNBA)
* **Maintainer**: [@onilana](https://github.com/onilana)
* **Issues & Bug Reports**: Submit an issue via [GitHub Issues](https://github.com/onilana/Chipao-Avei-Loubvii/issues) or use the in-app feedback tool in the **About** screen.

---

## 📄 License & Attribution

Hymn texts, Solfa arrangements, and translations are copyright © **Poumai Naga Baptist Association (PNBA)**.  
Application software and digital infrastructure maintained by Onisimus Lana. All rights reserved.
