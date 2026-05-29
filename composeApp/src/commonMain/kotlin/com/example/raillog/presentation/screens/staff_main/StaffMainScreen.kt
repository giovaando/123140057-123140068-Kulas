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

// --- PALET WARNA SESUAI DESAIN ---
val RailBlue = Color(0xFF193255)
val RailLightBlue = Color(0xFFF0F4FA)
val SafeGreen = Color(0xFF10B981)
val SafeGreenBg = Color(0xFFD1FAE5)
val CriticalRed = Color(0xFFEF4444)
val CriticalRedBg = Color(0xFFFEE2E2)
val MutedText = Color(0xFF6B7280)

// --- DEFINISI MENU BAWAH ---
sealed class BottomNavItem(val title: String, val icon: ImageVector) {
    data object Home : BottomNavItem("Home", Icons.Default.Home)
    data object Requests : BottomNavItem("Requests", Icons.Default.Assignment)
    data object Inventory : BottomNavItem("Inventory", Icons.Default.Inventory)
    data object History : BottomNavItem("History", Icons.Default.History)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StaffMainScreen(
    onNavigateToNewRequisition: () -> Unit
) {
    var selectedTab by remember { mutableStateOf(0) }
    val items = listOf(
        BottomNavItem.Home, BottomNavItem.Requests,
        BottomNavItem.Inventory, BottomNavItem.History
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier.size(32.dp).background(Color.LightGray, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("img", fontSize = 10.sp, color = Color.White)
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Text("RailLog Nusantara", fontWeight = FontWeight.Bold, color = RailBlue)
                    }
                },
                actions = {
                    IconButton(onClick = {}) {
                        Icon(Icons.Default.NotificationsNone, contentDescription = "Notification", tint = RailBlue)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
            )
        },
        bottomBar = {
            NavigationBar(containerColor = Color.White, tonalElevation = 8.dp) {
                items.forEachIndexed { index, item ->
                    NavigationBarItem(
                        icon = { Icon(item.icon, contentDescription = item.title) },
                        label = { Text(item.title, fontSize = 10.sp) },
                        selected = selectedTab == index,
                        onClick = { selectedTab = index },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = RailBlue,
                            selectedTextColor = RailBlue,
                            indicatorColor = RailLightBlue
                        )
                    )
                }
            }
        }
    ) { paddingValues ->
        Box(modifier = Modifier.fillMaxSize().padding(paddingValues).background(Color(0xFFFAFAFA))) {
            when (selectedTab) {
                0 -> StaffHomeTab(onNavigateToNewRequisition)
                1 -> StaffRequestsTab()
                2 -> StaffInventoryTab()
                3 -> StaffHistoryTab()
            }
        }
    }
}

// ==========================================
// 1. TAB HOME
// ==========================================
@Composable
fun StaffHomeTab(onStartNew: () -> Unit) {
    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(8.dp))
            Text("Good Morning, Ahmad", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = RailBlue)
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(top = 4.dp)) {
                Box(modifier = Modifier.size(8.dp).background(SafeGreen, CircleShape))
                Spacer(modifier = Modifier.width(8.dp))
                Text("3 Pending Requisitions require attention", color = MutedText, fontSize = 14.sp)
            }
        }

        item {
            Card(
                onClick = onStartNew,
                modifier = Modifier.fillMaxWidth().height(88.dp),
                colors = CardDefaults.cardColors(containerColor = RailBlue),
                shape = RoundedCornerShape(12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxSize().padding(horizontal = 20.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text("Start New Requisition", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                        Text("Initiate a 5-step material request", color = Color.White.copy(alpha = 0.8f), fontSize = 12.sp)
                    }
                    Icon(Icons.Default.Add, contentDescription = null, tint = Color.White, modifier = Modifier.size(28.dp))
                }
            }
        }

        item {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                MetricCard(modifier = Modifier.weight(1f), icon = Icons.Default.Assignment, title = "Active Requests", count = "12", iconTint = MutedText)
                MetricCard(modifier = Modifier.weight(1f), icon = Icons.Default.WarningAmber, title = "Inventory Alerts", count = "4", iconTint = CriticalRed, iconBg = CriticalRedBg)
            }
        }

        item {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text("Recent Activity", fontSize = 18.sp, fontWeight = FontWeight.Medium)
                Text("View All", fontSize = 14.sp, color = RailBlue, fontWeight = FontWeight.Medium)
            }
            Spacer(modifier = Modifier.height(8.dp))
            ActivityItemCard(id = "REQ-2023-089", status = "Awaiting Approval", time = "2h ago", icon = Icons.Default.Schedule)
            Spacer(modifier = Modifier.height(8.dp))
            ActivityItemCard(id = "REQ-2023-088", status = "Approved", time = "5h ago", icon = Icons.Default.CheckCircleOutline, iconTint = SafeGreen, iconBg = SafeGreenBg)
            Spacer(modifier = Modifier.height(8.dp))
            ActivityItemCard(id = "RCV-2023-042", status = "Items Received", time = "1d ago", icon = Icons.Default.Inventory2)
        }
    }
}

