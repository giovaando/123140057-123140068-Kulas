package com.example.raillog.presentation.screens.staff_main

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.SecondaryIndicator
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.raillog.domain.model.DraftItem
import com.example.raillog.domain.model.SupplyItem
import org.koin.compose.viewmodel.koinViewModel

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

// FUNGSI PENERJEMAH WAKTU
fun formatTimestamp(millis: Long): String {
    if (millis <= 0L) return "Just now"

    val totalSeconds = millis / 1000
    var days = totalSeconds / 86400

    val timeOfDay = (totalSeconds + 7 * 3600) % 86400 // WIB UTC+7
    val hours = timeOfDay / 3600
    val minutes = (timeOfDay % 3600) / 60

    var y = 1970
    while (true) {
        val daysInYear = if (y % 4 == 0 && (y % 100 != 0 || y % 400 == 0)) 366 else 365
        if (days < daysInYear) break
        days -= daysInYear
        y++
    }
    val daysInMonth = intArrayOf(31, if (y % 4 == 0 && (y % 100 != 0 || y % 400 == 0)) 29 else 28, 31, 30, 31, 30, 31, 31, 30, 31, 30, 31)
    val months = arrayOf("Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec")

    var m = 0
    while (days >= daysInMonth[m]) {
        days -= daysInMonth[m]
        m++
    }
    val day = days + 1
    return "${months[m]} ${day.toString().padStart(2, '0')}, ${hours.toString().padStart(2, '0')}:${minutes.toString().padStart(2, '0')}"
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StaffMainScreen(
    viewModel: StaffMainViewModel = koinViewModel(),
    onNavigateToNewRequisition: () -> Unit,
    onNavigateToResumeDraft: (String) -> Unit = {}
) {
    val userRole by viewModel.activeUserRole.collectAsState()
    val allItems by viewModel.allSupplyItems.collectAsState()
    val allDrafts by viewModel.allDrafts.collectAsState()

    var selectedTab by remember { mutableStateOf(0) }
    val tabs = listOf(BottomNavItem.Home, BottomNavItem.Requests, BottomNavItem.Inventory, BottomNavItem.History)

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(modifier = Modifier.size(32.dp).background(Color.LightGray, CircleShape), contentAlignment = Alignment.Center) {
                            Text("GL", fontSize = 10.sp, color = Color.White, fontWeight = FontWeight.Bold)
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Text("RailLog Nusantara", fontWeight = FontWeight.Bold, color = RailBlue)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
            )
        },
        floatingActionButton = {
            if (selectedTab == 1) {
                FloatingActionButton(onClick = onNavigateToNewRequisition, containerColor = RailBlue, contentColor = Color.White) {
                    Icon(Icons.Default.Add, contentDescription = "New Request")
                }
            }
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
                0 -> StaffHomeTab(
                    userRole = userRole,
                    allItems = allItems,
                    onStartNew = onNavigateToNewRequisition,
                    onViewAllActivity = { selectedTab = 3 } // MENGUBAH TAB KE HISTORY (INDEKS 3)
                )
                1 -> StaffRequestsTab(allItems, allDrafts, onNavigateToResumeDraft)
                2 -> StaffInventoryTab(allItems)
                3 -> StaffHistoryTab(allItems)
            }
        }
    }
}

@Composable
fun StaffHomeTab(
    userRole: String,
    allItems: List<SupplyItem>,
    onStartNew: () -> Unit,
    onViewAllActivity: () -> Unit // PARAMETER BARU UNTUK KLIK VIEW ALL
) {
    val pendingCount = allItems.count { it.status.name == "PENDING" }
    val criticalCount = allItems.count { it.priority.name == "CRITICAL" || it.priority.name == "HIGH" }

    LazyColumn(modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        item {
            Spacer(modifier = Modifier.height(8.dp))
            Text("Good Morning,\n$userRole", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = RailBlue, lineHeight = 30.sp)
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(top = 4.dp)) {
                Box(modifier = Modifier.size(8.dp).background(if(pendingCount > 0) CriticalRed else SafeGreen, CircleShape))
                Spacer(modifier = Modifier.width(8.dp))
                Text(if(pendingCount > 0) "$pendingCount Pending Requisitions" else "All systems normal", color = MutedText, fontSize = 14.sp)
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
                MetricCard(Modifier.weight(1f), Icons.Default.Assignment, "Total Items", allItems.size.toString(), MutedText)
                MetricCard(Modifier.weight(1f), Icons.Default.WarningAmber, "High Priority", criticalCount.toString(), CriticalRed, CriticalRedBg)
            }
        }

        item {
            Spacer(modifier = Modifier.height(8.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text("Recent Activity", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = RailBlue)
                // TOMBOL VIEW ALL YANG BISA DIKLIK
                Text(
                    text = "View All",
                    fontSize = 12.sp,
                    color = RailBlue,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier
                        .clickable { onViewAllActivity() }
                        .padding(vertical = 4.dp, horizontal = 8.dp) // Area klik yang lebih nyaman
                )
            }
        }

        if (allItems.isEmpty()) {
            item {
                Box(modifier = Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                    Text("No recent activity yet.", color = Color.Gray, fontSize = 14.sp)
                }
            }
        } else {
            // MENGAMBIL MAKSIMAL 3 DATA TERBARU SAJA
            items(allItems.reversed().take(3)) { item ->
                HistoryCard(item)
            }
        }
        item { Spacer(modifier = Modifier.height(24.dp)) }
    }
}

