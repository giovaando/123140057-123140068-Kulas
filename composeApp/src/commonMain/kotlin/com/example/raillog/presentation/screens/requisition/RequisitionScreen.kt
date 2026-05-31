package com.example.raillog.presentation.screens.requisition

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.koin.compose.viewmodel.koinViewModel // WAJIB ADA UNTUK VIEWMODEL

// --- TEMA WARNA ---
private val RailBlue = Color(0xFF193255)
private val SurfaceGray = Color(0xFFF7F9FC)
private val RailBlueLight = Color(0xFF3B5B85)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RequisitionScreen(
    viewModel: RequisitionViewModel = koinViewModel(), // 1. MENGAMBIL VIEWMODEL
    onNavigateBack: () -> Unit,
    onNavigateToHome: () -> Unit
) {
    // 2. MENGAMBIL STATE SECARA REAKTIF
    val uiState by viewModel.uiState.collectAsState()
    var currentStep by remember { mutableIntStateOf(1) }

    // Jika proses submit selesai, otomatis kembali ke halaman utama
    LaunchedEffect(uiState.submitSuccess) {
        if (uiState.submitSuccess) {
            onNavigateToHome()
        }
    }

    Scaffold(
        containerColor = SurfaceGray,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = if (currentStep == 2) "New Project" else "Requisition",
                        fontWeight = FontWeight.Bold, color = RailBlue, modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.Center
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { if (currentStep > 1) currentStep-- else onNavigateBack() }) {
                        Icon(imageVector = if (currentStep == 1 || currentStep == 2) Icons.Default.Close else Icons.Default.ArrowBack, contentDescription = "Back", tint = RailBlue)
                    }
                },
                actions = { Box(modifier = Modifier.padding(end = 16.dp)) { Icon(Icons.Default.AccountCircle, null, tint = Color.Gray, modifier = Modifier.size(32.dp)) } },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = SurfaceGray)
            )
        },
        bottomBar = {
            BottomActionBar(
                currentStep = currentStep,
                isSubmitting = uiState.isSubmitting, // Tampilkan efek loading jika sedang submit
                onNext = { if (currentStep < 5) currentStep++ },
                onBack = { if (currentStep > 1) currentStep-- },
                onSubmit = { viewModel.submitRequisition() } // 3. MEMANGGIL FUNGSI SUBMIT ASLI
            )
        }
    ) { paddingValues ->
        Column(modifier = Modifier.fillMaxSize().padding(paddingValues)) {
            StepProgressBar(currentStep = currentStep)

            // 4. MELEMPAR UISTATE DAN FUNGSI UPDATE KE DALAM FORM
            Box(modifier = Modifier.fillMaxSize().padding(horizontal = 24.dp)) {
                when (currentStep) {
                    1 -> Step1Identity(uiState) // Identitas bawaan user (Read-only)
                    2 -> Step2ProjectSpecs(uiState, viewModel) // Bisa diketik & dipilih
                    3 -> Step3MaterialCatalog() // (Masih Dummy UI untuk materi katalog sementara)
                    4 -> Step4TechnicalDocs()   // (Masih Dummy UI untuk file upload sementara)
                    5 -> Step5FinalReview(uiState) // Menampilkan hasil ketikan dari Step 2
                }
            }
        }
    }
}

// ==========================================
// KONTEN: STEP 1 - IDENTITAS (Berdasarkan State)
// ==========================================
@Composable
private fun Step1Identity(uiState: RequisitionFormState) {
    LazyColumn(modifier = Modifier.fillMaxSize()) {
        item {
            Text("Who is requesting?", fontSize = 32.sp, fontWeight = FontWeight.Bold, color = RailBlue)
            Spacer(modifier = Modifier.height(8.dp))
            Text("Please confirm your identity details for this material requisition.", color = Color.Gray, fontSize = 14.sp)
            Spacer(modifier = Modifier.height(32.dp))

            FormTextField(label = "Requestor Name", icon = Icons.Default.Person, value = uiState.requestorName, readOnly = true)
            FormTextField(label = "Employee ID", icon = Icons.Default.Badge, value = uiState.employeeId, readOnly = true)
            FormTextField(label = "Department / Unit", icon = Icons.Default.Business, value = uiState.department, isDropdown = true, readOnly = true)
            FormTextField(label = "Date of Request", icon = Icons.Default.CalendarToday, value = uiState.dateOfRequest, isTrailingIcon = true, readOnly = true)
        }
    }
}

