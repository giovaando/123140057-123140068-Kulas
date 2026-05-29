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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// --- TEMA WARNA ---
private val RailBlue = Color(0xFF193255)
private val SurfaceGray = Color(0xFFF7F9FC)
private val RailBlueLight = Color(0xFF3B5B85)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RequisitionScreen(
    onNavigateBack: () -> Unit,
    onNavigateToHome: () -> Unit // Dipanggil setelah Submit sukses
) {
    // STATE MACHINE: Mengingat kita sedang berada di langkah ke berapa (1 - 5)
    var currentStep by remember { mutableIntStateOf(1) }

    Scaffold(
        containerColor = SurfaceGray,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = if (currentStep == 2) "New Project" else "Requisition",
                        fontWeight = FontWeight.Bold,
                        color = RailBlue,
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = TextAlign.Center
                    )
                },
                navigationIcon = {
                    IconButton(onClick = {
                        if (currentStep > 1) currentStep-- else onNavigateBack()
                    }) {
                        Icon(
                            imageVector = if (currentStep == 1 || currentStep == 2) Icons.Default.Close else Icons.Default.ArrowBack,
                            contentDescription = "Back",
                            tint = RailBlue
                        )
                    }
                },
                actions = {
                    Box(modifier = Modifier.padding(end = 16.dp)) {
                        Icon(Icons.Default.AccountCircle, null, tint = Color.Gray, modifier = Modifier.size(32.dp))
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = SurfaceGray)
            )
        },
        bottomBar = {
            // Logika Tombol Bawah Dinamis
            BottomActionBar(
                currentStep = currentStep,
                onNext = { if (currentStep < 5) currentStep++ },
                onBack = { if (currentStep > 1) currentStep-- },
                onSubmit = { onNavigateToHome() }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // KOMPONEN: Indikator Progres (Garis Biru di atas)
            StepProgressBar(currentStep = currentStep)

            // STATE SWITCHER: Mengganti konten layar berdasarkan angka currentStep
            Box(modifier = Modifier.fillMaxSize().padding(horizontal = 24.dp)) {
                when (currentStep) {
                    1 -> Step1Identity()
                    2 -> Step2ProjectSpecs()
                    3 -> Step3MaterialCatalog()
                    4 -> Step4TechnicalDocs()
                    5 -> Step5FinalReview()
                }
            }
        }
    }
}

// ==========================================
// LOGIKA NAVIGASI BAWAH
// ==========================================
@Composable
private fun BottomActionBar(currentStep: Int, onNext: () -> Unit, onBack: () -> Unit, onSubmit: () -> Unit) {
    Surface(
        color = SurfaceGray,
        shadowElevation = 16.dp,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (currentStep > 1 && currentStep < 5) {
                OutlinedButton(
                    onClick = onBack,
                    modifier = Modifier.weight(1f).height(50.dp),
                    shape = RoundedCornerShape(8.dp),
                    border = BorderStroke(1.dp, Color.LightGray)
                ) {
                    Text("Back", color = RailBlue, fontWeight = FontWeight.Bold)
                }
                Spacer(modifier = Modifier.width(16.dp))
            }

            Button(
                onClick = if (currentStep == 5) onSubmit else onNext,
                modifier = Modifier.weight(2f).height(50.dp),
                colors = ButtonDefaults.buttonColors(containerColor = RailBlue),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text(
                    text = if (currentStep == 5) "Submit Request" else "Next Step",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
                if (currentStep < 5) {
                    Spacer(modifier = Modifier.width(8.dp))
                    Icon(Icons.Default.ArrowForward, null, modifier = Modifier.size(18.dp))
                }
            }
        }
    }
}

// ==========================================
// INDIKATOR PROGRES (STEP 1 OF 5)
// ==========================================
@Composable
private fun StepProgressBar(currentStep: Int) {
    Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp, vertical = 16.dp)) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text("Step $currentStep of 5", color = RailBlue, fontWeight = FontWeight.Bold, fontSize = 12.sp)
            Text(
                text = when(currentStep) {
                    1 -> "Requestor Identity"; 2 -> "Project Specifications"; 3 -> "Material Selection"; 4 -> "Technical Documentation"; else -> "Final Review"
                },
                color = RailBlue, fontWeight = FontWeight.Bold, fontSize = 12.sp
            )
        }
        Spacer(modifier = Modifier.height(8.dp))
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            for (i in 1..5) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(4.dp)
                        .background(if (i <= currentStep) RailBlue else Color.LightGray.copy(alpha = 0.5f), RoundedCornerShape(2.dp))
                )
            }
        }
    }
}

