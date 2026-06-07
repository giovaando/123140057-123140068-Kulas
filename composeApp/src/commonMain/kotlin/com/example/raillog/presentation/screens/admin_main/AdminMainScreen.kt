package com.example.raillog.presentation.screens.admin_main

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.FactCheck
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import com.example.raillog.domain.model.SupplyItem
import com.example.raillog.presentation.theme.RailLogColors
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import org.koin.compose.viewmodel.koinViewModel
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.material.icons.automirrored.filled.ArrowForward

/**
 * Format timestamp untuk Dashboard Admin
 */
fun formatAdminTimestamp(millis: Long): String {
    if (millis <= 0L) return "Baru saja"
    return try {
        val instant = Instant.fromEpochMilliseconds(millis)
        val dt = instant.toLocalDateTime(TimeZone.currentSystemDefault())
        "${dt.dayOfMonth.toString().padStart(2, '0')}/${dt.monthNumber.toString().padStart(2, '0')}"
    } catch (e: Exception) {
        "ID-$millis"
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminMainScreen(
    viewModel: AdminMainViewModel = koinViewModel(),
    onNavigateToVerificationDetail: (Long) -> Unit,
    onLogout: () -> Unit
) {
    var selectedTab by remember { mutableIntStateOf(1) }
    val allItems by viewModel.allItems.collectAsState()

    Scaffold(
        containerColor = RailLogColors.SurfaceSlate,
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier.size(36.dp).background(RailLogColors.PrimaryNavy, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.AdminPanelSettings, null, tint = Color.White, modifier = Modifier.size(20.dp))
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text("RailLog Control", fontWeight = FontWeight.ExtraBold, color = RailLogColors.PrimaryNavy, fontSize = 18.sp)
                            Text("PUSAT KENDALI AUDIT", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color.Black)
                        }
                    }
                },
                actions = {
                    IconButton(onClick = onLogout) {
                        Icon(Icons.AutoMirrored.Filled.Logout, "Logout", tint = RailLogColors.ErrorRed, modifier = Modifier.size(28.dp))
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
            )
        },
        bottomBar = {
            NavigationBar(containerColor = Color.White, tonalElevation = 8.dp) {
                val menuItems = listOf(
                    Triple("Inventaris", Icons.Default.Inventory, 0),
                    Triple("Verifikasi", Icons.AutoMirrored.Filled.FactCheck, 1),
                    Triple("Statistik", Icons.Default.QueryStats, 2)
                )
                menuItems.forEach { (label, icon, index) ->
                    NavigationBarItem(
                        selected = selectedTab == index,
                        onClick = { selectedTab = index },
                        label = { Text(label, fontWeight = FontWeight.Bold, fontSize = 11.sp) },
                        icon = { Icon(icon, null) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = RailLogColors.PrimaryNavy,
                            selectedTextColor = RailLogColors.PrimaryNavy,
                            unselectedTextColor = Color.Black,
                            indicatorColor = RailLogColors.AISurface
                        )
                    )
                }
            }
        }
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize().padding(padding)) {
            when (selectedTab) {
                0 -> AdminInventoryTab(allItems)
                1 -> AdminVerificationTab(viewModel, onNavigateToVerificationDetail)
                2 -> AdminOperationsTab(viewModel)
            }
        }
    }
}

@Composable
fun AdminInventoryTab(allItems: List<SupplyItem>) {
    LazyColumn(modifier = Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        item {
            Text("Kapasitas Gudang", fontSize = 20.sp, fontWeight = FontWeight.ExtraBold, color = RailLogColors.PrimaryNavy)
            Spacer(modifier = Modifier.height(8.dp))
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                border = BorderStroke(2.dp, Color.Black)
            ) {
                Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    AdminProgressItem("Infrastruktur", 0.75f)
                    AdminProgressItem("Sarana Kereta", 0.45f)
                    AdminProgressItem("Suku Cadang Elektronik", 0.12f)
                }
            }
        }

        item { 
            Text("Stok Kritis (< 15 Unit)", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = RailLogColors.ErrorRed)
        }
        
        val criticalItems = allItems.filter { it.quantity < 15 }
        if (criticalItems.isEmpty()) {
            item { Text("Stok komponen dalam kondisi optimal.", color = Color.Black, fontSize = 14.sp, fontWeight = FontWeight.Bold) }
        } else {
            items(criticalItems) { item ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    border = BorderStroke(2.dp, RailLogColors.ErrorRed)
                ) {
                    Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(item.name, fontWeight = FontWeight.ExtraBold, color = Color.Black, fontSize = 16.sp)
                            Text("SKU: ${item.partCode}", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.DarkGray)
                        }
                        Text("${item.quantity}", color = RailLogColors.ErrorRed, fontWeight = FontWeight.ExtraBold, fontSize = 24.sp)
                    }
                }
            }
        }
    }
}

