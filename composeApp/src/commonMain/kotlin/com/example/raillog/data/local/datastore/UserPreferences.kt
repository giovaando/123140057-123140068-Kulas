package com.example.raillog.data.local.datastore

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class UserPreferences(
    private val dataStore: DataStore<Preferences>
) {
    private object Keys {
        val DARK_MODE = booleanPreferencesKey("dark_mode")
        val SORT_BY = stringPreferencesKey("sort_by")
        val DEFAULT_CATEGORY = stringPreferencesKey("default_category")
        val SHOW_PREVIEW = booleanPreferencesKey("show_preview")
        val ONBOARDING_COMPLETED = booleanPreferencesKey("onboarding_completed")

        // Akun & Sesi
        val USER_ROLE = stringPreferencesKey("user_role")
        val STAFF_USERNAME = stringPreferencesKey("staff_username")
        val STAFF_PASSWORD = stringPreferencesKey("staff_password")
        val STAFF_NAME = stringPreferencesKey("staff_name")
        val STAFF_ID = stringPreferencesKey("staff_id") // NIP/Employee ID
        val STAFF_PHONE = stringPreferencesKey("staff_phone") // Nomor WA
    }

    val isDarkMode: Flow<Boolean> = dataStore.data.map { prefs -> prefs[Keys.DARK_MODE] ?: false }
    suspend fun setDarkMode(enabled: Boolean) { dataStore.edit { prefs -> prefs[Keys.DARK_MODE] = enabled } }

    // ==================== USER ROLE & SESSION ====================
    val userRole: Flow<String> = dataStore.data.map { prefs -> prefs[Keys.USER_ROLE] ?: "" }

    suspend fun setUserRole(role: String) {
        dataStore.edit { prefs -> prefs[Keys.USER_ROLE] = role }
    }

    suspend fun clearUserSession() {
        dataStore.edit { prefs -> prefs[Keys.USER_ROLE] = "" }
    }

    // ==================== REGISTER STAFF DATA ====================
    val staffUsername: Flow<String> = dataStore.data.map { prefs -> prefs[Keys.STAFF_USERNAME] ?: "" }
    val staffPassword: Flow<String> = dataStore.data.map { prefs -> prefs[Keys.STAFF_PASSWORD] ?: "" }
    val staffName: Flow<String> = dataStore.data.map { prefs -> prefs[Keys.STAFF_NAME] ?: "" }
    val staffId: Flow<String> = dataStore.data.map { prefs -> prefs[Keys.STAFF_ID] ?: "" }
    val staffPhone: Flow<String> = dataStore.data.map { prefs -> prefs[Keys.STAFF_PHONE] ?: "" }

    suspend fun registerStaff(name: String, user: String, pass: String, employeeId: String, phone: String) {
        dataStore.edit { prefs ->
            prefs[Keys.STAFF_NAME] = name
            prefs[Keys.STAFF_USERNAME] = user
            prefs[Keys.STAFF_PASSWORD] = pass
            prefs[Keys.STAFF_ID] = employeeId
            prefs[Keys.STAFF_PHONE] = phone
        }
    }
}