@Composable
fun MetricCard(modifier: Modifier = Modifier, icon: ImageVector, title: String, count: String, iconTint: Color, iconBg: Color = Color.LightGray.copy(alpha = 0.2f)) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, Color.LightGray.copy(alpha = 0.5f))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Box(modifier = Modifier.size(32.dp).background(iconBg, RoundedCornerShape(8.dp)), contentAlignment = Alignment.Center) {
                    Icon(icon, contentDescription = null, tint = iconTint, modifier = Modifier.size(18.dp))
                }
                Text(count, fontWeight = FontWeight.Bold, fontSize = 24.sp, color = RailBlue)
            }
            Spacer(modifier = Modifier.height(16.dp))
            Text(title, fontSize = 12.sp, color = MutedText, fontWeight = FontWeight.Medium)
        }
    }
}

@Composable
fun ActivityItemCard(id: String, status: String, time: String, icon: ImageVector, iconTint: Color = MutedText, iconBg: Color = Color.LightGray.copy(alpha = 0.2f)) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, Color.LightGray.copy(alpha = 0.5f))
    ) {
        Row(modifier = Modifier.fillMaxWidth().padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(modifier = Modifier.size(40.dp).background(iconBg, RoundedCornerShape(8.dp)), contentAlignment = Alignment.Center) {
                Icon(icon, contentDescription = null, tint = iconTint)
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(id, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                Text(status, color = if (status == "Approved") SafeGreen else MutedText, fontSize = 12.sp)
            }
            Text(time, color = MutedText, fontSize = 12.sp)
        }
    }
}

// ==========================================
// 2. TAB REQUESTS (DRAFTS)
// ==========================================
@Composable
fun StaffRequestsTab() {
    Column(modifier = Modifier.fillMaxSize()) {
        Text("Requests", fontSize = 28.sp, fontWeight = FontWeight.Bold, color = RailBlue, modifier = Modifier.padding(16.dp))
        OutlinedTextField(
            value = "", onValueChange = {},
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
            placeholder = { Text("Search ID or Project...") },
            leadingIcon = { Icon(Icons.Default.Search, null) },
            shape = RoundedCornerShape(8.dp),
            colors = OutlinedTextFieldDefaults.colors(unfocusedContainerColor = Color.White, focusedContainerColor = Color.White)
        )

        // Dummy Tabs
        ScrollableTabRow(
            selectedTabIndex = 1,
            containerColor = Color.Transparent,
            contentColor = RailBlue,
            edgePadding = 16.dp,
            modifier = Modifier.padding(top = 8.dp)
        ) {
            Tab(selected = false, onClick = {}, text = { Text("All") })
            Tab(selected = true, onClick = {}, text = { Text("Drafts", fontWeight = FontWeight.Bold) })
            Tab(selected = false, onClick = {}, text = { Text("In Progress") })
            Tab(selected = false, onClick = {}, text = { Text("Completed") })
        }

        LazyColumn(contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            item { RequestDraftCard("REQ-2023-102", "High-Speed Rail - Phase 2", "Step 3 of 5", 0.6f, "2 hours ago") }
            item { RequestDraftCard("REQ-2023-098", "Jakarta Depot Expansion", "Step 1 of 5", 0.2f, "yesterday") }
            item { RequestDraftCard("REQ-2023-085", "Signal Upgrade Kit", "Step 4 of 5", 0.8f, "Oct 12, 2023") }
        }
    }
}

