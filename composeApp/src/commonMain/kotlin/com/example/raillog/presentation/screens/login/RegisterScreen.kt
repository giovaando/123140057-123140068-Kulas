package com.example.raillog.presentation.screens.login

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.raillog.data.local.datastore.DataStoreFactory
import kotlinx.coroutines.launch
import org.koin.compose.koinInject

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegisterScreen(
    onNavigateBack: () -> Unit,
    dataStoreFactory: DataStoreFactory = koinInject()
) {
    val coroutineScope = rememberCoroutineScope()
    val userPreferences = remember { GlobalSessionManager.getPrefs(dataStoreFactory) }

    var name by remember { mutableStateOf("") }
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }

    var errorMsg by remember { mutableStateOf<String?>(null) }
    var isSuccess by remember { mutableStateOf(false) }

    if (isSuccess) {
        AlertDialog(
            onDismissRequest = { },
            title = { Text("Pendaftaran Berhasil!") },
            text = { Text("Akun Staff Gudang berhasil dibuat. Silakan login dengan akun tersebut.") },
            confirmButton = {
                Button(onClick = onNavigateBack) { Text("Ke Halaman Login") }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Buat Akun Staff", fontWeight = FontWeight.Bold, color = Color(0xFF193255)) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) { Icon(Icons.Default.ArrowBack, null, tint = Color(0xFF193255)) }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFFF0F4FA))
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier.fillMaxSize().background(Color(0xFFF0F4FA)).padding(paddingValues).padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("Khusus Staff Gudang", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = Color(0xFF193255))
            Spacer(modifier = Modifier.height(32.dp))

            OutlinedTextField(
                value = name, onValueChange = { name = it },
                label = { Text("Nama Lengkap") }, modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            )
            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = username, onValueChange = { username = it },
                label = { Text("Username") }, modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            )
            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = password, onValueChange = { password = it },
                label = { Text("Password") }, modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                trailingIcon = {
                    IconButton(onClick = { passwordVisible = !passwordVisible }) {
                        Icon(if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff, null)
                    }
                }
            )

            if (errorMsg != null) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(errorMsg!!, color = Color.Red, fontSize = 12.sp)
            }

            Spacer(modifier = Modifier.height(32.dp))

            Button(
                onClick = {
                    if (name.isBlank() || username.isBlank() || password.isBlank()) {
                        errorMsg = "Semua kolom wajib diisi!"
                    } else {
                        coroutineScope.launch {
                            userPreferences.registerStaff(name, username, password)
                            isSuccess = true
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth().height(56.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF193255)),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Daftar Sekarang", fontSize = 16.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}