@Composable
fun StaffRequestsTab(allItems: List<SupplyItem>, allDrafts: List<DraftItem>, onResumeDraft: (String) -> Unit) {
    var selectedSubTab by remember { mutableIntStateOf(0) }
    val subTabs = listOf("All", "Drafts", "In Progress", "Completed")

    val filteredItems = when (selectedSubTab) {
        0 -> allItems
        2 -> allItems.filter { it.status.name == "PENDING" }
        3 -> allItems.filter { it.status.name == "VERIFIED" || it.status.name == "REJECTED" }
        else -> allItems
    }

    Column(modifier = Modifier.fillMaxSize()) {
        Text("Requests", fontSize = 28.sp, fontWeight = FontWeight.Bold, color = RailBlue, modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp))

        SearchBarUI()

        ScrollableTabRow(
            selectedTabIndex = selectedSubTab, containerColor = Color.Transparent, edgePadding = 16.dp,
            indicator = { tabPositions -> SecondaryIndicator(Modifier.tabIndicatorOffset(tabPositions[selectedSubTab]), color = RailBlue) },
            divider = { HorizontalDivider(color = Color.LightGray.copy(alpha = 0.5f)) }
        ) {
            subTabs.forEachIndexed { index, title ->
                Tab(
                    selected = selectedSubTab == index, onClick = { selectedSubTab = index },
                    text = { Text(title, fontWeight = if(selectedSubTab == index) FontWeight.Bold else FontWeight.Normal, color = if(selectedSubTab == index) RailBlue else Color.Gray) }
                )
            }
        }

        LazyColumn(contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            if (selectedSubTab == 1) {
                if (allDrafts.isEmpty()) {
                    item { Text("No Drafts Saved. Start a new requisition to auto-save.", color = Color.Gray, modifier = Modifier.padding(top = 16.dp)) }
                }
                items(allDrafts) { draft -> DraftCard(draft) { onResumeDraft(draft.draftId) } }
            } else {
                if (filteredItems.isEmpty()) {
                    item { Text("No requests found.", color = Color.Gray, modifier = Modifier.padding(top = 16.dp)) }
                }
                items(filteredItems) { item -> RequestProgressCard(item) }
            }
            item { Spacer(modifier = Modifier.height(72.dp)) }
        }
    }
}

@Composable
fun StaffInventoryTab(allItems: List<SupplyItem>) {
    var selectedCategory by remember { mutableStateOf("All") }
    val categories = listOf("All", "Infrastructure", "Spare Parts", "Tools")

    val approvedItems = allItems.filter { it.status.name == "VERIFIED" }
    val filteredItems = if (selectedCategory == "All") approvedItems
    else approvedItems.filter { it.category.name.replace("_", " ").equals(selectedCategory, ignoreCase = true) }

    Column(modifier = Modifier.fillMaxSize()) {
        SearchBarUI()
        LazyRow(contentPadding = PaddingValues(horizontal = 16.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            items(categories) { category ->
                val isSelected = selectedCategory == category
                Box(modifier = Modifier.clickable { selectedCategory = category }.background(if (isSelected) RailBlue else Color.White, RoundedCornerShape(8.dp)).border(1.dp, if (isSelected) RailBlue else Color.LightGray, RoundedCornerShape(8.dp)).padding(horizontal = 16.dp, vertical = 8.dp)) {
                    Text(category, color = if (isSelected) Color.White else RailBlue, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        LazyColumn(contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            if (filteredItems.isEmpty()) {
                item {
                    Box(modifier = Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                        Text("No items found in inventory.\nPending requests need approval first.", textAlign = androidx.compose.ui.text.style.TextAlign.Center, color = Color.Gray, fontSize = 14.sp)
                    }
                }
            } else {
                items(filteredItems) { item -> InventoryItemCard(item) }
            }
            item { Spacer(modifier = Modifier.height(72.dp)) }
        }
    }
}

@Composable
fun StaffHistoryTab(allItems: List<SupplyItem>) {
    var selectedFilter by remember { mutableStateOf("All Requests") }
    val filters = listOf("All Requests", "Pending", "Verified", "Rejected")

    val filteredItems = when (selectedFilter) {
        "Pending" -> allItems.filter { it.status.name == "PENDING" }
        "Verified" -> allItems.filter { it.status.name == "VERIFIED" }
        "Rejected" -> allItems.filter { it.status.name == "REJECTED" }
        else -> allItems
    }

    Column(modifier = Modifier.fillMaxSize()) {
        Text("Request History", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = RailBlue, modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp))
        SearchBarUI(placeholder = "Search ID or Project...")
        LazyRow(contentPadding = PaddingValues(horizontal = 16.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            items(filters) { filter ->
                val isSelected = selectedFilter == filter
                Box(modifier = Modifier.clickable { selectedFilter = filter }.border(1.dp, if(isSelected) RailBlue else Color.LightGray, RoundedCornerShape(4.dp)).background(if(isSelected) RailBlue else Color.White, RoundedCornerShape(4.dp)).padding(horizontal = 12.dp, vertical = 8.dp)) {
                    Text(filter, color = if(isSelected) Color.White else Color.DarkGray, fontSize = 12.sp, fontWeight = FontWeight.Medium)
                }
            }
        }
        LazyColumn(contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            if (filteredItems.isEmpty()) {
                item { Text("No requests found.", color = Color.Gray, modifier = Modifier.padding(top = 16.dp)) }
            }
            items(filteredItems) { item ->
                HistoryCard(item)
            }
            item { Spacer(modifier = Modifier.height(72.dp)) }
        }
    }
}

@Composable
fun HistoryCard(item: SupplyItem) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, Color(0xFFE5E7EB))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = item.partCode.ifEmpty { "REQ-2023-089" },
                    color = RailBlue,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.background(Color(0xFFF3F4F6), RoundedCornerShape(4.dp)).padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Icon(Icons.Default.CalendarToday, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(12.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(formatTimestamp(item.createdAt.toEpochMilliseconds()), fontSize = 12.sp, color = Color.DarkGray)
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = item.name,
                fontSize = 14.sp,
                color = Color.DarkGray
            )

            Spacer(modifier = Modifier.height(16.dp))

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Inventory2, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("${item.quantity} Items", fontSize = 14.sp, color = Color.Gray)
                }

                val (bgStatusColor, textStatusColor, dotColor) = when (item.status.name) {
                    "VERIFIED" -> Triple(Color(0xFFECFDF5), SafeGreen, SafeGreen)
                    "PENDING" -> Triple(Color(0xFFFFFBEB), Color(0xFFD97706), Color(0xFFF59E0B))
                    "REJECTED" -> Triple(Color(0xFFFEF2F2), CriticalRed, CriticalRed)
                    else -> Triple(Color(0xFFF3F4F6), Color.Gray, Color.DarkGray)
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.background(bgStatusColor, RoundedCornerShape(4.dp)).padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Box(modifier = Modifier.size(6.dp).background(dotColor, CircleShape))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = item.status.name,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = textStatusColor
                    )
                }
            }
        }
    }
}

