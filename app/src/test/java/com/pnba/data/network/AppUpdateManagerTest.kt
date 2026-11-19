package com.pnba.data.network

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class AppUpdateManagerTest {

    @Test
    fun isVersionNewer_detectsNewerMajorVersion() {
        assertTrue(AppUpdateManager.isVersionNewer(latest = "2.0", current = "1.7.1"))
        assertTrue(AppUpdateManager.isVersionNewer(latest = "v2.0", current = "v1.9"))
    }

    @Test
    fun isVersionNewer_detectsNewerMinorVersion() {
        assertTrue(AppUpdateManager.isVersionNewer(latest = "1.8", current = "1.7.1"))
        assertTrue(AppUpdateManager.isVersionNewer(latest = "1.8.0", current = "1.7.5"))
    }

    @Test
    fun isVersionNewer_detectsNewerPatchVersion() {
        assertTrue(AppUpdateManager.isVersionNewer(latest = "1.7.2", current = "1.7.1"))
        assertTrue(AppUpdateManager.isVersionNewer(latest = "v1.7.2", current = "1.7.1"))
    }

    @Test
    fun isVersionNewer_returnsFalseWhenOlderOrEqual() {
        // Equal
        assertFalse(AppUpdateManager.isVersionNewer(latest = "1.7.1", current = "1.7.1"))
        assertFalse(AppUpdateManager.isVersionNewer(latest = "v1.7.1", current = "1.7.1"))

        // Older
        assertFalse(AppUpdateManager.isVersionNewer(latest = "1.7.0", current = "1.7.1"))
        assertFalse(AppUpdateManager.isVersionNewer(latest = "1.6", current = "1.7.1"))
        assertFalse(AppUpdateManager.isVersionNewer(latest = "0.9", current = "1.0"))
    }

    @Test
    fun isVersionNewer_handlesPrefixesAndBlankStrings() {
        assertFalse(AppUpdateManager.isVersionNewer(latest = "", current = "1.0"))
        assertFalse(AppUpdateManager.isVersionNewer(latest = "1.0", current = ""))
        assertTrue(AppUpdateManager.isVersionNewer(latest = "V1.8", current = "v1.7"))
    }
}
