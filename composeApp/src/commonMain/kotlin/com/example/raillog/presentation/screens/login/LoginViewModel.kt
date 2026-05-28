package com.example.raillog.presentation.screens.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.raillog.data.local.datastore.UserPreferences
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class LoginViewModel(
    private val userPreferences: UserPreferences
) : ViewModel() {

    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()

    fun onEvent(event: LoginEvent) {
        when (event) {
            is LoginEvent.UsernameChanged -> _uiState.value = _uiState.value.copy(username = event.username, errorMessage = null)
            is LoginEvent.PasswordChanged -> _uiState.value = _uiState.value.copy(password = event.password, errorMessage = null)
            is LoginEvent.SubmitLogin -> performLogin(event.onNavigateToHome)
        }
    }

    private fun performLogin(onNavigateToHome: () -> Unit) {
        val currentUsername = _uiState.value.username.trim()
        val currentPassword = _uiState.value.password

        if (currentUsername.isEmpty() || currentPassword.isEmpty()) {
            _uiState.value = _uiState.value.copy(errorMessage = "Username dan Password tidak boleh kosong!")
            return
        }

        _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)

        viewModelScope.launch {
            // Simulasi jeda waktu komunikasi dengan server backend (1.5 detik)
            delay(1500)

            // Logika simulasi Backend untuk menentukan Role
            val role = authenticateBackend(currentUsername, currentPassword)

            if (role != null) {
                // Login sukses, simpan role ke DataStore (Memori HP)
                userPreferences.setUserRole(role)
                _uiState.value = _uiState.value.copy(isLoading = false)
                onNavigateToHome()
            } else {
                // Login gagal
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = "Username atau Password salah!"
                )
            }
        }
    }

    // --- SIMULASI DATABASE BACKEND ---
    private fun authenticateBackend(username: String, pass: String): String? {
        // Semua akun menggunakan password yang sama untuk kemudahan testing
        if (pass != "raillog123") return null

        return when (username.lowercase()) {
            "operator" -> "Operator Gudang"
            "manager" -> "Manajer Logistik"
            "inspektor" -> "Inspektor Teknis"
            else -> null // Username tidak ditemukan
        }
    }
}

// --- STATE & EVENTS ---
data class LoginUiState(
    val username: String = "",
    val password: String = "",
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)

sealed interface LoginEvent {
    data class UsernameChanged(val username: String) : LoginEvent
    data class PasswordChanged(val password: String) : LoginEvent
    data class SubmitLogin(val onNavigateToHome: () -> Unit) : LoginEvent
}