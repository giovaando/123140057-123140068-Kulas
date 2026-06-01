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
    }

    val isDarkMode: Flow<Boolean> = dataStore.data.map { prefs -> prefs[Keys.DARK_MODE] ?: false }
    suspend fun setDarkMode(enabled: Boolean) { dataStore.edit { prefs -> prefs[Keys.DARK_MODE] = enabled } }

    val sortBy: Flow<String> = dataStore.data.map { prefs -> prefs[Keys.SORT_BY] ?: "UPDATED_DESC" }
    suspend fun setSortBy(sortBy: String) { dataStore.edit { prefs -> prefs[Keys.SORT_BY] = sortBy } }

    val defaultCategory: Flow<String> = dataStore.data.map { prefs -> prefs[Keys.DEFAULT_CATEGORY] ?: "GENERAL" }
    suspend fun setDefaultCategory(category: String) { dataStore.edit { prefs -> prefs[Keys.DEFAULT_CATEGORY] = category } }

    val showPreview: Flow<Boolean> = dataStore.data.map { prefs -> prefs[Keys.SHOW_PREVIEW] ?: true }
    suspend fun setShowPreview(show: Boolean) { dataStore.edit { prefs -> prefs[Keys.SHOW_PREVIEW] = show } }

    val isOnboardingCompleted: Flow<Boolean> = dataStore.data.map { prefs -> prefs[Keys.ONBOARDING_COMPLETED] ?: false }
    suspend fun setOnboardingCompleted() { dataStore.edit { prefs -> prefs[Keys.ONBOARDING_COMPLETED] = true } }

    // ==================== USER ROLE & SESSION ====================
    val userRole: Flow<String> = dataStore.data.map { prefs -> prefs[Keys.USER_ROLE] ?: "" }

    suspend fun setUserRole(role: String) {
        dataStore.edit { prefs -> prefs[Keys.USER_ROLE] = role }
    }

    suspend fun clearUserSession() {
        dataStore.edit { prefs -> prefs[Keys.USER_ROLE] = "" }
    }

    // ==================== REGISTER STAFF ====================
    val staffUsername: Flow<String> = dataStore.data.map { prefs -> prefs[Keys.STAFF_USERNAME] ?: "" }
    val staffPassword: Flow<String> = dataStore.data.map { prefs -> prefs[Keys.STAFF_PASSWORD] ?: "" }
    val staffName: Flow<String> = dataStore.data.map { prefs -> prefs[Keys.STAFF_NAME] ?: "" }

    suspend fun registerStaff(name: String, user: String, pass: String) {
        dataStore.edit { prefs ->
            prefs[Keys.STAFF_NAME] = name
            prefs[Keys.STAFF_USERNAME] = user
            prefs[Keys.STAFF_PASSWORD] = pass
        }
    }
}