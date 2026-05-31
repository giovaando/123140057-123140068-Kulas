package com.example.raillog.presentation.screens.admin_main

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.raillog.domain.model.SupplyItem
import org.koin.compose.viewmodel.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VerificationDetailScreen(
    requisitionId: Long,
    onNavigateBack: () -> Unit,
    viewModel: VerificationDetailViewModel = koinViewModel()
) {
    val item by viewModel.selectedItem.collectAsState()

    LaunchedEffect(requisitionId) { viewModel.loadItem(requisitionId) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    // ID Dinamis berdasarkan data
                    Text("Verify: PRJ-2026-${item?.id ?: "..."}", fontWeight = FontWeight.Bold, color = RailBlue)
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = RailBlue) }
                },
                actions = {
                    IconButton(onClick = { /* History */ }) { Icon(Icons.Default.History, contentDescription = "History", tint = RailBlue) }
                    IconButton(onClick = { /* Menu */ }) { Icon(Icons.Default.MoreVert, contentDescription = "Menu", tint = RailBlue) }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
            )
        },
        bottomBar = {
            Surface(shadowElevation = 16.dp, color = Color.White, modifier = Modifier.fillMaxWidth()) {
                Row(modifier = Modifier.padding(16.dp).fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedButton(
                        onClick = { viewModel.rejectItem(onSuccess = onNavigateBack) },
                        modifier = Modifier.weight(1f).height(50.dp),
                        shape = RoundedCornerShape(8.dp),
                        border = borderStroke(1.dp, RailBlue)
                    ) {
                        Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(18.dp), tint = RailBlue)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Revise", color = RailBlue, fontWeight = FontWeight.Bold)
                    }
                    Button(
                        onClick = { viewModel.verifyItem(onSuccess = onNavigateBack) },
                        modifier = Modifier.weight(1f).height(50.dp),
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = RailBlue)
                    ) {
                        Icon(Icons.Default.Verified, contentDescription = null, modifier = Modifier.size(18.dp), tint = Color.White)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Verify", color = Color.White, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    ) { paddingValues ->
        if (item == null) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator(color = RailBlue) }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .background(Color.Black)
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp)
            ) {
                SourceDocumentCard()
                Spacer(modifier = Modifier.height(16.dp))
                ExtractionResultsCard(item!!)
                Spacer(modifier = Modifier.height(32.dp))
            }
        }
    }
}

@Composable
fun SourceDocumentCard() {
    Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp), colors = CardDefaults.cardColors(containerColor = Color.White)) {
        Column {
            Row(modifier = Modifier.fillMaxWidth().padding(12.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Description, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("SOURCE DOCUMENT", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.Gray)
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.ZoomIn, contentDescription = null, tint = RailBlue, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Expand", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = RailBlue)
                }
            }
            Box(modifier = Modifier.fillMaxWidth().height(200.dp).background(Color.LightGray)) {
                Icon(Icons.Default.Image, contentDescription = null, tint = Color.DarkGray, modifier = Modifier.align(Alignment.Center).size(48.dp))
                Box(modifier = Modifier.align(Alignment.BottomEnd).padding(8.dp).background(Color.Black.copy(alpha = 0.7f), RoundedCornerShape(4.dp)).padding(horizontal = 8.dp, vertical = 4.dp)) {
                    Text("Page 1 of 3", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
fun ExtractionResultsCard(item: SupplyItem) {
    Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp), colors = CardDefaults.cardColors(containerColor = Color.White)) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.DocumentScanner, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("EXTRACTION RESULTS", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.Gray)
                }
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.background(Color(0xFFECFDF5), RoundedCornerShape(4.dp)).padding(horizontal = 8.dp, vertical = 4.dp)) {
                    Icon(Icons.Default.Bolt, contentDescription = null, tint = SafeGreen, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("98% Match", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = SafeGreen)
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
            HorizontalDivider(color = Color(0xFFF3F4F6))
            Row(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)) {
                Text("FIELD", modifier = Modifier.weight(1f), fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color.Gray)
                Text("EXTRACTED VALUE", modifier = Modifier.weight(1.5f), fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color.Gray)
                Text("STATUS", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color.Gray)
            }
            HorizontalDivider(color = Color(0xFFF3F4F6))

            // MENGGUNAKAN DATA DINAMIS DARI ITEM
            ExtractionRow("Material Name", item.name, isSuccess = true)
            ExtractionRow("Quantity", "${item.quantity} Units", isSuccess = true)

            // Simulasi error pada kode vendor untuk menunjukkan fitur AI
            Row(modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp), verticalAlignment = Alignment.CenterVertically) {
                Text("Part Code", modifier = Modifier.weight(1f), fontSize = 14.sp, color = CriticalRed, fontWeight = FontWeight.Medium)
                Column(modifier = Modifier.weight(1.5f)) {
                    Text(item.partCode.ifEmpty { "N/A" }, fontSize = 14.sp, color = CriticalRed, fontWeight = FontWeight.Bold)
                    Text("VND-99A", fontSize = 14.sp, color = Color.Gray, textDecoration = TextDecoration.LineThrough)
                }
                Icon(Icons.Default.Warning, contentDescription = null, tint = CriticalRed, modifier = Modifier.size(20.dp))
            }
            HorizontalDivider(color = Color(0xFFF3F4F6))
            ExtractionRow("Category", item.category.name.replace("_", " "), isSuccess = true)
        }
    }
}

@Composable
fun ExtractionRow(label: String, value: String, isSuccess: Boolean) {
    Row(modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp), verticalAlignment = Alignment.CenterVertically) {
        Text(label, modifier = Modifier.weight(1f), fontSize = 14.sp, color = Color.DarkGray)
        Text(value, modifier = Modifier.weight(1.5f), fontSize = 14.sp, color = Color.Black, fontWeight = FontWeight.Medium)
        Icon(if (isSuccess) Icons.Default.CheckCircle else Icons.Default.Cancel, contentDescription = null, tint = if (isSuccess) SafeGreen else CriticalRed, modifier = Modifier.size(20.dp))
    }
    HorizontalDivider(color = Color(0xFFF3F4F6))
}

fun borderStroke(width: androidx.compose.ui.unit.Dp, color: Color) = androidx.compose.foundation.BorderStroke(width, color)