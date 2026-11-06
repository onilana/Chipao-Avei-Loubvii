package com.pnba.data.network

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreSettings
import com.google.firebase.firestore.PersistentCacheSettings

/**
 * Manager for Cloud Firestore with offline persistence enabled by default
 * for dynamic content loading and real-time synchronization.
 */
object FirestoreManager {
    val firestore: FirebaseFirestore by lazy {
        val instance = FirebaseFirestore.getInstance()
        try {
            val settings = FirebaseFirestoreSettings.Builder()
                .setLocalCacheSettings(
                    PersistentCacheSettings.newBuilder().build()
                )
                .build()
            instance.firestoreSettings = settings
        } catch (_: Exception) {
            // If settings already applied or running in test environment
        }
        instance
    }
}
