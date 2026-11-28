# 📢 Firebase Remote Config Announcement Guide (Private)

> **CONFIDENTIAL / ADMIN ONLY**: This document contains private instructions for managing broadcast popups and announcements via Firebase Remote Config for **Chipao Avei Loubvü**. Keep this file local and do not share administrative parameters publicly.

---

## 🧭 How It Works

The app automatically contacts **Firebase Remote Config** on launch and fetches active configuration parameters. If an announcement is enabled and has a valid ID, the app displays a Material 3 popup dialog to the user.

* **Single Dismissal Tracking**: When a user taps **Dismiss**, the app saves `announcement_id` to their device's private `SharedPreferences`. The user will **not** be repeatedly prompted on subsequent launches.
* **Broadcasting a New Announcement**: Whenever you change `announcement_id` in Firebase (e.g. from `conf_2026_01` to `conf_2026_02`), the app detects it as a new announcement and shows the popup to all users again.
* **Instant Kill-Switch**: Setting `announcement_enabled` to `false` stops the popup immediately across all devices without needing to delete your announcement text.

---

## 🔑 Firebase Parameter Reference

Configure these exact parameter keys in the [Firebase Console](https://console.firebase.google.com/):

| Parameter Key | Data Type | Default | Description |
| :--- | :---: | :---: | :--- |
| `announcement_enabled` | **Boolean** | `false` | Master toggle. Set to `true` to show the popup, `false` to hide it. |
| `announcement_id` | **String** | `""` | Unique identifier (e.g., `event_2026_11_v1`). Change this string whenever you want to send a **new** announcement. |
| `announcement_title` | **String** | `Announcement` | Header title of the popup dialog. |
| `announcement_text` | **String** | `""` | Main body message displayed to users. Supports multi-line text. |
| `announcement_url` | **String** | `""` | *(Optional)* URL for the **"Learn More"** button. If empty, the button is hidden. |

---

## 📋 Step-by-Step Instructions

### 1. Broadcasting an Announcement

1. Go to the [Firebase Console](https://console.firebase.google.com/) and open the **Chipao Avei Loubvü** project.
2. In the left navigation panel, scroll to **Engage** (or **Run**) and click **Remote Config**.
3. Locate or add the parameters:
   * **`announcement_enabled`**: Set to `true`.
   * **`announcement_id`**: Set a unique ID for this notice (e.g., `youth_camp_2026`).
   * **`announcement_title`**: Set your title (e.g., `Annual Youth Camp 2026`).
   * **`announcement_text`**: Type your announcement message.
   * **`announcement_url`**: *(Optional)* Add link (e.g., `https://pnba.org/register`), or leave blank.
4. Click **Save** in the bottom right corner of the edit drawer.
5. Click **Publish changes** in the top banner.

---

### 2. Ending an Announcement

When the event has passed or you want to stop showing the popup:

1. Open **Remote Config** in the Firebase Console.
2. Edit **`announcement_enabled`** and set it to **`false`**.
3. Click **Save** and **Publish changes**.

*(You do not need to delete the text — keeping it allows you to reuse or reference previous messages).*

---

### 3. Sending the Next Announcement

When you are ready to post a new announcement in the future:

1. Open **Remote Config**.
2. Change **`announcement_id`** to a new value (e.g., from `youth_camp_2026` to `easter_service_2027`).
3. Update **`announcement_title`**, **`announcement_text`**, and optionally **`announcement_url`**.
4. Set **`announcement_enabled`** to **`true`**.
5. Click **Save** and **Publish changes**.

> [!IMPORTANT]
> Always change `announcement_id` when posting a new announcement. If you reuse an old ID, users who already dismissed the previous announcement with that same ID will not see the new message.

---

## 💡 Example Templates

### Scenario A: Important Church Service Notice
* `announcement_enabled`: `true`
* `announcement_id`: `special_service_nov26`
* `announcement_title`: `Combined Worship Notice`
* `announcement_text`: `Please note that Sunday Worship on Nov 22 will begin at 9:30 AM instead of 10:00 AM due to the PNBA leadership ordination service.`
* `announcement_url`: `""`

### Scenario B: Conference Registration with Web Link
* `announcement_enabled`: `true`
* `announcement_id`: `pnba_conf_2027`
* `announcement_title`: `PNBA General Conference 2027`
* `announcement_text`: `Delegate registration is now open for the 2027 General Conference. Click Learn More below to view schedules and submit delegate details.`
* `announcement_url`: `https://pnba.org/conference-2027`

---

## 🛠️ Testing & Troubleshooting

* **Fetch Interval**: In `DEBUG` mode, the app uses a fetch interval of `0` seconds (instant updates upon restart). In `RELEASE` mode, it uses a standard `3600` second (1 hour) cache to preserve battery and network quotas.
* **Resetting Dismissed State for Testing**: To see a dismissed popup again during testing without changing Firebase values, clear the app data or storage in Android Settings: **Settings ➔ Apps ➔ Chipao Avei Loubvü ➔ Storage ➔ Clear Storage**.