// ==========================================
// KONTEN: STEP 1 - IDENTITAS
// ==========================================
@Composable
private fun Step1Identity() {
    LazyColumn(modifier = Modifier.fillMaxSize()) {
        item {
            Text("Who is requesting?", fontSize = 32.sp, fontWeight = FontWeight.Bold, color = RailBlue)
            Spacer(modifier = Modifier.height(8.dp))
            Text("Please confirm your identity details for this material requisition.", color = Color.Gray, fontSize = 14.sp)
            Spacer(modifier = Modifier.height(32.dp))

            FormTextField(label = "Requestor Name", icon = Icons.Default.Person, value = "Giovan Lado")
            FormTextField(label = "Employee ID", icon = Icons.Default.Badge, value = "RLN-88392")
            FormTextField(label = "Department / Unit", icon = Icons.Default.Business, value = "Rolling Stock Maintenance", isDropdown = true)
            FormTextField(label = "Date of Request", icon = Icons.Default.CalendarToday, value = "29/05/2026", isTrailingIcon = true)
        }
    }
}

// ==========================================
// KONTEN: STEP 2 - PROJECT SPECS
// ==========================================
@Composable
private fun Step2ProjectSpecs() {
    LazyColumn(modifier = Modifier.fillMaxSize()) {
        item {
            Text("Select Project Type", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = RailBlue)
            Spacer(modifier = Modifier.height(16.dp))

            // Grid Simulasi (2x2)
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                ProjectTypeCard(modifier = Modifier.weight(1f), title = "K1 (Executive)", type = "PASSENGER", icon = Icons.Default.AirlineSeatReclineExtra, isSelected = false)
                ProjectTypeCard(modifier = Modifier.weight(1f), title = "LRT", type = "URBAN COMMUTE", icon = Icons.Default.DirectionsTransit, isSelected = true)
            }
            Spacer(modifier = Modifier.height(16.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                ProjectTypeCard(modifier = Modifier.weight(1f), title = "KRL", type = "SUBURBAN", icon = Icons.Default.Train, isSelected = false)
                ProjectTypeCard(modifier = Modifier.weight(1f), title = "High-Speed", type = "INTERCITY", icon = Icons.Default.Speed, isSelected = false)
            }

            Spacer(modifier = Modifier.height(32.dp))
            Divider(color = Color.LightGray.copy(alpha = 0.5f))
            Spacer(modifier = Modifier.height(24.dp))

            FormTextField(label = "PROJECT CODE", icon = Icons.Default.Numbers, value = "LRT-JABO-24A")
            Text("Must be unique per region.", color = Color.Gray, fontSize = 12.sp, modifier = Modifier.padding(bottom = 16.dp))
            FormTextField(label = "DESTINATION SITE / WORKSHOP", icon = Icons.Default.Factory, value = "Select a facility...", isDropdown = true)
        }
    }
}

// ==========================================
// KONTEN: STEP 3 - MATERIAL CATALOG
// ==========================================
@Composable
private fun Step3MaterialCatalog() {
    Column(modifier = Modifier.fillMaxSize()) {
        OutlinedTextField(
            value = "", onValueChange = {},
            modifier = Modifier.fillMaxWidth(),
            placeholder = { Text("Search material name or ID...") },
            leadingIcon = { Icon(Icons.Default.Search, null) },
            shape = RoundedCornerShape(8.dp),
            colors = OutlinedTextFieldDefaults.colors(unfocusedContainerColor = Color.White)
        )
        Spacer(modifier = Modifier.height(16.dp))
        // Tab Kategori
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            listOf("All", "Infrastructure", "Spare Parts").forEachIndexed { index, text ->
                Box(modifier = Modifier.background(if(index==0) RailBlue else Color.White, RoundedCornerShape(4.dp)).border(1.dp, if(index==0) RailBlue else Color.LightGray, RoundedCornerShape(4.dp)).padding(horizontal = 12.dp, vertical = 6.dp)) {
                    Text(text, color = if(index==0) Color.White else RailBlue, fontSize = 12.sp)
                }
            }
        }
        Spacer(modifier = Modifier.height(16.dp))

        // List Item
        LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            item { CatalogItemCard("Bantalan Beton Wika", "SLP-C-091", 450, true, 24) }
            item { CatalogItemCard("Rel Profile R54", "RFL-R-054", 12, false, 0) }
        }
    }
}