@Composable
fun AdminProgressItem(label: String, progress: Float) {
    val isCritical = progress < 0.2f
    Column {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(label, fontWeight = FontWeight.Bold, color = Color.Black, fontSize = 14.sp)
            Text("${(progress * 100).toInt()}%", fontWeight = FontWeight.ExtraBold, color = if(isCritical) RailLogColors.ErrorRed else RailLogColors.PrimaryNavy)
        }
        Spacer(modifier = Modifier.height(6.dp))
        LinearProgressIndicator(
            progress = { progress },
            modifier = Modifier.fillMaxWidth().height(12.dp).clip(RoundedCornerShape(6.dp)).border(1.dp, Color.Black, RoundedCornerShape(6.dp)),
            color = if(isCritical) RailLogColors.ErrorRed else RailLogColors.PrimaryNavy,
            trackColor = Color.LightGray.copy(alpha = 0.3f)
        )
    }
}

@Composable
fun AdminVerificationTab(viewModel: AdminMainViewModel, onDetail: (Long) -> Unit) {
    val query by viewModel.searchQuery.collectAsState()
    val items by viewModel.filteredPendingItems.collectAsState()

    Column(modifier = Modifier.fillMaxSize()) {
        Box(modifier = Modifier.background(RailLogColors.PrimaryNavy).fillMaxWidth().padding(24.dp)) {
            Column {
                Text("Antrean Audit Logistik", color = Color.White, fontSize = 24.sp, fontWeight = FontWeight.ExtraBold)
                Text("Validasi pengajuan staff dengan mesin AI Gemini", color = Color.White.copy(alpha = 0.8f), fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }
        }

        LazyColumn(contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            item {
                OutlinedTextField(
                    value = query, onValueChange = { viewModel.updateSearchQuery(it) },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("Cari berdasarkan nama pengajuan...", color = Color.Black) },
                    leadingIcon = { Icon(Icons.Default.Search, null, tint = Color.Black) },
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = Color.White, unfocusedContainerColor = Color.White,
                        unfocusedBorderColor = Color.Black, focusedBorderColor = RailLogColors.PrimaryNavy,
                        focusedTextColor = Color.Black, unfocusedTextColor = Color.Black
                    )
                )
            }

            items(items) { item ->
                Card(
                    modifier = Modifier.fillMaxWidth().clickable { onDetail(item.id) },
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    border = BorderStroke(2.dp, Color.Black)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("ID: REQ-${item.id}", fontWeight = FontWeight.ExtraBold, color = RailLogColors.PrimaryNavy, fontSize = 14.sp)
                            Text(formatAdminTimestamp(item.createdAt.toEpochMilliseconds()), fontSize = 10.sp, fontWeight = FontWeight.ExtraBold, color = Color.DarkGray)
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(item.name, fontWeight = FontWeight.ExtraBold, fontSize = 18.sp, color = Color.Black)
                        Text("Estimasi Kuantitas: ${item.quantity} unit", fontSize = 13.sp, color = Color.Black, fontWeight = FontWeight.Bold)
                        
                        Spacer(modifier = Modifier.height(12.dp))
                        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                            Box(modifier = Modifier.background(RailLogColors.SuccessBackground, RoundedCornerShape(6.dp)).padding(horizontal = 10.dp, vertical = 6.dp).weight(1f)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.AutoAwesome, null, tint = RailLogColors.SuccessEmerald, modifier = Modifier.size(14.dp))
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("ANALISIS AI SIAP", fontSize = 11.sp, color = RailLogColors.SuccessEmerald, fontWeight = FontWeight.ExtraBold)
                                }
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Icon(Icons.AutoMirrored.Filled.ArrowForward, null, tint = Color.Black, modifier = Modifier.size(20.dp))
                        }
                    }
                }
            }
            if (items.isEmpty()) {
                item {
                    Box(modifier = Modifier.fillMaxWidth().padding(60.dp), contentAlignment = Alignment.Center) {
                        Text("Tidak ada antrean audit saat ini.", fontWeight = FontWeight.Bold, color = Color.Black)
                    }
                }
            }
            item { Spacer(modifier = Modifier.height(60.dp)) }
        }
    }
}