@Composable
fun RequestDraftCard(id: String, title: String, step: String, progress: Float, time: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, Color.LightGray.copy(alpha = 0.5f))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(id, color = MutedText, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                Box(modifier = Modifier.background(RailLightBlue, RoundedCornerShape(4.dp)).padding(horizontal = 8.dp, vertical = 2.dp)) {
                    Text("Draft", fontSize = 10.sp, color = RailBlue)
                }
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(title, fontSize = 16.sp, fontWeight = FontWeight.Medium)
            Spacer(modifier = Modifier.height(16.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(step, fontSize = 12.sp, fontWeight = FontWeight.Medium)
                Text("${(progress * 100).toInt()}%", fontSize = 12.sp, fontWeight = FontWeight.Medium)
            }
            LinearProgressIndicator(progress = { progress }, modifier = Modifier.fillMaxWidth().padding(top = 4.dp, bottom = 12.dp), color = RailBlue, trackColor = RailLightBlue)
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Restore, null, modifier = Modifier.size(14.dp), tint = MutedText)
                Spacer(modifier = Modifier.width(4.dp))
                Text("Last updated $time", fontSize = 12.sp, color = MutedText)
            }
        }
    }
}

// ==========================================
// 3. TAB INVENTORY
// ==========================================
@Composable
fun StaffInventoryTab() {
    Column(modifier = Modifier.fillMaxSize()) {
        OutlinedTextField(
            value = "", onValueChange = {},
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            placeholder = { Text("Search by name or ID...") },
            leadingIcon = { Icon(Icons.Default.Search, null) },
            shape = RoundedCornerShape(8.dp),
            colors = OutlinedTextFieldDefaults.colors(unfocusedContainerColor = Color.White, focusedContainerColor = Color.White)
        )

        ScrollableTabRow(
            selectedTabIndex = 0, containerColor = Color.Transparent, contentColor = RailBlue, edgePadding = 16.dp
        ) {
            Tab(selected = true, onClick = {}, text = { Text("All", fontWeight = FontWeight.Bold) })
            Tab(selected = false, onClick = {}, text = { Text("Infrastructure") })
            Tab(selected = false, onClick = {}, text = { Text("Spare Parts") })
            Tab(selected = false, onClick = {}, text = { Text("Tools") })
        }

        LazyColumn(contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            item { InventoryItemCard("Heavy Duty Rail Joint", "RJ-7782-A", "A-12-04", "142", "Safe", SafeGreenBg, SafeGreen) }
            item { InventoryItemCard("Signal Relay Type C", "SR-1029-C", "C-05-11", "4", "Critical", CriticalRedBg, CriticalRed) }
            item { InventoryItemCard("Track Fastener Clip", "TC-441-Z", "A-14-22", "850", "Safe", SafeGreenBg, SafeGreen) }
        }
    }
}

