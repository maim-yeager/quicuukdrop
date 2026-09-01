package com.example.data.local

import android.content.Context
import android.os.Build
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "quickdrop_settings")

class PreferencesManager(private val context: Context) {
    private val deviceNameKey = stringPreferencesKey("device_name")
    private val themeModeKey = stringPreferencesKey("theme_mode") // "SYSTEM", "LIGHT", "DARK"
    private val hapticsKey = booleanPreferencesKey("haptic_feedback")
    private val saveLocationKey = stringPreferencesKey("save_location")

    val defaultDeviceName: String
        get() = "${Build.MANUFACTURER.replaceFirstChar { it.uppercase() }} ${Build.MODEL}"

    val deviceName: Flow<String> = context.dataStore.data.map { prefs ->
        prefs[deviceNameKey] ?: defaultDeviceName
    }

    val themeMode: Flow<String> = context.dataStore.data.map { prefs ->
        prefs[themeModeKey] ?: "DARK"
    }

    val hapticsEnabled: Flow<Boolean> = context.dataStore.data.map { prefs ->
        prefs[hapticsKey] ?: true
    }

    val saveLocation: Flow<String> = context.dataStore.data.map { prefs ->
        prefs[saveLocationKey] ?: "Internal Storage/Download/QuickDrop"
    }

    suspend fun setDeviceName(name: String) {
        context.dataStore.edit { prefs ->
            prefs[deviceNameKey] = name
        }
    }

    suspend fun setThemeMode(mode: String) {
        context.dataStore.edit { prefs ->
            prefs[themeModeKey] = mode
        }
    }

    suspend fun setHapticsEnabled(enabled: Boolean) {
        context.dataStore.edit { prefs ->
            prefs[hapticsKey] = enabled
        }
    }

    suspend fun setSaveLocation(location: String) {
        context.dataStore.edit { prefs ->
            prefs[saveLocationKey] = location
        }
    }
}
