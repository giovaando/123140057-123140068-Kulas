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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.koin.compose.viewmodel.koinViewModel
import com.example.raillog.core.util.MediaPicker
import com.example.raillog.core.util.rememberMediaPicker

private val RailBlue = Color(0xFF193255)
private val SurfaceGray = Color(0xFFF7F9FC)
private val RailBlueLight = Color(0xFF3B5B85)
private val SafeGreen = Color(0xFF10B981)
private val SafeGreenBg = Color(0xFFD1FAE5)
private val CriticalRed = Color(0xFFEF4444)
private val CriticalRedBg = Color(0xFFFEE2E2)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RequisitionScreen(
    viewModel: RequisitionViewModel = koinViewModel(),
    draftId: String? = null,
    onNavigateBack: () -> Unit,
    onNavigateToHome: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    var currentStep by remember { mutableIntStateOf(1) }

    // Alat untuk membuka Kamera / Galeri KMP yang sudah di-upgrade
    val mediaPicker: MediaPicker = rememberMediaPicker { fileName: String, base64Data: String? ->
        if (!uiState.hasScannedInitialDoc) {
            viewModel.processInitialDocument(base64Data ?: "", fileName)
        } else {
            viewModel.addUploadedDocument(fileName)
        }
    }

    LaunchedEffect(draftId) {
        if (!draftId.isNullOrBlank()) {
            // Jika memuat draft, langsung anggap sudah melewati tahap Scan
            viewModel.skipInitialScan()
            viewModel.loadDraft(draftId) { savedStep ->
                currentStep = savedStep
            }
        }
    }

    LaunchedEffect(uiState.submitSuccess) {
        if (uiState.submitSuccess) onNavigateToHome()
    }

    // ========================================================
    // SKENARIO B: STEP 0 (GATEKEEPER AI SCANNER)
    // ========================================================
    if (!uiState.hasScannedInitialDoc) {
        InitialAIScannerScreen(
            isProcessing = uiState.isProcessingAI,
            mediaPicker = mediaPicker,
            onSkip = { viewModel.skipInitialScan() },
            onBack = onNavigateBack
        )
        return // Hentikan render ke form, tunggu sampai Scan selesai
    }

    // ========================================================
    // FORM REVIEW (Step 1 s/d 5) - Data sudah terisi oleh AI
    // ========================================================
    Scaffold(
        containerColor = SurfaceGray,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = when(currentStep) {
                            2 -> "Project Specifications"; 3 -> "Material Verification"; 4 -> "Technical Docs"; else -> "Requisition"
                        },
                        fontWeight = FontWeight.Bold, color = RailBlue, modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.Center
                    )
                },
                navigationIcon = {
                    IconButton(onClick = {
                        // [TAMBAHAN] Simpan draf saat menekan tombol back di AppBar
                        viewModel.saveDraftAutomatically(currentStep)
                        if (currentStep > 1) currentStep-- else onNavigateBack()
                    }) {
                        Icon(if (currentStep in 1..2) Icons.Default.Close else Icons.Default.ArrowBack, "Back", tint = RailBlue)
                    }
                },
                actions = { Box(modifier = Modifier.padding(end = 16.dp)) { Icon(Icons.Default.AccountCircle, null, tint = Color.Gray, modifier = Modifier.size(32.dp)) } },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = SurfaceGray)
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
                    4 -> Step4TechnicalDocs(uiState, viewModel, mediaPicker)
                    5 -> Step5FinalReview(uiState, viewModel)
                }
            }
        }
    }
}

