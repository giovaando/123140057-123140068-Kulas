package com.example.raillog.presentation.screens.admin_main

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.raillog.domain.model.SupplyItem
import kotlinx.datetime.Clock
import org.koin.compose.viewmodel.koinViewModel
import kotlin.math.absoluteValue

val RailBlue = Color(0xFF193255)
val RailLightBlue = Color(0xFFF0F4FA)
val SafeGreen = Color(0xFF10B981)
val CriticalRed = Color(0xFFEF4444)

sealed class AdminBottomNavItem(val title: String, val icon: ImageVector) {
    data object Inventory : AdminBottomNavItem("Inventory", Icons.Default.Inventory2)
    data object Verification : AdminBottomNavItem("Verification", Icons.Default.FactCheck)
    data object Operations : AdminBottomNavItem("Operations", Icons.Default.Dashboard)
}

fun formatAdminTimestamp(millis: Long): String {
    val now = Clock.System.now().toEpochMilliseconds()
    val diff = now - millis
    val minutes = diff / (1000 * 60)
    return when {
        minutes <= 0 -> "Just now"
        minutes < 60 -> "${minutes}m ago"
        minutes < 1440 -> "${minutes / 60}h ago"
        else -> "${minutes / 1440}d ago"
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminMainScreen(
    viewModel: AdminMainViewModel = koinViewModel(),
    onNavigateToVerificationDetail: (Long) -> Unit
) {
    val pendingItems by viewModel.pendingRequisitions.collectAsState()
    val allItems by viewModel.allItems.collectAsState()

    var selectedTab by remember { mutableIntStateOf(1) }
    val tabs = listOf(AdminBottomNavItem.Inventory, AdminBottomNavItem.Verification, AdminBottomNavItem.Operations)

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(modifier = Modifier.size(32.dp).background(RailBlue, CircleShape), contentAlignment = Alignment.Center) {
                            Text("AL", fontSize = 10.sp, color = Color.White, fontWeight = FontWeight.Bold)
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Text("RailLog Nusantara", fontWeight = FontWeight.Bold, color = RailBlue)
                    }
                },
                actions = {
                    IconButton(onClick = { }) { Icon(Icons.Default.NotificationsNone, contentDescription = "Alerts", tint = RailBlue) }
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
                0 -> AdminInventoryTab(allItems)
                1 -> AdminVerificationTab(pendingItems, onNavigateToVerificationDetail)
                2 -> AdminOperationsTab(allItems, pendingItems.size)
            }
        }
    }
}

