package com.example.raillog.presentation.screens.requisition

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.raillog.data.local.datastore.UserPreferences
import com.example.raillog.presentation.theme.RailLogColors
import kotlinx.coroutines.flow.first
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import org.koin.compose.koinInject
import org.koin.compose.viewmodel.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RequisitionScreen(
    viewModel: RequisitionViewModel = koinViewModel(),
    draftId: String? = null,
    onNavigateBack: () -> Unit,
    onNavigateToHome: () -> Unit,
    userPreferences: UserPreferences = koinInject()
) {
    val uiState by viewModel.uiState.collectAsState()
    var currentStep by remember { mutableIntStateOf(1) }

    LaunchedEffect(Unit) {
        if (draftId.isNullOrBlank()) {
            val name = userPreferences.staffName.first()
            val nip = userPreferences.staffId.first()
            val phone = userPreferences.staffPhone.first()
            if (name.isNotEmpty()) viewModel.updateName(name)
            if (nip.isNotEmpty()) viewModel.updateEmployeeId(nip)
            if (phone.isNotEmpty()) viewModel.updatePhone(phone)
        } else {
            viewModel.loadDraft(draftId) { savedStep -> currentStep = savedStep }
        }
    }

    LaunchedEffect(uiState.submitSuccess) {
        if (uiState.submitSuccess) onNavigateToHome()
    }

    Scaffold(
        containerColor = RailLogColors.SurfaceSlate,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = when(currentStep) {
                            1 -> "1. Identitas Valid"; 2 -> "2. Spesifikasi Audit"; 3 -> "3. Input Kuantitas"
                            4 -> "4. Justifikasi"; else -> "5. Otorisasi Final"
                        },
                        fontWeight = FontWeight.ExtraBold, color = RailLogColors.PrimaryNavy,
                        modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.Center
                    )
                },
                navigationIcon = {
                    IconButton(onClick = {
                        viewModel.saveDraftAutomatically(currentStep)
                        if (currentStep > 1) currentStep-- else onNavigateBack()
                    }) {
                        Icon(if (currentStep == 1) Icons.Default.Close else Icons.AutoMirrored.Filled.ArrowBack, "Back", tint = RailLogColors.PrimaryNavy)
                    }
                },
                actions = { Spacer(modifier = Modifier.width(48.dp)) },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = RailLogColors.SurfaceSlate)
            )
        },
        bottomBar = {
            BottomActionBar(
                currentStep = currentStep,
                isSubmitting = uiState.isSubmitting,
                onNext = { if (currentStep < 5) currentStep++ },
                onBack = { if (currentStep > 1) currentStep-- },
                onSubmit = { viewModel.submitRequisition() },
                viewModel = viewModel
            )
        }
    ) { paddingValues ->
        Column(modifier = Modifier.fillMaxSize().padding(paddingValues)) {
            StepProgressBar(currentStep = currentStep)
            Box(modifier = Modifier.fillMaxSize().padding(horizontal = 24.dp)) {
                when (currentStep) {
                    1 -> Step1Identity(uiState, viewModel)
                    2 -> Step2ProjectSpecs(uiState, viewModel)
                    3 -> Step3MaterialCatalog(uiState, viewModel)
                    4 -> Step4Justification(uiState, viewModel)
                    5 -> Step5FinalReview(uiState, viewModel)
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun Step1Identity(uiState: RequisitionFormState, viewModel: RequisitionViewModel) {
    val departments = listOf("Maintenance", "Infrastructure", "Signals", "Logistics")
    var showDatePicker by remember { mutableStateOf(false) }
    val datePickerState = rememberDatePickerState()

    if (showDatePicker) {
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let { 
                        val instant = Instant.fromEpochMilliseconds(it)
                        val dt = instant.toLocalDateTime(TimeZone.currentSystemDefault())
                        viewModel.updateDate("${dt.dayOfMonth}/${dt.monthNumber}/${dt.year}")
                    }
                    showDatePicker = false
                }) { Text("SETEL", fontWeight = FontWeight.Bold, color = RailLogColors.PrimaryNavy) }
            }
        ) { DatePicker(state = datePickerState) }
    }

    LazyColumn(modifier = Modifier.fillMaxSize()) {
        item {
            Text("Identitas Resmi", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = RailLogColors.PrimaryNavy)
            Text("Data identitas valid ditarik otomatis dari akun Anda.", color = Color.Black, fontSize = 14.sp)
            Spacer(modifier = Modifier.height(24.dp))

            FormTextField("NAMA PENGJU", Icons.Default.Person, uiState.requestorName, { viewModel.updateName(it) }, "Nama sesuai ID", readOnly = true)
            FormTextField("NIP / ID PEGAWAI", Icons.Default.Badge, uiState.employeeId, { viewModel.updateEmployeeId(it) }, "NIP sesuai profil", readOnly = true)
            FormTextField("KONTAK WHATSAPP", Icons.Default.Phone, uiState.phoneNumber, { viewModel.updatePhone(it) }, "Kontak pengaju", readOnly = true)
            
            HorizontalDivider(modifier = Modifier.padding(vertical = 16.dp), color = Color.Black.copy(alpha = 0.2f))
            
            FormTextField("SUPERVISOR SITE", Icons.Default.SupervisorAccount, uiState.supervisorName, { viewModel.updateSupervisor(it) }, "Nama Atasan Langsung")
            DropdownField("UNIT KERJA", Icons.Default.Business, uiState.department, departments, { viewModel.updateDepartment(it) })
            
            Text("TANGGAL DIBUTUHKAN", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = RailLogColors.PrimaryNavy)
            Spacer(modifier = Modifier.height(8.dp))
            Box(modifier = Modifier.fillMaxWidth()) {
                OutlinedTextField(
                    value = uiState.dateOfRequest, onValueChange = {}, readOnly = true,
                    placeholder = { Text("Klik untuk pilih tanggal", color = Color.Black, fontWeight = FontWeight.Bold) },
                    leadingIcon = { Icon(Icons.Default.CalendarToday, null, tint = RailLogColors.PrimaryNavy) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.Black, unfocusedTextColor = Color.Black,
                        unfocusedBorderColor = Color.Black, focusedBorderColor = RailLogColors.PrimaryNavy,
                        unfocusedContainerColor = Color.White, focusedContainerColor = Color.White
                    )
                )
                Box(modifier = Modifier.matchParentSize().clickable { showDatePicker = true })
            }
            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
private fun Step2ProjectSpecs(uiState: RequisitionFormState, viewModel: RequisitionViewModel) {
    val sites = listOf("Depo MRT Lebak Bulus", "Depo LRT Harjamukti", "Depo LRT Kelapa Gading", "Balai Yasa Manggarai", "Workshop KCI Depok", "Depo HSR Tegalluar", "Workshop KAI Madiun", "Depo Cipinang")
    LazyColumn(modifier = Modifier.fillMaxSize()) {
        item {
            Text("Spesifikasi Audit", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = RailLogColors.PrimaryNavy)
            Spacer(modifier = Modifier.height(16.dp))
            Text("TIPE ARMADA", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = RailLogColors.PrimaryNavy)
            Spacer(modifier = Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                ProjectTypeCard(Modifier.weight(1f), "MRT", Icons.Default.Subway, uiState.projectType == "MRT") { viewModel.updateProjectType("MRT") }
                ProjectTypeCard(Modifier.weight(1f), "LRT", Icons.Default.DirectionsTransit, uiState.projectType == "LRT") { viewModel.updateProjectType("LRT") }
                ProjectTypeCard(Modifier.weight(1f), "KRL", Icons.Default.Train, uiState.projectType == "KRL") { viewModel.updateProjectType("KRL") }
            }
            Spacer(modifier = Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                ProjectTypeCard(Modifier.weight(1f), "Whoosh / HSR", Icons.Default.Speed, uiState.projectType == "HSR") { viewModel.updateProjectType("HSR") }
                ProjectTypeCard(Modifier.weight(1f), "KAI (Executive)", Icons.Default.AirlineSeatReclineExtra, uiState.projectType == "PASSENGER") { viewModel.updateProjectType("PASSENGER") }
            }
            Spacer(modifier = Modifier.height(24.dp))
            FormTextField("KODE PROYEK", Icons.Default.Numbers, uiState.projectCode, { viewModel.updateProjectCode(it) }, "Format: [TIPE]-[AREA]-[ID]")
            DropdownField("DEPO TUJUAN", Icons.Default.Factory, uiState.destinationSite, sites, { viewModel.updateDestinationSite(it) })
        }
    }
}

@Composable
private fun Step3MaterialCatalog(uiState: RequisitionFormState, viewModel: RequisitionViewModel) {
    val displayedItems = uiState.catalogItems.filter { item ->
        (uiState.selectedCategory == "All" || item.category == uiState.selectedCategory) &&
        (uiState.searchQuery.isBlank() || item.name.contains(uiState.searchQuery, ignoreCase = true))
    }

    Column(modifier = Modifier.fillMaxSize()) {
        Text("Katalog Material", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = RailLogColors.PrimaryNavy)
        Text("Input kuantitas manual untuk jumlah besar.", color = Color.Black, fontSize = 14.sp)
        Spacer(modifier = Modifier.height(16.dp))
        OutlinedTextField(
            value = uiState.searchQuery, onValueChange = { viewModel.updateSearchQuery(it) },
            modifier = Modifier.fillMaxWidth(), placeholder = { Text("Cari komponen teknis...", color = Color.Black) },
            leadingIcon = { Icon(Icons.Default.Search, null, tint = RailLogColors.PrimaryNavy) },
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.Black, unfocusedTextColor = Color.Black, unfocusedBorderColor = Color.Black, focusedBorderColor = RailLogColors.PrimaryNavy, unfocusedContainerColor = Color.White, focusedContainerColor = Color.White)
        )
        Spacer(modifier = Modifier.height(16.dp))
        LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            items(displayedItems) { item ->
                Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = Color.White), border = BorderStroke(2.dp, if (item.reqQty > 0) RailLogColors.PrimaryNavy else Color.Black)) {
                    Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(item.name, fontWeight = FontWeight.Bold, color = RailLogColors.PrimaryNavy, fontSize = 16.sp)
                            Text("Stok: ${item.stock} ${item.unit}", fontSize = 12.sp, color = if (item.isSafe) RailLogColors.SuccessEmerald else RailLogColors.ErrorRed, fontWeight = FontWeight.Bold)
                        }
                        OutlinedTextField(
                            value = if (item.reqQty == 0) "" else item.reqQty.toString(),
                            onValueChange = { val qty = it.filter { c -> c.isDigit() }.toIntOrNull() ?: 0; viewModel.updateItemQuantity(item.id, qty) },
                            modifier = Modifier.width(85.dp),
                            placeholder = { Text("0", textAlign = TextAlign.Center, fontWeight = FontWeight.Bold) },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true,
                            shape = RoundedCornerShape(8.dp),
                            textStyle = LocalTextStyle.current.copy(textAlign = TextAlign.Center, fontWeight = FontWeight.Bold, color = Color.Black),
                            colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.Black, unfocusedTextColor = Color.Black, unfocusedBorderColor = Color.Black, focusedBorderColor = RailLogColors.PrimaryNavy)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun Step4Justification(uiState: RequisitionFormState, viewModel: RequisitionViewModel) {
    LazyColumn(modifier = Modifier.fillMaxSize()) {
        item {
            Text("Lembar Justifikasi", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = RailLogColors.PrimaryNavy)
            Text("Berikan alasan teknis pengadaan kepada Admin.", color = Color.Black, fontSize = 14.sp)
            Spacer(modifier = Modifier.height(24.dp))
            OutlinedTextField(
                value = uiState.notes, onValueChange = { viewModel.updateNotes(it) },
                modifier = Modifier.fillMaxWidth().height(200.dp),
                placeholder = { Text("Tuliskan alasan teknis penggunaan barang...", color = Color.Black.copy(alpha = 0.5f)) },
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.Black, unfocusedTextColor = Color.Black, unfocusedBorderColor = Color.Black, focusedBorderColor = RailLogColors.PrimaryNavy, unfocusedContainerColor = Color.White, focusedContainerColor = Color.White)
            )
            Spacer(modifier = Modifier.height(24.dp))
            Button(
                onClick = { viewModel.runPreSubmitCheck() },
                modifier = Modifier.fillMaxWidth().height(56.dp),
                colors = ButtonDefaults.buttonColors(containerColor = RailLogColors.PrimaryNavy, contentColor = Color.White),
                shape = RoundedCornerShape(12.dp),
                enabled = !uiState.isProcessingPreCheck
            ) {
                if (uiState.isProcessingPreCheck) CircularProgressIndicator(color = Color.White, modifier = Modifier.size(20.dp))
                else Text("AUDIT KELAYAKAN DENGAN AI", fontWeight = FontWeight.ExtraBold, color = Color.White)
            }
            if (uiState.aiPreCheckResult != null) {
                Spacer(modifier = Modifier.height(16.dp))
                Card(colors = CardDefaults.cardColors(containerColor = RailLogColors.AISurface), border = BorderStroke(2.dp, RailLogColors.AIBorder), shape = RoundedCornerShape(12.dp)) {
                    Text(uiState.aiPreCheckResult, modifier = Modifier.padding(16.dp), fontSize = 13.sp, color = Color.Black, lineHeight = 20.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
private fun Step5FinalReview(uiState: RequisitionFormState, viewModel: RequisitionViewModel) {
    var signaturePaths by remember { mutableStateOf(listOf<Path>()) }
    var currentPath by remember { mutableStateOf<Path?>(null) }
    val requestedItems = uiState.catalogItems.filter { it.reqQty > 0 }

    LazyColumn(modifier = Modifier.fillMaxSize()) {
        item {
            Text("Otorisasi Digital", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = RailLogColors.PrimaryNavy)
            Text("Tanda tangan di bawah ini bersifat mengikat untuk audit resmi.", color = Color.Black, fontSize = 14.sp)
            Spacer(modifier = Modifier.height(24.dp))
            
            // SUMMARY CARD (As requested in Step 5 Enhancement)
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                border = BorderStroke(2.dp, Color.Black),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("RINGKASAN PENGAJUAN", fontWeight = FontWeight.ExtraBold, color = RailLogColors.PrimaryNavy, fontSize = 14.sp)
                    HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp), color = Color.Black.copy(alpha = 0.1f))
                    
                    SummaryRow("Pengaju", uiState.requestorName)
                    SummaryRow("ID Pegawai", uiState.employeeId)
                    SummaryRow("Proyek", "${uiState.projectType} (${uiState.projectCode})")
                    SummaryRow("Tujuan", uiState.destinationSite)
                    
                    Spacer(modifier = Modifier.height(12.dp))
                    Text("DAFTAR MATERIAL", fontWeight = FontWeight.ExtraBold, color = RailLogColors.PrimaryNavy, fontSize = 12.sp)
                    requestedItems.forEach { item ->
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("• ${item.name}", fontSize = 13.sp, color = Color.Black, modifier = Modifier.weight(1f))
                            Text("${item.reqQty} ${item.unit}", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = RailLogColors.PrimaryNavy)
                        }
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            Text("TANDA TANGAN DIGITAL", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = RailLogColors.PrimaryNavy)
            Spacer(modifier = Modifier.height(8.dp))
            Box(modifier = Modifier.fillMaxWidth().height(250.dp).background(Color.White, RoundedCornerShape(12.dp)).border(2.dp, Color.Black).clipToBounds()) {
                Canvas(modifier = Modifier.fillMaxSize().pointerInput(Unit) {
                    detectDragGestures(
                        onDragStart = { offset -> currentPath = Path().apply { moveTo(offset.x, offset.y) } },
                        onDrag = { change, _ -> currentPath?.lineTo(change.position.x, change.position.y) },
                        onDragEnd = { currentPath?.let { signaturePaths = signaturePaths + it; viewModel.setSignedStatus(true) }; currentPath = null }
                    )
                }) {
                    signaturePaths.forEach { drawPath(it, RailLogColors.PrimaryNavy, style = Stroke(6f, cap = StrokeCap.Round, join = StrokeJoin.Round)) }
                    currentPath?.let { drawPath(it, RailLogColors.PrimaryNavy, style = Stroke(6f, cap = StrokeCap.Round, join = StrokeJoin.Round)) }
                }
                if (signaturePaths.isEmpty()) Text("Gambarkan Tanda Tangan Di Sini", modifier = Modifier.align(Alignment.Center), color = Color.Black.copy(alpha = 0.3f), fontWeight = FontWeight.ExtraBold)
                IconButton(onClick = { signaturePaths = emptyList(); viewModel.setSignedStatus(false) }, modifier = Modifier.align(Alignment.TopEnd)) {
                    Icon(Icons.Default.Refresh, "Hapus", tint = RailLogColors.ErrorRed)
                }
            }
            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
private fun SummaryRow(label: String, value: String) {
    Row(modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp)) {
        Text("$label:", modifier = Modifier.width(100.dp), fontSize = 13.sp, color = Color.Black.copy(alpha = 0.6f), fontWeight = FontWeight.Bold)
        Text(value, fontSize = 13.sp, color = Color.Black, fontWeight = FontWeight.Bold)
    }
}

@Composable
private fun BottomActionBar(currentStep: Int, isSubmitting: Boolean, onNext: () -> Unit, onBack: () -> Unit, onSubmit: () -> Unit, viewModel: RequisitionViewModel) {
    Surface(color = Color.White, shadowElevation = 16.dp, modifier = Modifier.fillMaxWidth()) {
        Row(modifier = Modifier.fillMaxWidth().navigationBarsPadding().padding(16.dp), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            if (currentStep > 1) {
                OutlinedButton(onClick = onBack, modifier = Modifier.weight(1f).height(56.dp), border = BorderStroke(2.dp, Color.Black)) { 
                    Text("KEMBALI", color = Color.Black, fontWeight = FontWeight.ExtraBold) 
                }
            }
            Button(
                onClick = { if (currentStep == 5) onSubmit() else onNext() },
                modifier = Modifier.weight(2f).height(56.dp),
                colors = ButtonDefaults.buttonColors(containerColor = RailLogColors.PrimaryNavy, contentColor = Color.White),
                enabled = !isSubmitting
            ) {
                if (isSubmitting) CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                else Text(if (currentStep == 5) "SUBMIT AUDIT RESMI" else "LANJUT KE TAHAP ${currentStep + 1}", fontWeight = FontWeight.ExtraBold, color = Color.White)
            }
        }
    }
}

@Composable
private fun StepProgressBar(currentStep: Int) {
    Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp, vertical = 16.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        for (i in 1..5) Box(modifier = Modifier.weight(1f).height(6.dp).background(if (i <= currentStep) RailLogColors.PrimaryNavy else Color.Black.copy(alpha = 0.2f), RoundedCornerShape(3.dp)))
    }
}

@Composable
private fun ProjectTypeCard(modifier: Modifier, title: String, icon: ImageVector, isSelected: Boolean, onClick: () -> Unit) {
    Card(modifier = modifier.height(100.dp), onClick = onClick, colors = CardDefaults.cardColors(containerColor = if (isSelected) RailLogColors.PrimaryNavy else Color.White), border = BorderStroke(2.dp, if (isSelected) RailLogColors.PrimaryNavy else Color.Black)) {
        Column(modifier = Modifier.padding(8.dp).fillMaxSize(), verticalArrangement = Arrangement.Center, horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(icon, null, tint = if (isSelected) Color.White else Color.Black, modifier = Modifier.size(28.dp))
            Spacer(modifier = Modifier.height(4.dp))
            Text(title, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = if (isSelected) Color.White else Color.Black, textAlign = TextAlign.Center)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun FormTextField(label: String, icon: ImageVector, value: String, onValueChange: (String) -> Unit, placeholder: String, readOnly: Boolean = false) {
    Column(modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp)) {
        Text(label, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = RailLogColors.PrimaryNavy)
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedTextField(
            value = value, onValueChange = onValueChange, readOnly = readOnly,
            placeholder = { Text(placeholder, color = Color.Black.copy(alpha = 0.6f)) },
            leadingIcon = { Icon(icon, null, tint = RailLogColors.PrimaryNavy) },
            modifier = Modifier.fillMaxWidth(), 
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = Color.Black, unfocusedTextColor = Color.Black,
                unfocusedBorderColor = Color.Black, focusedBorderColor = RailLogColors.PrimaryNavy,
                unfocusedContainerColor = Color.White, focusedContainerColor = Color.White
            )
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DropdownField(label: String, icon: ImageVector, selected: String, options: List<String>, onSelection: (String) -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    Column(modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp)) {
        Text(label, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = RailLogColors.PrimaryNavy)
        Spacer(modifier = Modifier.height(8.dp))
        ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = it }) {
            OutlinedTextField(
                value = selected, onValueChange = {}, readOnly = true,
                leadingIcon = { Icon(icon, null, tint = RailLogColors.PrimaryNavy) }, 
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded) },
                modifier = Modifier.menuAnchor(MenuAnchorType.PrimaryNotEditable),
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = Color.Black, unfocusedTextColor = Color.Black,
                    unfocusedBorderColor = Color.Black, focusedBorderColor = RailLogColors.PrimaryNavy,
                    unfocusedContainerColor = Color.White, focusedContainerColor = Color.White
                )
            )
            ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }, modifier = Modifier.background(Color.White)) {
                options.forEach { option -> 
                    DropdownMenuItem(
                        text = { Text(option, fontWeight = FontWeight.Bold, color = Color.Black) },
                        onClick = { onSelection(option); expanded = false }
                    ) 
                }
            }
        }
    }
}
