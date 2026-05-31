package com.example.raillog.presentation.screens.staff_main

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.raillog.domain.model.SupplyItem
import org.koin.compose.viewmodel.koinViewModel // Koin Injector

// --- PALET WARNA ---
val RailBlue = Color(0xFF193255)
val RailLightBlue = Color(0xFFF0F4FA)
val SafeGreen = Color(0xFF10B981)
val SafeGreenBg = Color(0xFFD1FAE5)
val CriticalRed = Color(0xFFEF4444)
val CriticalRedBg = Color(0xFFFEE2E2)
val MutedText = Color(0xFF6B7280)

sealed class BottomNavItem(val title: String, val icon: ImageVector) {
    data object Home : BottomNavItem("Home", Icons.Default.Home)
    data object Requests : BottomNavItem("Requests", Icons.Default.Assignment)
    data object Inventory : BottomNavItem("Inventory", Icons.Default.Inventory)
    data object History : BottomNavItem("History", Icons.Default.History)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StaffMainScreen(
    viewModel: StaffMainViewModel = koinViewModel(), // Otak dinamis
    onNavigateToNewRequisition: () -> Unit
) {
    // BACA DATA ASLI DARI DATABASE
    val userRole by viewModel.activeUserRole.collectAsState()
    val allItems by viewModel.allSupplyItems.collectAsState()

    var selectedTab by remember { mutableStateOf(0) }
    val tabs = listOf(BottomNavItem.Home, BottomNavItem.Requests, BottomNavItem.Inventory, BottomNavItem.History)

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(modifier = Modifier.size(32.dp).background(Color.LightGray, CircleShape), contentAlignment = Alignment.Center) {
                            Text("img", fontSize = 10.sp, color = Color.White)
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Text("RailLog Nusantara", fontWeight = FontWeight.Bold, color = RailBlue)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
            )
        },
        bottomBar = {
            NavigationBar(containerColor = Color.White, tonalElevation = 8.dp) {
                tabs.forEachIndexed { index, item ->
                    NavigationBarItem(
                        icon = { Icon(item.icon, contentDescription = item.title) },
                        label = { Text(item.title, fontSize = 10.sp) },
                        selected = selectedTab == index,
                        onClick = { selectedTab = index },
                        colors = NavigationBarItemDefaults.colors(selectedIconColor = RailBlue, selectedTextColor = RailBlue, indicatorColor = RailLightBlue)
                    )
                }
            }
        }
    ) { paddingValues ->
        Box(modifier = Modifier.fillMaxSize().padding(paddingValues).background(Color(0xFFFAFAFA))) {
            when (selectedTab) {
                // Lempar data asli ke dalam Tab
                0 -> StaffHomeTab(userRole, allItems, onNavigateToNewRequisition)
                1 -> StaffRequestsTab() // Biarkan dulu
                2 -> StaffInventoryTab(allItems) // Lempar data ke tab katalog
                3 -> StaffHistoryTab() // Biarkan dulu
            }
        }
    }
}

// ==========================================
// 1. TAB HOME (SEKARANG DINAMIS)
// ==========================================
@Composable
fun StaffHomeTab(userRole: String, allItems: List<SupplyItem>, onStartNew: () -> Unit) {
    // KALKULASI METRIK ASLI DARI DATABASE
    val pendingCount = allItems.count { it.status.name == "PENDING" }
    val criticalCount = allItems.count { it.priority.name == "CRITICAL" || it.priority.name == "HIGH" }

    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(8.dp))
            // Menampilkan nama role dinamis
            Text("Good Morning, $userRole", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = RailBlue)
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(top = 4.dp)) {
                Box(modifier = Modifier.size(8.dp).background(if(pendingCount > 0) CriticalRed else SafeGreen, CircleShape))
                Spacer(modifier = Modifier.width(8.dp))
                // Teks dinamis
                Text("$pendingCount Pending Requisitions require attention", color = MutedText, fontSize = 14.sp)
            }
        }

        item {
            Card(onClick = onStartNew, modifier = Modifier.fillMaxWidth().height(88.dp), colors = CardDefaults.cardColors(containerColor = RailBlue), shape = RoundedCornerShape(12.dp)) {
                Row(modifier = Modifier.fillMaxSize().padding(horizontal = 20.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
                    Column {
                        Text("Start New Requisition", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                        Text("Initiate a 5-step material request", color = Color.White.copy(alpha = 0.8f), fontSize = 12.sp)
                    }
                    Icon(Icons.Default.Add, null, tint = Color.White, modifier = Modifier.size(28.dp))
                }
            }
        }

        item {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                // Angka dinamis dari database
                MetricCard(modifier = Modifier.weight(1f), icon = Icons.Default.Assignment, title = "Total Items", count = allItems.size.toString(), iconTint = MutedText)
                MetricCard(modifier = Modifier.weight(1f), icon = Icons.Default.WarningAmber, title = "High Priority", count = criticalCount.toString(), iconTint = CriticalRed, iconBg = CriticalRedBg)
            }
        }

        item {
            Text("Recent Activity (From Database)", fontSize = 18.sp, fontWeight = FontWeight.Medium)
            Spacer(modifier = Modifier.height(8.dp))

            if (allItems.isEmpty()) {
                Text("No activity yet. Start a new requisition!", color = MutedText, modifier = Modifier.padding(vertical = 16.dp))
            }
        }

        // TAMPILKAN DAFTAR ASLI DARI DATABASE (Dibalik agar yang terbaru di atas)
        items(allItems.reversed().take(5)) { item ->
            ActivityItemCard(
                id = item.partCode,
                title = item.name,
                status = item.status.name,
                icon = if(item.status.name == "PENDING") Icons.Default.Schedule else Icons.Default.CheckCircle
            )
            Spacer(modifier = Modifier.height(8.dp))
        }
    }
}