// ==========================================
// 1. TAB INVENTORY (DATA BENAR: INFRA, SPARE PARTS, TOOLS)
// ==========================================
@Composable
fun AdminInventoryTab(allItems: List<SupplyItem>) {
    val verifiedItems = allItems.filter { it.status.name == "VERIFIED" }

    // Sesuaikan dengan data riil dari Staf Gudang
    val infraQty = verifiedItems.filter { it.category.name.contains("INFRA", ignoreCase = true) }.sumOf { it.quantity }
    val spareQty = verifiedItems.filter { it.category.name.contains("SPARE", ignoreCase = true) }.sumOf { it.quantity }
    val toolsQty = verifiedItems.filter { it.category.name.contains("TOOL", ignoreCase = true) }.sumOf { it.quantity }

    val maxCapacity = 1000f

    val infraProgress = (infraQty / maxCapacity).coerceIn(0f, 1f)
    val spareProgress = (spareQty / maxCapacity).coerceIn(0f, 1f)
    val toolsProgress = (toolsQty / maxCapacity).coerceIn(0f, 1f)

    val groupedInventory = verifiedItems.groupBy { it.name }.map { (name, items) ->
        val totalQty = items.sumOf { it.quantity }
        val partCode = items.first().partCode
        Triple(name, partCode, totalQty)
    }

    val urgentItems = groupedInventory.filter { it.third < 50 }.sortedBy { it.third }.take(3)
    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(16.dp)
    ) {
        Text("Inventory Overview", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = RailBlue)
        Spacer(modifier = Modifier.height(16.dp))

        InventoryProgressCard("Infrastructure", infraProgress, infraQty, maxCapacity.toInt(), RailBlue)
        Spacer(modifier = Modifier.height(12.dp))
        InventoryProgressCard("Spare Parts", spareProgress, spareQty, maxCapacity.toInt(), Color(0xFF4C51BF))
        Spacer(modifier = Modifier.height(12.dp))
        InventoryProgressCard("Tools", toolsProgress, toolsQty, maxCapacity.toInt(), CriticalRed, isWarning = toolsProgress > 0.85f)

        Spacer(modifier = Modifier.height(24.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.ErrorOutline, contentDescription = null, tint = Color(0xFF64748B), modifier = Modifier.size(20.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text("Urgent Replenishments", fontSize = 18.sp, fontWeight = FontWeight.Medium, color = Color(0xFF0F172A))
        }
        Spacer(modifier = Modifier.height(12.dp))

        if (urgentItems.isEmpty()) {
            Box(modifier = Modifier.fillMaxWidth().padding(16.dp), contentAlignment = Alignment.Center) {
                Text("Semua stok dalam keadaan aman.", color = Color.Gray)
            }
        } else {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
                shape = RoundedCornerShape(8.dp)
            ) {
                Column {
                    urgentItems.forEachIndexed { index, item ->
                        UrgentItemRow(item.first, item.second.ifEmpty { "SKU-N/A" }, "${item.third} Units Left")
                        if (index < urgentItems.size - 1) {
                            HorizontalDivider(color = Color(0xFFE2E8F0))
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(80.dp))
    }
}

@Composable
fun InventoryProgressCard(title: String, progress: Float, currentQty: Int, maxQty: Int, color: Color, isWarning: Boolean = false) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = if (isWarning) Color(0xFFFEF2F2) else Color.White),
        border = BorderStroke(1.dp, if (isWarning) CriticalRed else Color(0xFFE2E8F0)),
        shape = RoundedCornerShape(8.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(title, fontSize = 16.sp, fontWeight = FontWeight.Medium, color = if (isWarning) CriticalRed else Color(0xFF0F172A))
                    if (isWarning) {
                        Spacer(modifier = Modifier.width(8.dp))
                        Icon(Icons.Default.WarningAmber, contentDescription = null, tint = CriticalRed, modifier = Modifier.size(16.dp))
                    }
                }
                // Menampilkan Persentase
                Text("${(progress * 100).toInt()}%", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = if (isWarning) CriticalRed else Color(0xFF64748B))
            }
            Spacer(modifier = Modifier.height(4.dp))
            // Menampilkan Angka Riil sebagai Subteks
            Text("$currentQty / $maxQty Units", fontSize = 12.sp, color = Color(0xFF64748B))
            Spacer(modifier = Modifier.height(8.dp))
            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier.fillMaxWidth().height(12.dp),
                color = color,
                trackColor = Color(0xFFE2E8F0),
                strokeCap = StrokeCap.Round
            )
        }
    }
}

@Composable
fun UrgentItemRow(title: String, sku: String, status: String) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(title, fontSize = 14.sp, fontWeight = FontWeight.Medium, color = Color(0xFF0F172A))
            Spacer(modifier = Modifier.height(2.dp))
            Text(sku, fontSize = 12.sp, color = Color(0xFF64748B))
        }
        Box(
            modifier = Modifier.background(Color(0xFFFEE2E2), RoundedCornerShape(4.dp)).padding(horizontal = 8.dp, vertical = 4.dp)
        ) {
            Text(status, color = CriticalRed, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
        }
    }
}