// ==========================================
// KONTEN: STEP 2 - PROJECT SPECS (BISA DIKETIK MANUAL)
// ==========================================
@Composable
private fun Step2ProjectSpecs(uiState: RequisitionFormState, viewModel: RequisitionViewModel) {
    LazyColumn(modifier = Modifier.fillMaxSize()) {
        item {
            Text("Select Project Type", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = RailBlue)
            Spacer(modifier = Modifier.height(16.dp))

            // TOMBOL DINAMIS: Klik akan mengubah data projectType
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                ProjectTypeCard(modifier = Modifier.weight(1f), title = "K1 (Executive)", type = "PASSENGER", icon = Icons.Default.AirlineSeatReclineExtra, isSelected = uiState.projectType == "PASSENGER") { viewModel.updateProjectType("PASSENGER") }
                ProjectTypeCard(modifier = Modifier.weight(1f), title = "LRT", type = "URBAN COMMUTE", icon = Icons.Default.DirectionsTransit, isSelected = uiState.projectType == "LRT") { viewModel.updateProjectType("LRT") }
            }
            Spacer(modifier = Modifier.height(16.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                ProjectTypeCard(modifier = Modifier.weight(1f), title = "KRL", type = "SUBURBAN", icon = Icons.Default.Train, isSelected = uiState.projectType == "KRL") { viewModel.updateProjectType("KRL") }
                ProjectTypeCard(modifier = Modifier.weight(1f), title = "High-Speed", type = "INTERCITY", icon = Icons.Default.Speed, isSelected = uiState.projectType == "High-Speed") { viewModel.updateProjectType("High-Speed") }
            }

            Spacer(modifier = Modifier.height(32.dp))
            HorizontalDivider(color = Color.LightGray.copy(alpha = 0.5f))
            Spacer(modifier = Modifier.height(24.dp))

            // KOLOM KETIK DINAMIS
            FormTextField(
                label = "PROJECT CODE", icon = Icons.Default.Numbers,
                value = uiState.projectCode,
                onValueChange = { viewModel.updateProjectCode(it) } // Menyimpan hasil ketikan
            )
            Text("Must be unique per region.", color = Color.Gray, fontSize = 12.sp, modifier = Modifier.padding(bottom = 16.dp))

            FormTextField(
                label = "DESTINATION SITE / WORKSHOP", icon = Icons.Default.Factory,
                value = uiState.destinationSite,
                onValueChange = { viewModel.updateDestinationSite(it) },
                isDropdown = true // Dalam simulasi ini, kita buat textfield agar mudah diketik
            )

            // Tampilkan error jika Project Code kosong saat disubmit
            if (uiState.errorMessage != null) {
                Text(text = uiState.errorMessage, color = Color.Red, fontSize = 12.sp, modifier = Modifier.padding(top = 8.dp))
            }
            Spacer(modifier = Modifier.height(32.dp)) // Padding bawah
        }
    }
}

// ==========================================
// KONTEN: STEP 5 - FINAL REVIEW (Menampilkan data ketikan)
// ==========================================
@Composable
private fun Step5FinalReview(uiState: RequisitionFormState) {
    LazyColumn(modifier = Modifier.fillMaxSize()) {
        item {
            Text("Final Review", fontSize = 28.sp, fontWeight = FontWeight.Bold, color = RailBlue)
            Text("Verify requisition details and provide authorization signature.", color = Color.Gray, fontSize = 14.sp)
            Spacer(modifier = Modifier.height(24.dp))

            Card(colors = CardDefaults.cardColors(containerColor = Color.White), border = BorderStroke(1.dp, Color.LightGray.copy(alpha=0.5f))) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Authorization Signature", color = RailBlue, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(16.dp))
                    Box(modifier = Modifier.fillMaxWidth().height(120.dp).border(1.dp, Color.LightGray, RoundedCornerShape(8.dp)), contentAlignment = Alignment.Center) {
                        Text("Sign Here X", color = Color.LightGray, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
            Spacer(modifier = Modifier.height(16.dp))

            // CARD INI SEKARANG MENAMPILKAN DATA ASLI
            Card(colors = CardDefaults.cardColors(containerColor = Color.White), border = BorderStroke(1.dp, Color.LightGray.copy(alpha=0.5f)), modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("REQUISITION DETAILS", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.Gray)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Project Type: ${uiState.projectType}", fontWeight = FontWeight.Bold, color = RailBlue) // Dinamis
                    Spacer(modifier = Modifier.height(4.dp))
                    Text("Project Code: ${uiState.projectCode.ifEmpty { "N/A" }}", fontWeight = FontWeight.Bold, color = RailBlue) // Dinamis
                    Spacer(modifier = Modifier.height(4.dp))
                    Text("Destination: ${uiState.destinationSite.ifEmpty { "N/A" }}", fontWeight = FontWeight.Bold, color = RailBlue) // Dinamis

                    HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))

                    Text("Requestor: ${uiState.requestorName}", fontWeight = FontWeight.Bold, color = RailBlue) // Dinamis
                }
            }
            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

// ==========================================
// HELPER COMPOSABLES (DIPERBARUI MENJADI REAKTIF)
// ==========================================
@Composable
private fun FormTextField(
    label: String, icon: androidx.compose.ui.graphics.vector.ImageVector,
    value: String,
    onValueChange: (String) -> Unit = {}, // Kunci agar bisa diketik
    isDropdown: Boolean = false, isTrailingIcon: Boolean = false,
    readOnly: Boolean = false // Tombol khusus untuk disable edit (misal nama/id)
) {
    Column(modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp)) {
        Text(label, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.DarkGray)
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange, // Terhubung ke parameter input
            readOnly = readOnly,
            leadingIcon = if (!isTrailingIcon) { { Icon(icon, null, tint = Color.Gray) } } else null,
            trailingIcon = if (isTrailingIcon || isDropdown) { { Icon(if (isDropdown) Icons.Default.ArrowDropDown else icon, null, tint = Color.Gray) } } else null,
            modifier = Modifier.fillMaxWidth(),
            colors = OutlinedTextFieldDefaults.colors(unfocusedContainerColor = Color.White, focusedContainerColor = Color.White)
        )
    }
}