// ==========================================
// 3. TAB INVENTORY (SEKARANG DINAMIS)
// ==========================================
@Composable
fun StaffInventoryTab(allItems: List<SupplyItem>) {
    Column(modifier = Modifier.fillMaxSize()) {
        OutlinedTextField(
            value = "", onValueChange = {},
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            placeholder = { Text("Search by name or ID...") },
            leadingIcon = { Icon(Icons.Default.Search, null) },
            shape = RoundedCornerShape(8.dp),
            colors = OutlinedTextFieldDefaults.colors(unfocusedContainerColor = Color.White)
        )

        LazyColumn(contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            if (allItems.isEmpty()) {
                item { Text("Database kosong. Silakan tambah data via Start New Requisition.") }
            }
            // Loop data asli dari SQLDelight
            items(allItems) { item ->
                InventoryItemCard(
                    name = item.name,
                    id = item.partCode,
                    loc = "Depot System",
                    qty = item.quantity.toString(),
                    status = item.priority.name,
                    statusBg = if(item.priority.name == "HIGH") CriticalRedBg else SafeGreenBg,
                    statusColor = if(item.priority.name == "HIGH") CriticalRed else SafeGreen
                )
            }
        }
    }
}

// --- TAB LAIN TETAP DUMMY (SISA) ---
@Composable
fun StaffRequestsTab() { Box(modifier=Modifier.fillMaxSize(), contentAlignment=Alignment.Center){ Text("Drafts & In Progress") } }
@Composable
fun StaffHistoryTab() { Box(modifier=Modifier.fillMaxSize(), contentAlignment=Alignment.Center){ Text("History (Approved/Completed)") } }

// --- KOMPONEN KECIL ---
@Composable
fun MetricCard(modifier: Modifier = Modifier, icon: ImageVector, title: String, count: String, iconTint: Color, iconBg: Color = Color.LightGray.copy(alpha = 0.2f)) {
    Card(modifier = modifier, colors = CardDefaults.cardColors(containerColor = Color.White), border = BorderStroke(1.dp, Color.LightGray.copy(alpha = 0.5f))) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Box(modifier = Modifier.size(32.dp).background(iconBg, RoundedCornerShape(8.dp)), contentAlignment = Alignment.Center) { Icon(icon, null, tint = iconTint, modifier = Modifier.size(18.dp)) }
                Text(count, fontWeight = FontWeight.Bold, fontSize = 24.sp, color = RailBlue)
            }
            Spacer(modifier = Modifier.height(16.dp))
            Text(title, fontSize = 12.sp, color = MutedText, fontWeight = FontWeight.Medium)
        }
    }
}

@Composable
fun ActivityItemCard(id: String, title: String, status: String, icon: ImageVector) {
    Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = Color.White), border = BorderStroke(1.dp, Color.LightGray.copy(alpha = 0.5f))) {
        Row(modifier = Modifier.fillMaxWidth().padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(modifier = Modifier.size(40.dp).background(if(status=="PENDING") Color(0xFFFEF3C7) else SafeGreenBg, RoundedCornerShape(8.dp)), contentAlignment = Alignment.Center) {
                Icon(icon, null, tint = if(status=="PENDING") Color(0xFFD97706) else SafeGreen)
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(title, fontWeight = FontWeight.Bold, fontSize = 14.sp, maxLines = 1)
                Text("Code: $id | Status: $status", color = MutedText, fontSize = 12.sp)
            }
        }
    }
}

@Composable
fun InventoryItemCard(name: String, id: String, loc: String, qty: String, status: String, statusBg: Color, statusColor: Color) {
    Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = Color.White), border = BorderStroke(1.dp, Color.LightGray.copy(alpha = 0.5f))) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.Top) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(name, fontWeight = FontWeight.Bold, fontSize = 14.sp, maxLines = 1)
                    Text("ID: $id", color = MutedText, fontSize = 12.sp)
                }
                Box(modifier = Modifier.background(statusBg, RoundedCornerShape(4.dp)).padding(horizontal = 8.dp, vertical = 4.dp)) {
                    Text(status, fontSize = 10.sp, color = statusColor, fontWeight = FontWeight.Bold)
                }
            }
            Divider(modifier = Modifier.padding(vertical = 12.dp), color = Color.LightGray.copy(alpha = 0.3f))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.Bottom) {
                Column {
                    Text("LOCATION", fontSize = 10.sp, color = MutedText, fontWeight = FontWeight.Bold)
                    Text(loc, fontSize = 12.sp, color = MutedText)
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text("QTY", fontSize = 10.sp, color = MutedText, fontWeight = FontWeight.Bold)
                    Text(qty, fontSize = 20.sp, fontWeight = FontWeight.Bold, color = RailBlue)
                }
            }
        }
    }
}