// ==========================================
// TAMPILAN AWAL (SCANNER/GATEKEEPER)
// ==========================================
@Composable
private fun InitialAIScannerScreen(
    isProcessing: Boolean,
    mediaPicker: MediaPicker,
    onSkip: () -> Unit,
    onBack: () -> Unit
) {
    Box(modifier = Modifier.fillMaxSize().background(RailBlue)) {
        IconButton(onClick = onBack, modifier = Modifier.padding(16.dp).align(Alignment.TopStart)) {
            Icon(Icons.Default.Close, contentDescription = "Back", tint = Color.White)
        }

        Column(
            modifier = Modifier.fillMaxSize().padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            if (isProcessing) {
                // ANIMASI LOADING AI GEMINI
                Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = Color(0xFFFBBF24), modifier = Modifier.size(80.dp))
                Spacer(modifier = Modifier.height(24.dp))
                CircularProgressIndicator(color = Color.White)
                Spacer(modifier = Modifier.height(24.dp))
                Text("Gemini AI is analyzing...", color = Color.White, fontSize = 24.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(8.dp))
                Text("Extracting Waybill data & identifying SKUs", color = Color.LightGray, fontSize = 14.sp)
            } else {
                // TAMPILAN AWAL SEBELUM SCAN
                Box(modifier = Modifier.size(100.dp).background(Color.White.copy(alpha=0.1f), CircleShape), contentAlignment = Alignment.Center) {
                    Icon(Icons.Default.DocumentScanner, contentDescription = null, tint = Color.White, modifier = Modifier.size(50.dp))
                }
                Spacer(modifier = Modifier.height(32.dp))
                Text("Auto-Fill with AI", color = Color.White, fontSize = 28.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(12.dp))
                Text("Take a photo of the Delivery Order or SPK.\nGemini AI will extract all materials and fill the form instantly.",
                    color = Color.LightGray, fontSize = 14.sp, textAlign = TextAlign.Center)

                Spacer(modifier = Modifier.height(48.dp))

                Button(
                    onClick = { mediaPicker.launchCamera() },
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color.White),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Default.CameraAlt, contentDescription = null, tint = RailBlue)
                    Spacer(modifier = Modifier.width(12.dp))
                    Text("Open Camera", color = RailBlue, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                }

                Spacer(modifier = Modifier.height(16.dp))

                OutlinedButton(
                    onClick = { mediaPicker.launchGallery() },
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    border = BorderStroke(1.dp, Color.White.copy(alpha = 0.5f)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Default.PhotoLibrary, contentDescription = null, tint = Color.White)
                    Spacer(modifier = Modifier.width(12.dp))
                    Text("Upload PDF / Image", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                }

                Spacer(modifier = Modifier.height(32.dp))
                TextButton(onClick = onSkip) {
                    Text("Skip & Enter Manually", color = Color.White.copy(alpha = 0.7f), fontSize = 14.sp)
                }
            }
        }
    }
}

