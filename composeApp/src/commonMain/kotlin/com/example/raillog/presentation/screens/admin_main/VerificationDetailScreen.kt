package com.example.raillog.presentation.screens.admin_main

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.raillog.presentation.theme.RailLogColors
import org.koin.compose.viewmodel.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VerificationDetailScreen(
    requisitionId: Long,
    onNavigateBack: () -> Unit,
    viewModel: VerificationDetailViewModel = koinViewModel()
) {
    LaunchedEffect(requisitionId) {
        viewModel.loadItem(requisitionId)
    }

    val item by viewModel.selectedItem.collectAsState()
    val aiValidation by viewModel.aiValidation.collectAsState()

    if (item == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator(color = RailLogColors.PrimaryNavy)
        }
        return
    }

    val currentItem = item!!

    Scaffold(
        containerColor = RailLogColors.SurfaceSlate,
        topBar = {
            TopAppBar(
                title = { Text("Audit Verifikasi Logistik", fontWeight = FontWeight.ExtraBold, color = RailLogColors.PrimaryNavy) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = RailLogColors.PrimaryNavy)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = RailLogColors.SurfaceSlate)
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding).padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = RailLogColors.PrimaryNavy),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Text("REQUISITION ID: #${currentItem.id}", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold, color = Color.White.copy(alpha = 0.9f))
                        Text(currentItem.name, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.ExtraBold, color = Color.White)
                        Spacer(modifier = Modifier.height(12.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            StatusChip(currentItem.status.name, RailLogColors.PrimaryNavyLight, Color.White)
                            StatusChip(currentItem.priority.name, RailLogColors.ErrorRed, Color.White)
                        }
                    }
                }
            }

            item { Text("Data Teknis & Proyek", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.ExtraBold, color = RailLogColors.PrimaryNavy) }

            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = RailLogColors.SurfaceWhite), 
                    shape = RoundedCornerShape(12.dp), 
                    border = BorderStroke(2.dp, RailLogColors.BorderBlack)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        VerificationField(label = "KODE PROYEK", value = currentItem.partCode, valid = currentItem.partCode.isNotBlank())
                        VerificationField(label = "KOMPONEN", value = currentItem.name, valid = true)
                        VerificationField(label = "KUANTITAS", value = "${currentItem.quantity} ${currentItem.unit}", valid = currentItem.quantity > 0)
                        VerificationField(label = "SUPPLIER", value = currentItem.supplier, valid = currentItem.supplier.isNotBlank())
                        VerificationField(label = "CATATAN JUSTIFIKASI", value = currentItem.notes.ifBlank { "Tidak ada catatan." }, valid = true, isLast = true)
                    }
                }
            }

            item {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.AutoAwesome, null, tint = RailLogColors.PrimaryNavy, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Analisis Kelayakan AI", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.ExtraBold, color = RailLogColors.PrimaryNavy)
                    }
                    if (aiValidation.status != AIValidationStatus.LOADING) {
                        IconButton(onClick = { viewModel.retryAiValidation() }) {
                            Icon(Icons.Default.Refresh, null, tint = RailLogColors.PrimaryNavy)
                        }
                    }
                }
            }

            item { AIValidationPanel(aiValidation = aiValidation) }

            item {
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.padding(bottom = 32.dp)) {
                    OutlinedButton(
                        modifier = Modifier.weight(1f).height(56.dp),
                        onClick = { viewModel.rejectItem { onNavigateBack() } },
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = RailLogColors.ErrorRed),
                        border = BorderStroke(2.dp, RailLogColors.ErrorRed)
                    ) {
                        Icon(Icons.Default.Warning, null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("REJECT", fontWeight = FontWeight.ExtraBold)
                    }

                    Button(
                        modifier = Modifier.weight(1f).height(56.dp),
                        onClick = { viewModel.verifyItem { onNavigateBack() } },
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = RailLogColors.SuccessEmerald, contentColor = Color.White)
                    ) {
                        Icon(Icons.Default.CheckCircle, null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("APPROVE", fontWeight = FontWeight.ExtraBold)
                    }
                }
            }
        }
    }
}

@Composable
private fun AIValidationPanel(aiValidation: AIValidationState) {
    when (aiValidation.status) {
        AIValidationStatus.LOADING -> {
            Card(
                colors = CardDefaults.cardColors(containerColor = RailLogColors.AISurface), 
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.dp, RailLogColors.AIBorder)
            ) {
                Row(modifier = Modifier.fillMaxWidth().padding(20.dp), verticalAlignment = Alignment.CenterVertically) {
                    CircularProgressIndicator(modifier = Modifier.size(22.dp), strokeWidth = 3.dp, color = RailLogColors.PrimaryNavy)
                    Spacer(modifier = Modifier.width(14.dp))
                    Text("Gemini AI sedang meninjau kelayakan...", fontWeight = FontWeight.Bold, color = RailLogColors.PrimaryNavy, fontSize = 14.sp)
                }
            }
        }
        AIValidationStatus.SUCCESS -> {
            val res = aiValidation.result ?: ""
            val (bgColor, labelColor, statusLabel) = when {
                res.contains("TIDAK VALID", true) -> Triple(RailLogColors.ErrorBackground, RailLogColors.ErrorRed, "TIDAK VALID")
                res.contains("PERLU REVIEW", true) -> Triple(RailLogColors.WarningBackground, RailLogColors.WarningAmber, "PERLU REVIEW")
                else -> Triple(RailLogColors.SuccessBackground, RailLogColors.SuccessEmerald, "VALID")
            }
            AnimatedVisibility(visible = true, enter = fadeIn() + slideInVertically { it / 2 }) {
                Card(
                    colors = CardDefaults.cardColors(containerColor = bgColor), 
                    shape = RoundedCornerShape(12.dp), 
                    border = BorderStroke(2.dp, labelColor)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.AutoAwesome, null, tint = labelColor, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(statusLabel, color = labelColor, fontWeight = FontWeight.ExtraBold, fontSize = 13.sp)
                        }
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(res, color = Color.Black, fontSize = 14.sp, lineHeight = 20.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
        AIValidationStatus.ERROR -> {
            Card(colors = CardDefaults.cardColors(containerColor = RailLogColors.ErrorBackground), shape = RoundedCornerShape(12.dp)) {
                Text(aiValidation.errorMessage ?: "Kritikal: Gagal menghubungi AI.", color = RailLogColors.ErrorRed, modifier = Modifier.padding(16.dp), fontWeight = FontWeight.Bold)
            }
        }
        else -> {}
    }
}

@Composable
private fun StatusChip(text: String, bgColor: Color, textColor: Color) {
    Box(modifier = Modifier.background(bgColor, RoundedCornerShape(6.dp)).padding(horizontal = 10.dp, vertical = 4.dp)) {
        Text(text, color = textColor, fontSize = 11.sp, fontWeight = FontWeight.ExtraBold)
    }
}

@Composable
private fun VerificationField(label: String, value: String, valid: Boolean, isLast: Boolean = false) {
    Row(modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
        Column(modifier = Modifier.weight(1f)) {
            Text(label, style = MaterialTheme.typography.labelSmall, color = Color.Black, fontWeight = FontWeight.ExtraBold)
            Text(value, fontWeight = FontWeight.Bold, color = RailLogColors.PrimaryNavy, fontSize = 15.sp)
        }
        if (!valid) Icon(Icons.Default.Warning, null, tint = RailLogColors.ErrorRed, modifier = Modifier.size(22.dp))
    }
    if (!isLast) HorizontalDivider(color = Color.Black.copy(alpha = 0.2f), thickness = 1.dp)
}