@Composable
fun DraftCard(draft: DraftItem, onClick: () -> Unit) {
    Card(modifier = Modifier.fillMaxWidth().clickable { onClick() }, colors = CardDefaults.cardColors(containerColor = Color.White), border = BorderStroke(1.dp, Color(0xFFD1D5DB))) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text(draft.projectTitle.ifEmpty { "Untitled Draft" }, color = RailBlue, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                Box(modifier = Modifier.background(Color(0xFFE5E7EB), RoundedCornerShape(4.dp)).padding(horizontal = 6.dp, vertical = 2.dp)) {
                    Text("Draft", fontSize = 10.sp, color = Color.DarkGray)
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text("Saved at Step ${draft.currentStep} of 5", fontSize = 12.sp, color = MutedText)
            Spacer(modifier = Modifier.height(12.dp))
            LinearProgressIndicator(progress = { draft.currentStep / 5f }, modifier = Modifier.fillMaxWidth().height(4.dp), color = Color.Gray, trackColor = Color.LightGray.copy(alpha = 0.5f))
        }
    }
}

@Composable
fun InventoryItemCard(item: SupplyItem) {
    Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = Color.White), border = BorderStroke(1.dp, Color.LightGray.copy(alpha = 0.5f))) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(item.name, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    Text("ID: ${item.partCode}", color = MutedText, fontSize = 12.sp)
                }
                Box(modifier = Modifier.background(SafeGreenBg, RoundedCornerShape(4.dp)).padding(horizontal = 8.dp, vertical = 4.dp)) {
                    Text("Qty: ${item.quantity}", fontSize = 12.sp, color = SafeGreen, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
fun RequestProgressCard(item: SupplyItem) {
    Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = Color.White), border = BorderStroke(1.dp, Color.LightGray)) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(item.partCode.ifEmpty { "REQ-N/A" }, color = RailBlue, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                Box(modifier = Modifier.background(Color.LightGray.copy(0.2f), RoundedCornerShape(4.dp)).padding(horizontal = 6.dp, vertical = 2.dp)) {
                    Text(item.status.name, fontSize = 10.sp, color = Color.DarkGray)
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(item.name, fontSize = 16.sp, fontWeight = FontWeight.Medium)
        }
    }
}

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
fun SearchBarUI(placeholder: String = "Search by name or ID...") {
    OutlinedTextField(
        value = "", onValueChange = {}, modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
        placeholder = { Text(placeholder, color = Color.Gray) }, leadingIcon = { Icon(Icons.Default.Search, null, tint = Color.Gray) },
        shape = RoundedCornerShape(8.dp), colors = OutlinedTextFieldDefaults.colors(unfocusedContainerColor = Color.White)
    )
}