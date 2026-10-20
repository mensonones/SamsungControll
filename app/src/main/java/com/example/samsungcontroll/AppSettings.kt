package com.example.samsungcontroll

import android.content.Context
import androidx.core.content.edit

/** User-facing preferences (not security-sensitive, unlike [SecureTvPreferences]). */
class AppSettings(context: Context) {
    private val prefs = context.applicationContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    var hapticsEnabled: Boolean
        get() = prefs.getBoolean(KEY_HAPTICS, true)
        set(value) = prefs.edit { putBoolean(KEY_HAPTICS, value) }

    /** When true, the phone's volume buttons control the TV while connected. */
    var volumeKeysEnabled: Boolean
        get() = prefs.getBoolean(KEY_VOLUME_KEYS, true)
        set(value) = prefs.edit { putBoolean(KEY_VOLUME_KEYS, value) }

    /** Minutes without interaction before the connection is closed; 0 keeps it open. */
    var idleTimeoutMinutes: Int
        get() = prefs.getInt(KEY_IDLE_TIMEOUT, 0)
        set(value) = prefs.edit { putInt(KEY_IDLE_TIMEOUT, value) }

    companion object {
        val IDLE_TIMEOUT_OPTIONS = listOf(0, 15, 30, 60)
        private const val PREFS_NAME = "app_settings"
        private const val KEY_HAPTICS = "haptics_enabled"
        private const val KEY_VOLUME_KEYS = "volume_keys_enabled"
        private const val KEY_IDLE_TIMEOUT = "idle_timeout_minutes"
    }
}