// ==========================================
// LANGKAH 1 HINGGA 5 (REVIEW MODE)
// ==========================================
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun Step1Identity(uiState: RequisitionFormState, viewModel: RequisitionViewModel) {
    val departments = listOf("Rolling Stock Maintenance", "Track Infrastructure", "Signaling & Telecom", "Operational Logistics")

    var showDatePicker by remember { mutableStateOf(false) }
    val datePickerState = rememberDatePickerState()

    fun convertMillisToDate(millis: Long): String {
        val days = (millis / 86400000L)
        var y = 1970
        var d = days.toInt()
        while (true) {
            val daysInYear = if (y % 4 == 0 && (y % 100 != 0 || y % 400 == 0)) 366 else 365
            if (d < daysInYear) break
            d -= daysInYear
            y++
        }
        val daysInMonth = intArrayOf(31, if (y % 4 == 0 && (y % 100 != 0 || y % 400 == 0)) 29 else 28, 31, 30, 31, 30, 31, 31, 30, 31, 30, 31)
        var m = 0
        while (d >= daysInMonth[m]) {
            d -= daysInMonth[m]
            m++
        }
        return "${(d + 1).toString().padStart(2, '0')}/${(m + 1).toString().padStart(2, '0')}/$y"
    }

    if (showDatePicker) {
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    val millis = datePickerState.selectedDateMillis
                    if (millis != null) viewModel.updateDate(convertMillisToDate(millis))
                    showDatePicker = false
                }) { Text("OK") }
            },
            dismissButton = { TextButton(onClick = { showDatePicker = false }) { Text("Cancel") } }
        ) { DatePicker(state = datePickerState) }
    }

    LazyColumn(modifier = Modifier.fillMaxSize()) {
        item {
            Text("Review Identity", fontSize = 28.sp, fontWeight = FontWeight.Bold, color = RailBlue)
            Text("Please review the auto-filled identity details below.", color = Color.Gray, fontSize = 14.sp)
            Spacer(modifier = Modifier.height(24.dp))

            FormTextField("Requestor Name", Icons.Default.Person, uiState.requestorName, { viewModel.updateName(it) }, "e.g., Giovan Lado")
            FormTextField("Employee ID", Icons.Default.Badge, uiState.employeeId, { viewModel.updateEmployeeId(it) }, "e.g., RLN-123140068")
            DropdownField("Department / Unit", Icons.Default.Business, uiState.department, departments, { viewModel.updateDepartment(it) })

            Box {
                FormTextField("Date of Request", Icons.Default.CalendarToday, uiState.dateOfRequest, {}, "Select Date", isTrailingIcon = true, readOnly = true)
                Spacer(modifier = Modifier.matchParentSize().clickable { showDatePicker = true })
            }
        }
    }
}

