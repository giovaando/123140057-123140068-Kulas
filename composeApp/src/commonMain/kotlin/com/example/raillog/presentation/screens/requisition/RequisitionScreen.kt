package com.example.raillog.presentation.screens.requisition

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.raillog.core.util.rememberMediaPicker

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RequisitionScreen(
    viewModel: RequisitionViewModel,
    onNavigateBack: () -> Unit,
    onSubmissionSuccess: () -> Unit
) {
    // Mengamati perubahan state dari ViewModel
    val uiState by viewModel.uiState.collectAsState()

    // =========================================================================
    // PERBAIKAN UTAMA: Menerima 2 parameter (fileName dan base64Data)
    // =========================================================================
    val mediaPicker = rememberMediaPicker { fileName, base64Data ->
        if (base64Data != null) {
            // Jika sukses memilih file, kirim gambar ke Google Vision API
            viewModel.processInitialDocument(
                base64Image = base64Data,
                fileName = fileName
            )
        } else {
            println("Gagal membaca file atau proses dibatalkan pengguna.")
        }
    }
    // =========================================================================

    // Navigasi otomatis jika submit berhasil
    LaunchedEffect(uiState.submitSuccess) {
        if (uiState.submitSuccess) {
            onSubmissionSuccess()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Form Pengajuan Material") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Kembali")
                    }
                }
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Navigasi Layar: Jika belum scan, tampilkan menu upload. Jika sudah, tampilkan form.
            if (!uiState.hasScannedInitialDoc) {
                InitialScanSection(
                    isProcessingAI = uiState.isProcessingAI,
                    onOpenCamera = { mediaPicker.launchCamera() },
                    onOpenGallery = { mediaPicker.launchGallery() },
                    onSkip = { viewModel.skipInitialScan() }
                )
            } else {
                RequisitionFormSection(
                    uiState = uiState,
                    viewModel = viewModel
                )
            }

            // Menampilkan pesan error dari Ktor/API jika ada
            uiState.errorMessage?.let { errorMsg ->
                Snackbar(
                    modifier = Modifier
                        .padding(16.dp)
                        .align(Alignment.BottomCenter),
                    containerColor = MaterialTheme.colorScheme.errorContainer,
                    contentColor = MaterialTheme.colorScheme.onErrorContainer
                ) {
                    Text(errorMsg)
                }
            }

            // Indikator Loading saat submit data
            if (uiState.isSubmitting) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            }
        }
    }
}

@Composable
fun InitialScanSection(
    isProcessingAI: Boolean,
    onOpenCamera: () -> Unit,
    onOpenGallery: () -> Unit,
    onSkip: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        if (isProcessingAI) {
            CircularProgressIndicator(modifier = Modifier.size(64.dp))
            Spacer(modifier = Modifier.height(24.dp))
            Text("AI sedang menganalisis dokumen...", style = MaterialTheme.typography.bodyLarge)
            Text("Mohon tunggu sebentar.", style = MaterialTheme.typography.bodyMedium, color = Color.Gray)
        } else {
            Icon(
                imageVector = Icons.Default.DocumentScanner,
                contentDescription = "Scanner",
                modifier = Modifier.size(80.dp),
                tint = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text("Unggah Dokumen SPK", style = MaterialTheme.typography.headlineSmall)
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                "Sistem AI akan secara otomatis mengekstrak data material dari Surat Jalan Anda.",
                style = MaterialTheme.typography.bodyMedium,
                color = Color.Gray,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )

            Spacer(modifier = Modifier.height(40.dp))

            Button(
                onClick = onOpenCamera,
                modifier = Modifier.fillMaxWidth().height(50.dp)
            ) {
                Icon(Icons.Default.CameraAlt, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Buka Kamera")
            }

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedButton(
                onClick = onOpenGallery,
                modifier = Modifier.fillMaxWidth().height(50.dp)
            ) {
                Icon(Icons.Default.Image, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Upload PDF / Image")
            }

            Spacer(modifier = Modifier.height(24.dp))

            TextButton(onClick = onSkip) {
                Text("Lewati & Isi Form Manual")
            }
        }
    }
}

@Composable
fun RequisitionFormSection(
    uiState: RequisitionFormState,
    viewModel: RequisitionViewModel
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text("Data Informasi Proyek", style = MaterialTheme.typography.titleLarge)
            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = uiState.projectCode,
                onValueChange = { viewModel.updateProjectCode(it) },
                label = { Text("Kode Proyek") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
            OutlinedTextField(
                value = uiState.destinationSite,
                onValueChange = { viewModel.updateDestinationSite(it) },
                label = { Text("Tujuan Site (Depot)") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
            OutlinedTextField(
                value = uiState.requestorName,
                onValueChange = { viewModel.updateName(it) },
                label = { Text("Nama Pemohon") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
        }

        item {
            HorizontalDivider(modifier = Modifier.padding(vertical = 16.dp))
            Text("Daftar Kebutuhan Material", style = MaterialTheme.typography.titleLarge)
            Spacer(modifier = Modifier.height(8.dp))
        }

        items(uiState.catalogItems) { item ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = if (item.reqQty > 0) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                Row(
                    modifier = Modifier
                        .padding(16.dp)
                        .fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(item.name, style = MaterialTheme.typography.bodyLarge, fontWeight = androidx.compose.ui.text.font.FontWeight.Bold)
                        Text("Stok Gudang: ${item.stock}", style = MaterialTheme.typography.bodyMedium)
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(onClick = { viewModel.updateItemQuantity(item.id, isAdd = false) }) {
                            Icon(Icons.Default.RemoveCircleOutline, contentDescription = "Kurang")
                        }
                        Text(
                            text = "${item.reqQty}",
                            style = MaterialTheme.typography.titleMedium,
                            modifier = Modifier.padding(horizontal = 8.dp)
                        )
                        IconButton(onClick = { viewModel.updateItemQuantity(item.id, isAdd = true) }) {
                            Icon(Icons.Default.AddCircleOutline, contentDescription = "Tambah")
                        }
                    }
                }
            }
        }

        item {
            HorizontalDivider(modifier = Modifier.padding(vertical = 16.dp))
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Checkbox(
                    checked = uiState.isSigned,
                    onCheckedChange = { viewModel.setSignedStatus(it) }
                )
                Text("Saya mengonfirmasi data ini valid dan telah ditandatangani.", style = MaterialTheme.typography.bodyMedium)
            }

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = { viewModel.submitRequisition() },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(55.dp),
                enabled = !uiState.isSubmitting && uiState.isSigned && uiState.projectCode.isNotBlank()
            ) {
                Text("Submit Pengajuan Material")
            }
            Spacer(modifier = Modifier.height(40.dp))
        }
    }
}