@Composable
private fun ProjectTypeCard(
    modifier: Modifier, title: String, type: String, icon: androidx.compose.ui.graphics.vector.ImageVector,
    isSelected: Boolean,
    onClick: () -> Unit // Kunci agar tombol tipe kereta bisa diklik
) {
    Card(
        modifier = modifier.height(100.dp),
        onClick = onClick,
        colors = CardDefaults.cardColors(containerColor = if (isSelected) RailBlueLight.copy(alpha = 0.1f) else Color.White),
        border = BorderStroke(if (isSelected) 2.dp else 1.dp, if (isSelected) RailBlue else Color.LightGray)
    ) {
        Column(modifier = Modifier.padding(12.dp).fillMaxSize(), verticalArrangement = Arrangement.SpaceBetween) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Icon(icon, null, tint = if (isSelected) RailBlue else Color.Gray)
                Icon(if (isSelected) Icons.Default.RadioButtonChecked else Icons.Default.RadioButtonUnchecked, null, tint = if (isSelected) RailBlue else Color.LightGray)
            }
            Column {
                Text(type, fontSize = 10.sp, color = if (isSelected) RailBlue else Color.Gray, fontWeight = FontWeight.Bold)
                Text(title, fontSize = 14.sp, color = RailBlue, fontWeight = FontWeight.Bold)
            }
        }
    }
}

// --- BAGIAN INI SAMA DENGAN SEBELUMNYA ---
@Composable
private fun BottomActionBar(currentStep: Int, isSubmitting: Boolean, onNext: () -> Unit, onBack: () -> Unit, onSubmit: () -> Unit) {
    Surface(color = SurfaceGray, shadowElevation = 16.dp, modifier = Modifier.fillMaxWidth()) {
        Row(modifier = Modifier.fillMaxWidth().navigationBarsPadding().padding(16.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            if (currentStep in 2..4) {
                OutlinedButton(onClick = onBack, modifier = Modifier.weight(1f).height(50.dp), shape = RoundedCornerShape(8.dp), border = BorderStroke(1.dp, Color.LightGray)) {
                    Text("Back", color = RailBlue, fontWeight = FontWeight.Bold)
                }
                Spacer(modifier = Modifier.width(16.dp))
            }
            Button(
                onClick = if (currentStep == 5) onSubmit else onNext,
                modifier = Modifier.weight(2f).height(50.dp),
                enabled = !isSubmitting,
                colors = ButtonDefaults.buttonColors(containerColor = RailBlue), shape = RoundedCornerShape(8.dp)
            ) {
                if (isSubmitting) {
                    CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                } else {
                    Text(text = if (currentStep == 5) "Submit Request" else "Next Step", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    if (currentStep < 5) { Spacer(modifier = Modifier.width(8.dp)); Icon(Icons.Default.ArrowForward, null, modifier = Modifier.size(18.dp)) }
                }
            }
        }
    }
}

@Composable
private fun StepProgressBar(currentStep: Int) {
    Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp, vertical = 16.dp)) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text("Step $currentStep of 5", color = RailBlue, fontWeight = FontWeight.Bold, fontSize = 12.sp)
            Text(when(currentStep) { 1 -> "Requestor Identity"; 2 -> "Project Specifications"; 3 -> "Material Selection"; 4 -> "Technical Documentation"; else -> "Final Review" }, color = RailBlue, fontWeight = FontWeight.Bold, fontSize = 12.sp)
        }
        Spacer(modifier = Modifier.height(8.dp))
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            for (i in 1..5) { Box(modifier = Modifier.weight(1f).height(4.dp).background(if (i <= currentStep) RailBlue else Color.LightGray.copy(alpha = 0.5f), RoundedCornerShape(2.dp))) }
        }
    }
}

// Dummy Tabs (Dibiarkan untuk estetika visual saat presentasi Step 3 dan 4)
@Composable private fun Step3MaterialCatalog() { Box(modifier=Modifier.fillMaxSize(), contentAlignment=Alignment.Center){ Text("Material Catalog UI (Tahap Selanjutnya)") } }
@Composable private fun Step4TechnicalDocs() { Box(modifier=Modifier.fillMaxSize(), contentAlignment=Alignment.Center){ Text("Blueprint Upload UI (Tahap Selanjutnya)") } }