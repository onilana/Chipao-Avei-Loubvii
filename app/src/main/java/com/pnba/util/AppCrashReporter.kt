package com.pnba.util

import com.google.firebase.crashlytics.FirebaseCrashlytics

/**
 * Utility wrapper around Firebase Crashlytics for application stability tracking,
 * breadcrumb logging, and non-fatal exception recording.
 */
object AppCrashReporter {
    private val crashlytics: FirebaseCrashlytics
        get() = FirebaseCrashlytics.getInstance()

    /**
     * Log a breadcrumb message to Crashlytics.
     */
    fun log(message: String) {
        try {
            crashlytics.log(message)
        } catch (_: Exception) {}
    }

    /**
     * Record a non-fatal exception.
     */
    fun recordException(throwable: Throwable) {
        try {
            crashlytics.recordException(throwable)
        } catch (_: Exception) {}
    }

    /**
     * Set a custom key-value pair to provide context in crash reports.
     */
    fun setCustomKey(key: String, value: String) {
        try {
            crashlytics.setCustomKey(key, value)
        } catch (_: Exception) {}
    }

    fun setCustomKey(key: String, value: Boolean) {
        try {
            crashlytics.setCustomKey(key, value)
        } catch (_: Exception) {}
    }

    fun setCustomKey(key: String, value: Int) {
        try {
            crashlytics.setCustomKey(key, value)
        } catch (_: Exception) {}
    }

    /**
     * Set a user identifier for crash grouping if applicable.
     */
    fun setUserId(userId: String) {
        try {
            crashlytics.setUserId(userId)
        } catch (_: Exception) {}
    }
}