@Composable
fun InventoryItemCard(name: String, id: String, loc: String, qty: String, status: String, statusBg: Color, statusColor: Color) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, Color.LightGray.copy(alpha = 0.5f))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.Top) {
                Column {
                    Text(name, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    Text("ID: $id", color = MutedText, fontSize = 12.sp)
                }
                Box(modifier = Modifier.background(statusBg, RoundedCornerShape(4.dp)).padding(horizontal = 8.dp, vertical = 4.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(modifier = Modifier.size(6.dp).background(statusColor, CircleShape))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(status, fontSize = 10.sp, color = statusColor, fontWeight = FontWeight.Bold)
                    }
                }
            }
            Divider(modifier = Modifier.padding(vertical = 12.dp), color = Color.LightGray.copy(alpha = 0.3f))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.Bottom) {
                Column {
                    Text("LOCATION", fontSize = 10.sp, color = MutedText, fontWeight = FontWeight.Bold)
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Storefront, null, modifier = Modifier.size(14.dp), tint = MutedText)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(loc, fontSize = 12.sp, color = MutedText)
                    }
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text("QTY", fontSize = 10.sp, color = MutedText, fontWeight = FontWeight.Bold)
                    Text(qty, fontSize = 20.sp, fontWeight = FontWeight.Bold, color = RailBlue)
                }
            }
        }
    }
}

// ==========================================
// 4. TAB HISTORY
// ==========================================
@Composable
fun StaffHistoryTab() {
    Column(modifier = Modifier.fillMaxSize()) {
        Text("Request History", fontSize = 28.sp, fontWeight = FontWeight.Bold, color = RailBlue, modifier = Modifier.padding(16.dp))
        OutlinedTextField(
            value = "", onValueChange = {},
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
            placeholder = { Text("Search ID or Project...") },
            leadingIcon = { Icon(Icons.Default.Search, null) },
            shape = RoundedCornerShape(8.dp),
            colors = OutlinedTextFieldDefaults.colors(unfocusedContainerColor = Color.White, focusedContainerColor = Color.White)
        )

        ScrollableTabRow(
            selectedTabIndex = 0, containerColor = Color.Transparent, contentColor = RailBlue, edgePadding = 16.dp, modifier = Modifier.padding(top = 8.dp)
        ) {
            Tab(selected = true, onClick = {}, text = { Text("All Requests", fontWeight = FontWeight.Bold) })
            Tab(selected = false, onClick = {}, text = { Text("Pending") })
            Tab(selected = false, onClick = {}, text = { Text("Approved") })
            Tab(selected = false, onClick = {}, text = { Text("Completed") })
        }

        LazyColumn(contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            item { HistoryItemCard("REQ-2023-089", "Track Maintenance - Sector 7G", "12 Items", "COMPLETED", "Oct 24, 14:30", SafeGreen) }
            item { HistoryItemCard("REQ-2023-091", "Signal Repair Kit - Junction B", "3 Items", "APPROVED", "Oct 25, 09:15", SafeGreen) }
            item { HistoryItemCard("REQ-2023-094", "Routine Safety Gear Restock", "45 Items", "PENDING", "Oct 26, 11:00", MutedText) }
        }
    }
}

@Composable
fun HistoryItemCard(id: String, title: String, items: String, status: String, time: String, statusColor: Color) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, Color.LightGray.copy(alpha = 0.5f))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(id, color = RailBlue, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.background(Color.LightGray.copy(alpha = 0.2f), RoundedCornerShape(4.dp)).padding(horizontal = 6.dp, vertical = 2.dp)) {
                    Icon(Icons.Default.Event, null, modifier = Modifier.size(12.dp), tint = MutedText)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(time, fontSize = 10.sp, color = MutedText)
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(title, fontSize = 14.sp, fontWeight = FontWeight.Medium)
            Spacer(modifier = Modifier.height(12.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Inventory2, null, modifier = Modifier.size(14.dp), tint = MutedText)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(items, fontSize = 12.sp, color = MutedText)
                }
                Box(modifier = Modifier.border(1.dp, statusColor.copy(alpha = 0.3f), RoundedCornerShape(4.dp)).padding(horizontal = 8.dp, vertical = 4.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(modifier = Modifier.size(6.dp).background(statusColor, CircleShape))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(status, fontSize = 10.sp, color = statusColor, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}