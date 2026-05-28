package com.example.raillog.data.local.datastore

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/**
 * User Preferences menggunakan DataStore
 */
class UserPreferences(
    private val dataStore: DataStore<Preferences>
) {
    // ==================== PREFERENCE KEYS ====================

    private object Keys {
        val DARK_MODE = booleanPreferencesKey("dark_mode")
        val SORT_BY = stringPreferencesKey("sort_by")
        val DEFAULT_CATEGORY = stringPreferencesKey("default_category")
        val SHOW_PREVIEW = booleanPreferencesKey("show_preview")
        val ONBOARDING_COMPLETED = booleanPreferencesKey("onboarding_completed")

        // Kunci baru untuk menyimpan ROLE pengguna (Operator / Manajer / Inspektor)
        val USER_ROLE = stringPreferencesKey("user_role")
    }

    // ==================== DARK MODE ====================

    val isDarkMode: Flow<Boolean> = dataStore.data.map { prefs ->
        prefs[Keys.DARK_MODE] ?: false
    }

    suspend fun setDarkMode(enabled: Boolean) {
        dataStore.edit { prefs ->
            prefs[Keys.DARK_MODE] = enabled
        }
    }

    // ==================== SORT BY ====================

    val sortBy: Flow<String> = dataStore.data.map { prefs ->
        prefs[Keys.SORT_BY] ?: "UPDATED_DESC"
    }

    suspend fun setSortBy(sortBy: String) {
        dataStore.edit { prefs ->
            prefs[Keys.SORT_BY] = sortBy
        }
    }

    // ==================== DEFAULT CATEGORY ====================

    val defaultCategory: Flow<String> = dataStore.data.map { prefs ->
        prefs[Keys.DEFAULT_CATEGORY] ?: "GENERAL"
    }

    suspend fun setDefaultCategory(category: String) {
        dataStore.edit { prefs ->
            prefs[Keys.DEFAULT_CATEGORY] = category
        }
    }

    // ==================== SHOW PREVIEW ====================

    val showPreview: Flow<Boolean> = dataStore.data.map { prefs ->
        prefs[Keys.SHOW_PREVIEW] ?: true
    }

    suspend fun setShowPreview(show: Boolean) {
        dataStore.edit { prefs ->
            prefs[Keys.SHOW_PREVIEW] = show
        }
    }

    // ==================== ONBOARDING ====================

    val isOnboardingCompleted: Flow<Boolean> = dataStore.data.map { prefs ->
        prefs[Keys.ONBOARDING_COMPLETED] ?: false
    }

    suspend fun setOnboardingCompleted() {
        dataStore.edit { prefs ->
            prefs[Keys.ONBOARDING_COMPLETED] = true
        }
    }

    // ==================== USER ROLE (SIMULASI LOGIN) ====================

    /**
     * Membaca role yang sedang aktif.
     * Akan mengembalikan string kosong ("") jika user belum login.
     */
    val userRole: Flow<String> = dataStore.data.map { prefs ->
        prefs[Keys.USER_ROLE] ?: ""
    }

    /**
     * Menyimpan role pengguna yang dipilih dari halaman Login
     */
    suspend fun setUserRole(role: String) {
        dataStore.edit { prefs ->
            prefs[Keys.USER_ROLE] = role
        }
    }
}