@Composable
fun AdminOperationsTab(viewModel: AdminMainViewModel) {
    val allItems by viewModel.allItems.collectAsState()
    val pendingCount by viewModel.pendingRequisitions.collectAsState()
    val criticalPendingCount by viewModel.criticalPendingCount.collectAsState()
    val aiConfidence by viewModel.averageAiConfidence.collectAsState()
    
    val verifiedCount = allItems.count { it.status.name == "VERIFIED" }

    LazyColumn(modifier = Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(20.dp)) {
        item {
            Text("Kontrol Operasional", fontSize = 22.sp, fontWeight = FontWeight.ExtraBold, color = RailLogColors.PrimaryNavy)
            Spacer(modifier = Modifier.height(8.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                AdminMetricCard("TOTAL VERIFIKASI", verifiedCount.toString(), RailLogColors.SuccessEmerald, Modifier.weight(1f))
                AdminMetricCard("AUDIT PENDING", pendingCount.size.toString(), if(pendingCount.isNotEmpty()) RailLogColors.WarningAmber else RailLogColors.SuccessEmerald, Modifier.weight(1f))
            }
            Spacer(modifier = Modifier.height(12.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                AdminMetricCard("KRITIS (PENDING)", criticalPendingCount.toString(), if(criticalPendingCount > 0) RailLogColors.ErrorRed else RailLogColors.SuccessEmerald, Modifier.weight(1f))
                AdminMetricCard("AVG AI CONFIDENCE", "$aiConfidence%", RailLogColors.PrimaryNavy, Modifier.weight(1f))
            }
        }

        item {
            Text("Integritas Sistem", fontSize = 18.sp, fontWeight = FontWeight.ExtraBold, color = Color.Black)
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                border = BorderStroke(2.dp, Color.Black)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    AdminStatusRow("Audit Engine DB", "SINKRON", RailLogColors.SuccessEmerald)
                    HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp), color = Color.Black.copy(alpha = 0.1f))
                    AdminStatusRow("Gemini AI Core", "AKTIF", RailLogColors.SuccessEmerald)
                    HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp), color = Color.Black.copy(alpha = 0.1f))
                    AdminStatusRow("Pusat Notifikasi", "SIAGA", RailLogColors.SuccessEmerald)
                }
            }
        }
    }
}

@Composable
fun AdminMetricCard(label: String, value: String, color: Color, modifier: Modifier) {
    Card(modifier = modifier, colors = CardDefaults.cardColors(containerColor = Color.White), border = BorderStroke(2.dp, Color.Black)) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(label, fontSize = 10.sp, fontWeight = FontWeight.ExtraBold, color = Color.DarkGray)
            Text(value, fontSize = 28.sp, fontWeight = FontWeight.ExtraBold, color = color)
        }
    }
}

@Composable
fun AdminStatusRow(label: String, status: String, color: Color) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
        Text(label, fontWeight = FontWeight.Bold, color = Color.Black, fontSize = 14.sp)
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(modifier = Modifier.size(8.dp).background(color, CircleShape))
            Spacer(modifier = Modifier.width(6.dp))
            Text(status, color = color, fontWeight = FontWeight.ExtraBold, fontSize = 12.sp)
        }
    }
}
