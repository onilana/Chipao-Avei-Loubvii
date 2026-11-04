package com.pnba.data.network

/**
 * Configuration for in-app direct feedback submissions via Google Apps Script Web App.
 *
 * To deploy your Google Apps Script:
 * 1. Visit https://script.google.com/ and create a new project.
 * 2. Paste the provided doPost(e) script.
 * 3. Click "Deploy" > "New deployment" > Select type "Web app".
 * 4. Set "Execute as": "Me".
 * 5. Set "Who has access": "Anyone".
 * 6. Copy the generated Web App URL and paste it below into [WEBHOOK_URL].
 */
object FeedbackConfig {
    const val WEBHOOK_URL: String = "https://script.google.com/macros/s/AKfycbw1TJpjglSZPGMXiqdEFUj4IfVDyLbIxix7Ai28JfPk3FvyaFZHHhxjR1O8FWSvxCNung/exec"
    const val UPDATE_CHECK_URL: String = "https://raw.githubusercontent.com/onilana/Chipao-Avei-Loubvii/main/version.json"
}