@Composable
private fun Step2ProjectSpecs(uiState: RequisitionFormState, viewModel: RequisitionViewModel) {
    val sites = listOf("Manggarai Depot", "Depok Workshop", "Bandung Station", "Yogyakarta Yard")

    LazyColumn(modifier = Modifier.fillMaxSize()) {
        item {
            Text("Review Project Specs", fontSize = 28.sp, fontWeight = FontWeight.Bold, color = RailBlue)
            Spacer(modifier = Modifier.height(16.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                ProjectTypeCard(Modifier.weight(1f), "K1 (Executive)", "PASSENGER", Icons.Default.AirlineSeatReclineExtra, uiState.projectType == "PASSENGER") { viewModel.updateProjectType("PASSENGER") }
                ProjectTypeCard(Modifier.weight(1f), "LRT", "URBAN COMMUTE", Icons.Default.DirectionsTransit, uiState.projectType == "LRT") { viewModel.updateProjectType("LRT") }
            }
            Spacer(modifier = Modifier.height(16.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                ProjectTypeCard(Modifier.weight(1f), "KRL", "SUBURBAN", Icons.Default.Train, uiState.projectType == "KRL") { viewModel.updateProjectType("KRL") }
                ProjectTypeCard(Modifier.weight(1f), "High-Speed", "INTERCITY", Icons.Default.Speed, uiState.projectType == "High-Speed") { viewModel.updateProjectType("High-Speed") }
            }
            Spacer(modifier = Modifier.height(32.dp))
            HorizontalDivider(color = Color.LightGray.copy(alpha = 0.5f))
            Spacer(modifier = Modifier.height(24.dp))

            FormTextField("PROJECT CODE", Icons.Default.Numbers, uiState.projectCode, { viewModel.updateProjectCode(it) }, "e.g. LRT-JABO-24A")
            Text("Must be unique per region.", color = Color.Gray, fontSize = 12.sp, modifier = Modifier.padding(bottom = 16.dp))
            DropdownField("DESTINATION SITE / WORKSHOP", Icons.Default.Factory, uiState.destinationSite, sites, { viewModel.updateDestinationSite(it) })

            if (uiState.errorMessage != null) Text(uiState.errorMessage, color = Color.Red, fontSize = 12.sp, modifier = Modifier.padding(top = 8.dp))
            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun Step3MaterialCatalog(uiState: RequisitionFormState, viewModel: RequisitionViewModel) {
    val displayedItems = uiState.catalogItems.filter { item ->
        val matchCategory = uiState.selectedCategory == "All" || item.category == uiState.selectedCategory
        val matchSearch = uiState.searchQuery.isBlank() || item.name.contains(uiState.searchQuery, ignoreCase = true) || item.id.contains(uiState.searchQuery, ignoreCase = true)
        matchCategory && matchSearch
    }

    Column(modifier = Modifier.fillMaxSize()) {
        Text("Verify Quantities", fontSize = 28.sp, fontWeight = FontWeight.Bold, color = RailBlue)
        Text("AI has filled these based on your document. Tap (+) or (-) to correct if AI made a mistake.", color = Color.Gray, fontSize = 14.sp)
        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = uiState.searchQuery, onValueChange = { viewModel.updateSearchQuery(it) },
            modifier = Modifier.fillMaxWidth(), placeholder = { Text("Search material name or ID...") },
            leadingIcon = { Icon(Icons.Default.Search, null) }, shape = RoundedCornerShape(8.dp), colors = OutlinedTextFieldDefaults.colors(unfocusedContainerColor = Color.White)
        )
        Spacer(modifier = Modifier.height(16.dp))

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            listOf("All", "Infrastructure", "Spare Parts", "Tools").forEach { text ->
                val isSelected = uiState.selectedCategory == text
                Box(
                    modifier = Modifier.clickable { viewModel.updateCategoryFilter(text) }
                        .background(if(isSelected) RailBlue else Color.White, RoundedCornerShape(4.dp))
                        .border(1.dp, if(isSelected) RailBlue else Color.LightGray, RoundedCornerShape(4.dp))
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                ) { Text(text, color = if(isSelected) Color.White else RailBlue, fontSize = 12.sp) }
            }
        }
        Spacer(modifier = Modifier.height(16.dp))

        LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            items(displayedItems) { item ->
                Card(colors = CardDefaults.cardColors(containerColor = Color.White), border = BorderStroke(if(item.reqQty > 0) 2.dp else 1.dp, if(item.reqQty > 0) RailBlue else Color.LightGray), modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(item.name, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = RailBlue)
                        Text("# ${item.id}", color = Color.Gray, fontSize = 12.sp)
                        Spacer(modifier = Modifier.height(8.dp))
                        Box(modifier = Modifier.background(if(item.isSafe) SafeGreenBg else CriticalRedBg, RoundedCornerShape(4.dp)).padding(horizontal = 8.dp, vertical = 4.dp)) {
                            Text(if(item.isSafe) "Safe: ${item.stock}" else "Low: ${item.stock}", color = if(item.isSafe) SafeGreen else CriticalRed, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                            Text("Quantity Required", fontSize = 14.sp)
                            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.border(1.dp, Color.LightGray, RoundedCornerShape(8.dp))) {
                                IconButton(onClick = { viewModel.updateItemQuantity(item.id, false) }) { Icon(Icons.Default.Remove, null, tint = RailBlue) }
                                Text("${item.reqQty}", modifier = Modifier.padding(horizontal = 16.dp), fontWeight = FontWeight.Bold)
                                IconButton(onClick = { viewModel.updateItemQuantity(item.id, true) }) { Icon(Icons.Default.Add, null, tint = RailBlue) }
                            }
                        }
                    }
                }
            }
            item { Spacer(modifier = Modifier.height(64.dp)) }
        }
    }
}

@Composable
private fun Step4TechnicalDocs(uiState: RequisitionFormState, viewModel: RequisitionViewModel, mediaPicker: MediaPicker) {
    var showMediaDialog by remember { mutableStateOf(false) }

    if (showMediaDialog) {
        AlertDialog(
            onDismissRequest = { showMediaDialog = false }, title = { Text("Upload Document", fontWeight = FontWeight.Bold) },
            text = { Text("Choose a method to provide your document:") },
            confirmButton = {
                Button(onClick = { showMediaDialog = false; mediaPicker.launchCamera() }, colors = ButtonDefaults.buttonColors(containerColor = RailBlue)) {
                    Icon(Icons.Default.CameraAlt, null, modifier = Modifier.size(16.dp)); Spacer(modifier = Modifier.width(8.dp)); Text("Camera")
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { showMediaDialog = false; mediaPicker.launchGallery() }) {
                    Icon(Icons.Default.Folder, null, modifier = Modifier.size(16.dp)); Spacer(modifier = Modifier.width(8.dp)); Text("Gallery")
                }
            }
        )
    }

    LazyColumn(modifier = Modifier.fillMaxSize()) {
        item {
            Text("Attached Documents", fontSize = 28.sp, fontWeight = FontWeight.Bold, color = RailBlue)
            Text("The document you scanned earlier is attached here. Add more if needed.", color = Color.Gray, fontSize = 14.sp)
            Spacer(modifier = Modifier.height(24.dp))

            Box(modifier = Modifier.fillMaxWidth().height(140.dp).background(RailBlue, RoundedCornerShape(8.dp)).clickable { showMediaDialog = true }, contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Default.UploadFile, null, tint = Color.White, modifier = Modifier.size(32.dp))
                    Spacer(modifier = Modifier.height(12.dp))
                    Text("Add Additional Document", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            if (uiState.uploadedDocs.isEmpty()) {
                Text("No documents yet.", color = Color.Gray, fontSize = 12.sp, modifier = Modifier.padding(top=8.dp))
            } else {
                uiState.uploadedDocs.forEach { docName ->
                    Card(colors = CardDefaults.cardColors(containerColor = Color.White), border = BorderStroke(1.dp, Color.LightGray), modifier = Modifier.fillMaxWidth().padding(top = 8.dp)) {
                        Row(modifier = Modifier.fillMaxWidth().padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                            Box(modifier = Modifier.size(40.dp).background(SafeGreenBg, RoundedCornerShape(8.dp)), contentAlignment = Alignment.Center) { Icon(Icons.Default.CheckCircle, null, tint = SafeGreen) }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(docName, fontSize = 14.sp, fontWeight = FontWeight.Medium)
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.Verified, null, tint = SafeGreen, modifier = Modifier.size(12.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("AI Scanned & Attached", color = SafeGreen, fontSize = 12.sp)
                                }
                            }
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
private fun Step5FinalReview(uiState: RequisitionFormState, viewModel: RequisitionViewModel) {
    val requestedItems = uiState.catalogItems.filter { it.reqQty > 0 }
    var signaturePaths by remember { mutableStateOf(listOf<Path>()) }
    var currentPath by remember { mutableStateOf<Path?>(null) }

    LazyColumn(modifier = Modifier.fillMaxSize()) {
        item {
            Text("Final Review", fontSize = 28.sp, fontWeight = FontWeight.Bold, color = RailBlue)
            Spacer(modifier = Modifier.height(24.dp))

            Card(colors = CardDefaults.cardColors(containerColor = Color.White), border = BorderStroke(1.dp, if(uiState.errorMessage != null && !uiState.isSigned) Color.Red else Color.LightGray.copy(alpha=0.5f))) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Authorization Signature", color = RailBlue, fontSize = 16.sp, fontWeight = FontWeight.Medium)
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.clickable {
                            signaturePaths = emptyList(); currentPath = null; viewModel.setSignedStatus(false)
                        }) {
                            Icon(Icons.Default.Clear, null, modifier = Modifier.size(12.dp), tint = Color.Gray); Text("Clear", color = Color.Gray, fontSize = 12.sp)
                        }
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    Box(modifier = Modifier.fillMaxWidth().height(120.dp).background(Color(0xFFF9FAFB), RoundedCornerShape(8.dp)).border(1.dp, Color.LightGray, RoundedCornerShape(8.dp)).clipToBounds()) {
                        if (signaturePaths.isEmpty() && currentPath == null) Text("Draw your signature here", color = Color.LightGray, fontSize = 16.sp, modifier = Modifier.align(Alignment.Center))
                        Canvas(
                            modifier = Modifier.fillMaxSize().pointerInput(Unit) {
                                detectDragGestures(
                                    onDragStart = { offset -> currentPath = Path().apply { moveTo(offset.x, offset.y) } },
                                    onDrag = { change, _ -> currentPath?.lineTo(change.position.x, change.position.y) },
                                    onDragEnd = { currentPath?.let { signaturePaths = signaturePaths + it; viewModel.setSignedStatus(true) }; currentPath = null }
                                )
                            }
                        ) {
                            signaturePaths.forEach { path -> drawPath(path = path, color = RailBlue, style = Stroke(width = 6f, cap = StrokeCap.Round, join = StrokeJoin.Round)) }
                            currentPath?.let { path -> drawPath(path = path, color = RailBlue, style = Stroke(width = 6f, cap = StrokeCap.Round, join = StrokeJoin.Round)) }
                        }
                    }
                    if (uiState.errorMessage != null && !uiState.isSigned) Text(uiState.errorMessage, color = Color.Red, fontSize = 12.sp, modifier = Modifier.padding(top=8.dp))
                }
            }
            Spacer(modifier = Modifier.height(16.dp))

            Card(colors = CardDefaults.cardColors(containerColor = Color.White), border = BorderStroke(1.dp, Color.LightGray.copy(alpha=0.5f)), modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Inventory2, null, tint = RailBlue, modifier = Modifier.size(16.dp)); Spacer(modifier = Modifier.width(8.dp)); Text("Requested Materials", color = RailBlue, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        }
                        Box(modifier = Modifier.background(Color.LightGray.copy(alpha=0.3f), RoundedCornerShape(4.dp)).padding(horizontal = 8.dp, vertical = 2.dp)) { Text("${requestedItems.size} Items", fontSize = 12.sp, color = Color.DarkGray) }
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    requestedItems.forEach { item ->
                        Row(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                            Box(modifier = Modifier.size(40.dp).background(SurfaceGray, RoundedCornerShape(8.dp)), contentAlignment = Alignment.Center) { Icon(Icons.Default.Build, null, tint = Color.Gray) }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) { Text(item.name, fontSize = 14.sp, fontWeight = FontWeight.Medium); Text("SKU: ${item.id}", color = Color.Gray, fontSize = 12.sp) }
                            Column(horizontalAlignment = Alignment.End) { Text("Qty", fontSize = 10.sp, color = Color.Gray); Text("${item.reqQty}", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = RailBlue) }
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DropdownField(label: String, icon: ImageVector, selectedText: String, options: List<String>, onSelection: (String) -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    Column(modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp)) {
        Text(label, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.DarkGray)
        Spacer(modifier = Modifier.height(8.dp))
        ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = !expanded }) {
            OutlinedTextField(
                value = selectedText, onValueChange = {}, readOnly = true, leadingIcon = { Icon(icon, null, tint = Color.Gray) },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) }, modifier = Modifier.fillMaxWidth().menuAnchor(),
                colors = OutlinedTextFieldDefaults.colors(unfocusedContainerColor = Color.White, focusedContainerColor = Color.White)
            )
            ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                options.forEach { selectionOption -> DropdownMenuItem(text = { Text(selectionOption) }, onClick = { onSelection(selectionOption); expanded = false }) }
            }
        }
    }
}

@Composable
private fun FormTextField(label: String, icon: ImageVector, value: String, onValueChange: (String) -> Unit = {}, placeholder: String = "", isTrailingIcon: Boolean = false, readOnly: Boolean = false) {
    Column(modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp)) {
        Text(label, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.DarkGray)
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedTextField(
            value = value, onValueChange = onValueChange, readOnly = readOnly, placeholder = { Text(placeholder, color = Color.LightGray) },
            leadingIcon = if (!isTrailingIcon) { { Icon(icon, null, tint = Color.Gray) } } else null,
            trailingIcon = if (isTrailingIcon) { { Icon(icon, null, tint = Color.Gray) } } else null,
            modifier = Modifier.fillMaxWidth(), colors = OutlinedTextFieldDefaults.colors(unfocusedContainerColor = Color.White, focusedContainerColor = Color.White)
        )
    }
}

@Composable
private fun ProjectTypeCard(modifier: Modifier, title: String, type: String, icon: ImageVector, isSelected: Boolean, onClick: () -> Unit) {
    Card(modifier = modifier.height(100.dp), onClick = onClick, colors = CardDefaults.cardColors(containerColor = if (isSelected) RailBlueLight.copy(alpha = 0.1f) else Color.White), border = BorderStroke(if (isSelected) 2.dp else 1.dp, if (isSelected) RailBlue else Color.LightGray)) {
        Column(modifier = Modifier.padding(12.dp).fillMaxSize(), verticalArrangement = Arrangement.SpaceBetween) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Icon(icon, null, tint = if (isSelected) RailBlue else Color.Gray)
                Icon(if (isSelected) Icons.Default.RadioButtonChecked else Icons.Default.RadioButtonUnchecked, null, tint = if (isSelected) RailBlue else Color.LightGray)
            }
            Column { Text(type, fontSize = 10.sp, color = if (isSelected) RailBlue else Color.Gray, fontWeight = FontWeight.Bold); Text(title, fontSize = 14.sp, color = RailBlue, fontWeight = FontWeight.Bold) }
        }
    }
}