// ==========================================
// KONTEN: STEP 4 - TECHNICAL DOCS
// ==========================================
@Composable
private fun Step4TechnicalDocs() {
    LazyColumn(modifier = Modifier.fillMaxSize()) {
        item {
            Text("Upload Blueprints & SPK", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = RailBlue)
            Spacer(modifier = Modifier.height(8.dp))
            Text("Please provide the necessary technical documentation for this asset. Our AI will automatically scan and verify the schematics.", color = Color.Gray, fontSize = 14.sp)
            Spacer(modifier = Modifier.height(24.dp))

            // Box Upload Besar
            Box(
                modifier = Modifier.fillMaxWidth().height(180.dp).background(RailBlue, RoundedCornerShape(8.dp)).clickable { },
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Box(modifier = Modifier.size(64.dp).background(Color.White.copy(alpha=0.2f), RoundedCornerShape(16.dp)), contentAlignment = Alignment.Center) {
                        Icon(Icons.Default.UploadFile, null, tint = Color.White, modifier = Modifier.size(32.dp))
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("Scan Document with AI", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                    Text("Use camera or upload file", color = Color.White.copy(alpha = 0.7f), fontSize = 14.sp)
                }
            }

            Spacer(modifier = Modifier.height(32.dp))
            Text("UPLOADED DOCUMENTS", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.Gray)
            Spacer(modifier = Modifier.height(8.dp))
            DocItemCard("SPK_Locomotive_B29.pdf", "AI Processing...", Icons.Default.Sync)
            Spacer(modifier = Modifier.height(8.dp))
            DocItemCard("Bogie_Blueprint_v2.dwg", "Verified • 4.2 MB", Icons.Default.CheckCircle, Color(0xFF10B981))
        }
    }
}

// ==========================================
// KONTEN: STEP 5 - FINAL REVIEW
// ==========================================
@Composable
private fun Step5FinalReview() {
    LazyColumn(modifier = Modifier.fillMaxSize()) {
        item {
            Text("Final Review", fontSize = 28.sp, fontWeight = FontWeight.Bold, color = RailBlue)
            Text("Verify requisition details and provide authorization signature.", color = Color.Gray, fontSize = 14.sp)
            Spacer(modifier = Modifier.height(24.dp))

            // Box Signature
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

            // Summary Card
            Card(colors = CardDefaults.cardColors(containerColor = Color.White), border = BorderStroke(1.dp, Color.LightGray.copy(alpha=0.5f)), modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("REQUISITION DETAILS", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.Gray)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Project Code: LRT-JABO-24A", fontWeight = FontWeight.Bold, color = RailBlue)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Requestor: Giovan Lado", fontWeight = FontWeight.Bold, color = RailBlue)
                }
            }
        }
    }
}

// ==========================================
// HELPER COMPOSABLES (UI KECIL)
// ==========================================
@Composable
private fun FormTextField(label: String, icon: androidx.compose.ui.graphics.vector.ImageVector, value: String, isDropdown: Boolean = false, isTrailingIcon: Boolean = false) {
    Column(modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp)) {
        Text(label, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.DarkGray)
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedTextField(
            value = value, onValueChange = {}, readOnly = true,
            leadingIcon = if (!isTrailingIcon) { { Icon(icon, null, tint = Color.Gray) } } else null,
            trailingIcon = if (isTrailingIcon || isDropdown) { { Icon(if (isDropdown) Icons.Default.ArrowDropDown else icon, null, tint = Color.Gray) } } else null,
            modifier = Modifier.fillMaxWidth(),
            colors = OutlinedTextFieldDefaults.colors(unfocusedContainerColor = Color.White, focusedContainerColor = Color.White)
        )
    }
}

@Composable
private fun ProjectTypeCard(modifier: Modifier, title: String, type: String, icon: androidx.compose.ui.graphics.vector.ImageVector, isSelected: Boolean) {
    Card(
        modifier = modifier.height(100.dp),
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

@Composable
private fun CatalogItemCard(name: String, id: String, stock: Int, isSafe: Boolean, qtyReq: Int) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(if(qtyReq > 0) 2.dp else 1.dp, if(qtyReq > 0) RailBlue else Color.LightGray)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(name, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = RailBlue)
            Text(id, color = Color.Gray, fontSize = 12.sp)
            Spacer(modifier = Modifier.height(8.dp))
            Text(if (isSafe) "Safe: $stock" else "Low: $stock", color = if(isSafe) Color(0xFF10B981) else Color.Red, fontSize = 12.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(16.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text("Quantity Required", fontSize = 14.sp)
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.border(1.dp, Color.LightGray, RoundedCornerShape(8.dp))) {
                    IconButton(onClick = {}) { Icon(Icons.Default.Remove, null, tint = RailBlue) }
                    Text("$qtyReq", modifier = Modifier.padding(horizontal = 16.dp), fontWeight = FontWeight.Bold)
                    IconButton(onClick = {}) { Icon(Icons.Default.Add, null, tint = RailBlue) }
                }
            }
        }
    }
}

@Composable
private fun DocItemCard(name: String, status: String, iconStatus: androidx.compose.ui.graphics.vector.ImageVector, colorStatus: Color = RailBlue) {
    Card(colors = CardDefaults.cardColors(containerColor = Color.White), border = BorderStroke(1.dp, Color.LightGray)) {
        Row(modifier = Modifier.fillMaxWidth().padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(modifier = Modifier.size(40.dp).background(Color.LightGray.copy(alpha=0.3f), RoundedCornerShape(8.dp)), contentAlignment = Alignment.Center) {
                Icon(Icons.Default.Description, null, tint = Color.Gray)
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(name, fontSize = 14.sp, fontWeight = FontWeight.Medium)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(iconStatus, null, tint = colorStatus, modifier = Modifier.size(12.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(status, color = colorStatus, fontSize = 12.sp)
                }
            }
            Icon(Icons.Default.MoreVert, null, tint = Color.Gray)
        }
    }
}