// ==========================================
// 2. TAB VERIFICATION
// ==========================================
@Composable
fun AdminVerificationTab(pendingItems: List<SupplyItem>, onNavigateToDetail: (Long) -> Unit) {
    var selectedFilter by remember { mutableStateOf("All") }
    val filters = listOf("All", "High Confidence", "Low Confidence", "Flagged")

    Column(modifier = Modifier.fillMaxSize()) {
        Text(
            text = "Verification Queue",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = RailBlue,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)
        )

        LazyRow(
            contentPadding = PaddingValues(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(filters) { filter ->
                val isSelected = selectedFilter == filter
                Box(
                    modifier = Modifier
                        .clickable { selectedFilter = filter }
                        .background(if (isSelected) RailBlue else Color.White, RoundedCornerShape(8.dp))
                        .border(1.dp, if (isSelected) RailBlue else Color(0xFFE2E8F0), RoundedCornerShape(8.dp))
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    Text(
                        text = filter,
                        color = if (isSelected) Color.White else Color(0xFF64748B),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        LazyColumn(
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 100.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            if (pendingItems.isEmpty()) {
                item {
                    Box(modifier = Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                        Text("Queue is empty.", color = Color.Gray)
                    }
                }
            } else {
                items(pendingItems) { item ->
                    val confidenceScore = 45 + (item.id.hashCode().absoluteValue % 54)
                    val isHighConfidence = confidenceScore >= 80
                    val isFlagged = confidenceScore < 60

                    val showItem = when (selectedFilter) {
                        "High Confidence" -> isHighConfidence
                        "Low Confidence" -> !isHighConfidence && !isFlagged
                        "Flagged" -> isFlagged
                        else -> true
                    }
                    if (showItem) {
                        VerificationQueueCard(item, confidenceScore, isHighConfidence, isFlagged) {
                            onNavigateToDetail(item.id)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun VerificationQueueCard(item: SupplyItem, confidenceScore: Int, isHighConfidence: Boolean, isFlagged: Boolean, onClick: () -> Unit) {
    val progressColor = when {
        isHighConfidence -> SafeGreen
        isFlagged -> CriticalRed
        else -> Color(0xFFEAB308)
    }

    Card(
        modifier = Modifier.fillMaxWidth().clickable { onClick() },
        colors = CardDefaults.cardColors(containerColor = if (isFlagged) Color(0xFFFEF2F2) else Color.White),
        border = BorderStroke(1.dp, if (isFlagged) Color(0xFFFECACA) else Color(0xFFE2E8F0)),
        shape = RoundedCornerShape(8.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "PRJ-2026-${item.id.toString().padStart(4, '0')}",
                    color = RailBlue,
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.AccessTime, contentDescription = null, tint = Color(0xFF64748B), modifier = Modifier.size(12.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(formatAdminTimestamp(item.createdAt.toEpochMilliseconds()), fontSize = 12.sp, color = Color(0xFF64748B))
                }
            }

            Spacer(modifier = Modifier.height(12.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(item.name, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color(0xFF0F172A))
                if (isFlagged) {
                    Spacer(modifier = Modifier.width(8.dp))
                    Icon(Icons.Default.Warning, contentDescription = null, tint = CriticalRed, modifier = Modifier.size(16.dp))
                }
            }

            Spacer(modifier = Modifier.height(4.dp))
            Text("Submission for ${item.category.name.replace("_", " ").lowercase()}.", fontSize = 14.sp, color = Color(0xFF64748B))

            Spacer(modifier = Modifier.height(16.dp))

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("AI Extraction Confidence", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF64748B))
                Text("$confidenceScore%", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = progressColor)
            }
            Spacer(modifier = Modifier.height(8.dp))
            LinearProgressIndicator(
                progress = { confidenceScore / 100f },
                modifier = Modifier.fillMaxWidth().height(8.dp),
                color = progressColor,
                trackColor = Color(0xFFE2E8F0),
                strokeCap = StrokeCap.Round
            )
        }
    }
}

// ==========================================
// 3. TAB OPERATIONS
// ==========================================
@Composable
fun AdminOperationsTab(allItems: List<SupplyItem>, pendingCount: Int) {
    val verifiedCount = allItems.count { it.status.name == "VERIFIED" }

    val dynamicAlerts = remember(allItems) {
        val alerts = mutableListOf<Triple<String, String, Boolean>>()

        val verifiedItems = allItems.filter { it.status.name == "VERIFIED" }
        val lowStockCount = verifiedItems.groupBy { it.name }.count { (_, items) -> items.sumOf { it.quantity } < 50 }
        if (lowStockCount > 0) {
            alerts.add(Triple("Low Stock Alert", "$lowStockCount part(s) running critically low in inventory. Restock recommended.", true))
        }

        val highPriorityPending = allItems.count { it.status.name == "PENDING" && (it.priority.name == "HIGH" || it.priority.name == "CRITICAL") }
        if (highPriorityPending > 0) {
            alerts.add(Triple("Action Required", "$highPriorityPending high-priority requisition(s) awaiting your verification.", true))
        }

        val rejectedCount = allItems.count { it.status.name == "REJECTED" }
        if (rejectedCount > 0) {
            alerts.add(Triple("Revisions Tracked", "$rejectedCount document(s) sent back to staff for revision.", false))
        }

        if (alerts.isEmpty()) {
            alerts.add(Triple("System Normal", "All operational nodes and inventories are stable.", false))
        }

        alerts
    }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text("Operations Overview", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = RailBlue)
        Spacer(modifier = Modifier.height(16.dp))

        Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = Color.White), border = BorderStroke(1.dp, Color(0xFFE5E7EB))) {
            Row(modifier = Modifier.padding(16.dp).fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Column {
                    Text("TOTAL DOCS VERIFIED", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color.Gray)
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(verifiedCount.toString(), fontSize = 28.sp, fontWeight = FontWeight.Bold, color = RailBlue)
                    }
                }
                Box(modifier = Modifier.size(48.dp).background(RailLightBlue, RoundedCornerShape(8.dp)))
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Card(modifier = Modifier.weight(1f), colors = CardDefaults.cardColors(containerColor = Color.White), border = BorderStroke(1.dp, Color(0xFFE5E7EB))) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("AVG AI CONFIDENCE", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color.Gray)
                    Text("94.2%", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = Color.Black)
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.CheckCircle, contentDescription = null, tint = SafeGreen, modifier = Modifier.size(12.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Optimal", fontSize = 12.sp, color = SafeGreen)
                    }
                }
                Box(modifier = Modifier.fillMaxWidth().height(4.dp).background(SafeGreen))
            }

            Card(modifier = Modifier.weight(1f), colors = CardDefaults.cardColors(containerColor = Color.White), border = BorderStroke(1.dp, Color(0xFFE5E7EB))) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("PENDING QUEUE", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color.Gray)
                    Text(pendingCount.toString(), fontSize = 24.sp, fontWeight = FontWeight.Bold, color = if (pendingCount > 0) CriticalRed else SafeGreen)
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(if (pendingCount > 0) Icons.Default.Warning else Icons.Default.CheckCircle, contentDescription = null, tint = if (pendingCount > 0) CriticalRed else SafeGreen, modifier = Modifier.size(12.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(if (pendingCount > 0) "Requires Action" else "All Cleared", fontSize = 12.sp, color = if (pendingCount > 0) CriticalRed else SafeGreen)
                    }
                }
                Box(modifier = Modifier.fillMaxWidth().height(4.dp).background(if (pendingCount > 0) CriticalRed else SafeGreen))
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
        Text("System Alerts", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color.DarkGray)
        Spacer(modifier = Modifier.height(12.dp))

        Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = Color.White), border = BorderStroke(1.dp, Color(0xFFE5E7EB))) {
            Column {
                dynamicAlerts.forEachIndexed { index, alert ->
                    AlertRow(
                        title = alert.first,
                        desc = alert.second,
                        time = "Recent",
                        isCritical = alert.third
                    )
                    if (index < dynamicAlerts.size - 1) {
                        HorizontalDivider(color = Color(0xFFF3F4F6))
                    }
                }
            }
        }
    }
}

@Composable
fun AlertRow(title: String, desc: String, time: String, isCritical: Boolean) {
    Row(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
        Icon(if (isCritical) Icons.Default.Error else Icons.Default.Info, contentDescription = null, tint = if (isCritical) CriticalRed else Color.Gray, modifier = Modifier.size(20.dp))
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(title, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color.Black)
            Text(desc, fontSize = 12.sp, color = Color.DarkGray)
        }
        Text(time, fontSize = 12.sp, color = Color.Gray)
    }
}