@Composable
private fun BottomActionBar(currentStep: Int, isSubmitting: Boolean, onNext: () -> Unit, onBack: () -> Unit, onSubmit: () -> Unit, viewModel: RequisitionViewModel) {
    Surface(color = SurfaceGray, shadowElevation = 16.dp, modifier = Modifier.fillMaxWidth()) {
        Row(modifier = Modifier.fillMaxWidth().navigationBarsPadding().padding(16.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {

            if (currentStep in 2..5) {
                OutlinedButton(
                    onClick = {
                        // [TAMBAHAN] Simpan draf saat mundur
                        viewModel.saveDraftAutomatically(currentStep)
                        onBack()
                    },
                    modifier = Modifier.weight(1f).height(50.dp), shape = RoundedCornerShape(8.dp), border = BorderStroke(1.dp, Color.LightGray)
                ) { Text("Back", color = RailBlue, fontWeight = FontWeight.Bold) }
                Spacer(modifier = Modifier.width(16.dp))
            }

            Button(
                onClick = {
                    if (currentStep == 5) {
                        onSubmit()
                    } else {
                        // [TAMBAHAN] Simpan draf saat maju ke step berikutnya
                        viewModel.saveDraftAutomatically(currentStep)
                        onNext()
                    }
                },
                modifier = Modifier.weight(2f).height(50.dp), enabled = !isSubmitting,
                colors = ButtonDefaults.buttonColors(containerColor = RailBlue), shape = RoundedCornerShape(8.dp)
            ) {
                if (isSubmitting) CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                else {
                    Text(text = if (currentStep == 5) "Submit Request" else "Review Next", fontWeight = FontWeight.Bold, fontSize = 16.sp)
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
            Text(when(currentStep) { 1 -> "Review Identity"; 2 -> "Review Specs"; 3 -> "Verify Materials"; 4 -> "Docs Attached"; else -> "Final Review" }, color = RailBlue, fontWeight = FontWeight.Bold, fontSize = 12.sp)
        }
        Spacer(modifier = Modifier.height(8.dp))
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            for (i in 1..5) Box(modifier = Modifier.weight(1f).height(4.dp).background(if (i <= currentStep) RailBlue else Color.LightGray.copy(alpha = 0.5f), RoundedCornerShape(2.dp)))
        }
    }
}