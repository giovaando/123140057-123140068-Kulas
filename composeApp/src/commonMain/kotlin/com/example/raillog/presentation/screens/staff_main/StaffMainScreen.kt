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
import androidx.compose.material.icons.automirrored.filled.Assignment
import androidx.compose.material.icons.automirrored.filled.Logout
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.raillog.domain.model.DraftItem
import com.example.raillog.domain.model.SupplyItem
import com.example.raillog.presentation.components.ActiveTaskCard
import com.example.raillog.presentation.components.RailLogMetricCard
import com.example.raillog.presentation.components.RailLogSearchField
import com.example.raillog.presentation.components.RailLogSectionHeader
import com.example.raillog.presentation.components.RailLogStatusChip
import com.example.raillog.presentation.theme.RailLogColors
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import org.koin.compose.viewmodel.koinViewModel

sealed class BottomNavItem(val title: String, val icon: ImageVector) {
    data object Home : BottomNavItem("Beranda", Icons.Default.Home)
    data object Requests : BottomNavItem("Pengajuan", Icons.AutoMirrored.Filled.Assignment)
    data object Inventory : BottomNavItem("Gudang", Icons.Default.Inventory)
    data object History : BottomNavItem("Audit", Icons.Default.History)
}

/**
 * Mendapatkan sapaan dinamis berdasarkan jam sistem saat ini (KMP Safe)
 */
fun getGreeting(): String {
    val now = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())
    return when (now.hour) {
        in 5..10 -> "Selamat Pagi"
        in 11..14 -> "Selamat Siang"
        in 15..18 -> "Selamat Sore"
        else -> "Selamat Malam"
    }
}

fun formatTimestamp(millis: Long): String {
    if (millis <= 0L) return "Baru saja"
    return try {
        val instant = Instant.fromEpochMilliseconds(millis)
        val dt = instant.toLocalDateTime(TimeZone.currentSystemDefault())
        "${dt.dayOfMonth.toString().padStart(2, '0')}/${dt.monthNumber.toString().padStart(2, '0')}/${dt.year}"
    } catch (e: Exception) {
        "Audit ID: $millis"
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StaffMainScreen(
    viewModel: StaffMainViewModel = koinViewModel(),
    onNavigateToNewRequisition: () -> Unit,
    onNavigateToResumeDraft: (String) -> Unit = {},
    onLogout: () -> Unit
) {
    val userRole by viewModel.activeUserRole.collectAsState()
    val allItems by viewModel.allSupplyItems.collectAsState()
    val allDrafts by viewModel.allDrafts.collectAsState()

    var selectedTab by remember { mutableIntStateOf(0) }
    var showAccountMenu by remember { mutableStateOf(false) }

    val tabs = listOf(BottomNavItem.Home, BottomNavItem.Requests, BottomNavItem.Inventory, BottomNavItem.History)

    Scaffold(
        containerColor = RailLogColors.SurfaceSlate,
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier.size(32.dp).background(RailLogColors.PrimaryNavy, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("GL", fontSize = 12.sp, color = Color.White, fontWeight = FontWeight.ExtraBold)
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Text("RailLog Nusantara", fontWeight = FontWeight.ExtraBold, color = RailLogColors.PrimaryNavy)
                    }
                },
                actions = {
                    Box {
                        IconButton(onClick = { showAccountMenu = true }) {
                            Icon(Icons.Default.AccountCircle, "Akun", tint = RailLogColors.PrimaryNavy, modifier = Modifier.size(32.dp))
                        }
                        // Dropdown dengan Kontras Tinggi (Background Putih Solid)
                        DropdownMenu(
                            expanded = showAccountMenu, 
                            onDismissRequest = { showAccountMenu = false },
                            modifier = Modifier.background(Color.White).border(2.dp, Color.Black, RoundedCornerShape(8.dp))
                        ) {
                            val savedName by viewModel.userPreferences.staffName.collectAsState(initial = "")
                            val savedId by viewModel.userPreferences.staffId.collectAsState(initial = "")
                            val savedPhone by viewModel.userPreferences.staffPhone.collectAsState(initial = "")
                            
                            DropdownMenuItem(
                                text = { 
                                    Column(modifier = Modifier.padding(vertical = 4.dp)) {
                                        Text(savedName.ifEmpty { "Staff Logistik" }, fontWeight = FontWeight.ExtraBold, color = Color.Black, fontSize = 16.sp)
                                        if (savedId.isNotEmpty()) Text("NIP: $savedId", fontSize = 13.sp, color = Color.Black, fontWeight = FontWeight.Bold)
                                        if (savedPhone.isNotEmpty()) Text("WA: $savedPhone", fontSize = 13.sp, color = Color.Black, fontWeight = FontWeight.Bold)
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text("PERAN: $userRole", fontSize = 11.sp, color = RailLogColors.PrimaryNavy, fontWeight = FontWeight.ExtraBold)
                                    }
                                },
                                onClick = { showAccountMenu = false }
                            )
                            HorizontalDivider(color = Color.Black, thickness = 1.dp)
                            DropdownMenuItem(
                                text = { Text("LOGOUT SISTEM", color = RailLogColors.ErrorRed, fontWeight = FontWeight.ExtraBold) },
                                onClick = { showAccountMenu = false; onLogout() },
                                leadingIcon = { Icon(Icons.AutoMirrored.Filled.Logout, null, tint = RailLogColors.ErrorRed) }
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
            )
        },
        floatingActionButton = {
            if (selectedTab == 1) {
                FloatingActionButton(
                    onClick = onNavigateToNewRequisition, 
                    containerColor = RailLogColors.PrimaryNavy, 
                    contentColor = Color.White
                ) {
                    Icon(Icons.Default.Add, "Tambah", tint = Color.White)
                }
            }
        },
        bottomBar = {
            NavigationBar(containerColor = Color.White, tonalElevation = 8.dp) {
                tabs.forEachIndexed { index, item ->
                    NavigationBarItem(
                        icon = { Icon(item.icon, contentDescription = item.title) },
                        label = { Text(item.title, fontSize = 11.sp, fontWeight = FontWeight.ExtraBold) },
                        selected = selectedTab == index,
                        onClick = { selectedTab = index },
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
                0 -> StaffHomeTab(userRole, allItems, onNavigateToNewRequisition, { selectedTab = 3 })
                1 -> StaffRequestsTab(viewModel, allDrafts, onNavigateToResumeDraft)
                2 -> StaffInventoryTab(viewModel)
                3 -> StaffHistoryTab(viewModel)
            }
        }
    }
}

@Composable
fun StaffHomeTab(userRole: String, allItems: List<SupplyItem>, onStart: () -> Unit, onViewAll: () -> Unit) {
    val pending = allItems.count { it.status.name == "PENDING" }
    val critical = allItems.count { it.priority.name == "CRITICAL" || it.priority.name == "HIGH" }

    LazyColumn(modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        item {
            Spacer(modifier = Modifier.height(16.dp))
            // SAPAAN DINAMIS JAM SISTEM
            Column {

                Text(
                    getGreeting(),
                    fontSize = 14.sp,
                    color = Color.Gray
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    "Warehouse Operations Center",
                    fontSize = 26.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = RailLogColors.PrimaryNavy
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    userRole,
                    fontSize = 14.sp,
                    color = Color.Gray
                )
            }
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(top = 4.dp)) {
                Box(modifier = Modifier.size(8.dp).background(if (pending > 0) RailLogColors.WarningAmber else RailLogColors.SuccessEmerald, CircleShape))
                Spacer(modifier = Modifier.width(8.dp))
                Text(if (pending > 0) "$pending Permintaan Audit Tertunda" else "Status Operasional Aman", color = Color.Black, fontSize = 14.sp, fontWeight = FontWeight.ExtraBold)
            }
        }

        item {
            ActiveTaskCard(
                pendingCount = pending,
                onClick = onStart
            )
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {

                RailLogMetricCard(
                    modifier = Modifier.weight(1f),
                    title = "Inventory",
                    value = allItems.size.toString(),
                    icon = Icons.Default.Inventory2,
                    iconColor = RailLogColors.PrimaryNavy
                )

                RailLogMetricCard(
                    modifier = Modifier.weight(1f),
                    title = "Critical",
                    value = critical.toString(),
                    icon = Icons.Default.Warning,
                    iconColor = RailLogColors.ErrorRed
                )

                RailLogMetricCard(
                    modifier = Modifier.weight(1f),
                    title = "Pending",
                    value = pending.toString(),
                    icon = Icons.Default.Schedule,
                    iconColor = RailLogColors.WarningAmber
                )
            }
        }

        item {

            RailLogSectionHeader(
                title = "Quick Actions",
                subtitle = "Frequently used operations"
            )
        }

        item {

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {

                OutlinedButton(
                    modifier = Modifier.weight(1f),
                    onClick = onStart
                ) {
                    Icon(
                        Icons.Default.Add,
                        null
                    )

                    Spacer(
                        Modifier.width(8.dp)
                    )

                    Text("New Request")
                }

                OutlinedButton(
                    modifier = Modifier.weight(1f),
                    onClick = onViewAll
                ) {
                    Icon(
                        Icons.Default.History,
                        null
                    )

                    Spacer(
                        Modifier.width(8.dp)
                    )

                    Text("History")
                }
            }
        }

        item {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                RailLogSectionHeader(
                    title = "Recent Activity",
                    subtitle = "Aktivitas logistik terbaru"
                )
                TextButton(onClick = onViewAll) { Text("LIHAT SEMUA", color = RailLogColors.PrimaryNavy, fontWeight = FontWeight.ExtraBold) }
            }
        }

        if (allItems.isEmpty()) {
            item { EmptyStateBox("Belum ada data audit logistik.") }
        } else {
            items(allItems.reversed().take(3)) { item -> HistoryCard(item) }
        }
        item { Spacer(modifier = Modifier.height(24.dp)) }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StaffRequestsTab(viewModel: StaffMainViewModel, allDrafts: List<DraftItem>, onResume: (String) -> Unit) {
    var subTab by remember { mutableIntStateOf(0) }
    val labels = listOf("Semua", "Draf", "Audit", "Selesai")
    val query by viewModel.searchQuery.collectAsState()
    val items by viewModel.filteredRequestItems.collectAsState()

    val display = when (subTab) {
        2 -> items.filter { it.status.name == "PENDING" }
        3 -> items.filter { it.status.name == "VERIFIED" || it.status.name == "REJECTED" }
        else -> items
    }

    Column(modifier = Modifier.fillMaxSize()) {
        Text("Daftar Pengajuan", fontSize = 24.sp, fontWeight = FontWeight.ExtraBold, color = RailLogColors.PrimaryNavy, modifier = Modifier.padding(16.dp))
        RailLogSearchField(
            value = query,
            onValueChange = {
                viewModel.updateSearchQuery(it)
            },
            placeholder = "Cari berdasarkan nama atau kode..."
        )

        ScrollableTabRow(
            selectedTabIndex = subTab, containerColor = Color.Transparent, edgePadding = 16.dp,
            indicator = { tabPositions ->
                SecondaryIndicator(Modifier.tabIndicatorOffset(tabPositions[subTab]), color = RailLogColors.PrimaryNavy)
            },
            divider = { HorizontalDivider(color = Color.Black.copy(alpha = 0.1f)) }
        ) {
            labels.forEachIndexed { i, title ->
                Tab(selected = subTab == i, onClick = { subTab = i }, text = { Text(title, fontWeight = if (subTab == i) FontWeight.Bold else FontWeight.Normal, color = if(subTab == i) RailLogColors.PrimaryNavy else Color.Black) })
            }
        }

        LazyColumn(contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            if (subTab == 1 || subTab == 0) {
                items(allDrafts) { DraftCard(it) { onResume(it.draftId) } }
            }
            if (subTab != 1) {
                items(display) { item -> HistoryCard(item) }
            }
            if (display.isEmpty() && (subTab != 1 || allDrafts.isEmpty())) {
                item { EmptyStateBox("Data tidak ditemukan.") }
            }
            item { Spacer(modifier = Modifier.height(72.dp)) }
        }
    }
}

@Composable
fun StaffInventoryTab(viewModel: StaffMainViewModel) {
    val cats = listOf("All", "Infrastructure", "Bogie", "Propulsion", "Braking", "Tools")
    val query by viewModel.searchQuery.collectAsState()
    val selectedCat by viewModel.inventoryCategory.collectAsState()
    val items by viewModel.filteredInventoryItems.collectAsState()

    Column(modifier = Modifier.fillMaxSize()) {
        Text(
            text = "Inventory Center",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = RailLogColors.PrimaryNavy,
            modifier = Modifier.padding(
                start = 16.dp,
                top = 16.dp,
                end = 16.dp
            )
        )

        Text(
            text = "Warehouse stock monitoring",
            fontSize = 13.sp,
            color = Color.Gray,
            modifier = Modifier.padding(
                start = 16.dp,
                end = 16.dp,
                bottom = 16.dp
            )
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {

            RailLogMetricCard(
                modifier = Modifier.weight(1f),
                title = "Items",
                value = items.size.toString(),
                icon = Icons.Default.Inventory2,
                iconColor = RailLogColors.PrimaryNavy
            )

            RailLogMetricCard(
                modifier = Modifier.weight(1f),
                title = "Categories",
                value = cats.size.toString(),
                icon = Icons.Default.Category,
                iconColor = RailLogColors.SuccessEmerald
            )

            RailLogMetricCard(
                modifier = Modifier.weight(1f),
                title = "Available",
                value = items.count {
                    it.quantity > 0
                }.toString(),
                icon = Icons.Default.CheckCircle,
                iconColor = RailLogColors.SuccessEmerald
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        RailLogSectionHeader(
            title = "Inventory Explorer",
            subtitle = "Search and filter warehouse items"
        )

        RailLogSearchField(
            value = query,
            onValueChange = {
                viewModel.updateSearchQuery(it)
            },
            placeholder = "Cari di gudang..."
        )

        LazyRow(contentPadding = PaddingValues(horizontal = 16.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            items(cats) { cat ->
                FilterChip(
                    selected = selectedCat == cat,
                    onClick = {
                        viewModel.updateInventoryCategory(cat)
                    },
                    label = {
                        Text(
                            cat,
                            fontWeight = FontWeight.SemiBold
                        )
                    },
                    shape = RoundedCornerShape(12.dp),
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = RailLogColors.PrimaryNavy,
                        selectedLabelColor = Color.White
                    )
                )
            }
        }
        LazyColumn(
            modifier = Modifier.weight(1f),
            contentPadding = PaddingValues(
                start = 16.dp,
                end = 16.dp,
                top = 8.dp,
                bottom = 100.dp
            ),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            if (items.isEmpty()) {
                item {
                    EmptyStateBox("No inventory data available")
                }
            } else {
                items(items) {
                    InventoryItemCard(it)
                }
            }

            item {
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}

@Composable
fun StaffHistoryTab(viewModel: StaffMainViewModel) {
    val filters = listOf("Semua", "Pending", "Terverifikasi", "Ditolak")
    val query by viewModel.historySearchQuery.collectAsState()
    val filter by viewModel.historyFilter.collectAsState()
    val items by viewModel.filteredHistoryItems.collectAsState()

    Column(modifier = Modifier.fillMaxSize()) {
        Text("Riwayat Audit", fontSize = 24.sp, fontWeight = FontWeight.ExtraBold, color = RailLogColors.PrimaryNavy, modifier = Modifier.padding(16.dp))
        RailLogSearchField(
            value = query,
            onValueChange = {
                viewModel.updateHistorySearchQuery(it)
            },
            placeholder = "Cari ID Proyek..."
        )
        LazyRow(contentPadding = PaddingValues(horizontal = 16.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            items(filters) { f ->
                FilterChip(selected = filter.contains(f, true) || (f == "Semua" && filter == "All Requests"), onClick = { viewModel.updateHistoryFilter(if(f=="Semua") "All Requests" else f) }, label = { Text(f, fontWeight = FontWeight.Bold) })
            }
        }
        LazyColumn(contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            if (items.isEmpty()) item { EmptyStateBox("Belum ada riwayat.") }
            else items(items) { HistoryCard(it) }
            item { Spacer(modifier = Modifier.height(72.dp)) }
        }
    }
}

@Composable
fun EmptyStateBox(msg: String) {

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 48.dp),
        contentAlignment = Alignment.Center
    ) {

        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            Icon(
                Icons.Default.Inbox,
                contentDescription = null,
                modifier = Modifier.size(56.dp),
                tint = Color.LightGray
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                msg,
                color = Color.Gray,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
fun HistoryCard(item: SupplyItem) {

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        ),
        border = BorderStroke(
            1.dp,
            Color(0xFFE5E7EB)
        )
    ) {

        Column(
            modifier = Modifier.padding(18.dp)
        ) {

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {

                Column {

                    Text(
                        item.partCode,
                        fontSize = 12.sp,
                        color = Color.Gray
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        item.name,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = RailLogColors.PrimaryNavy
                    )
                }

                RailLogStatusChip(
                    status = item.status.name
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            HorizontalDivider(
                color = Color(0xFFF1F5F9)
            )

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {

                Text(
                    "${item.quantity} ${item.unit}",
                    fontWeight = FontWeight.SemiBold
                )

                Text(
                    formatTimestamp(
                        item.createdAt.toEpochMilliseconds()
                    ),
                    color = Color.Gray,
                    fontSize = 12.sp
                )
            }
        }
    }
}

@Composable
fun DraftCard(
    draft: DraftItem,
    onClick: () -> Unit
) {

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        ),
        border = BorderStroke(
            1.dp,
            Color(0xFFE2E8F0)
        )
    ) {

        Column(
            modifier = Modifier.padding(18.dp)
        ) {

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {

                Text(
                    draft.projectTitle.ifEmpty {
                        "Railway Requisition"
                    },
                    fontWeight = FontWeight.Bold,
                    color = RailLogColors.PrimaryNavy
                )

                AssistChip(
                    onClick = {},
                    label = {
                        Text("Draft")
                    }
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                "Progress Form",
                color = Color.Gray,
                fontSize = 12.sp
            )

            Spacer(modifier = Modifier.height(6.dp))

            LinearProgressIndicator(
                progress = { draft.currentStep / 5f },
                modifier = Modifier.fillMaxWidth(),
                color = RailLogColors.PrimaryNavy
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                "Step ${draft.currentStep} of 5",
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@Composable
fun InventoryItemCard(
    item: SupplyItem
) {

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        ),
        border = BorderStroke(
            1.dp,
            Color(0xFFE2E8F0)
        )
    ) {

        Row(
            modifier = Modifier.padding(18.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {

            Column(
                modifier = Modifier.weight(1f)
            ) {

                Text(
                    item.name,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    item.partCode,
                    color = Color.Gray,
                    fontSize = 12.sp
                )

                Spacer(modifier = Modifier.height(6.dp))

                AssistChip(
                    onClick = {},
                    label = {
                        Text(item.category.name)
                    }
                )
            }

            Column(
                horizontalAlignment = Alignment.End
            ) {

                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = RailLogColors.AISurface
                    )
                ) {
                    Text(
                        "${item.quantity}",
                        modifier = Modifier.padding(
                            horizontal = 14.dp,
                            vertical = 8.dp
                        ),
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(
                    Modifier.height(6.dp)
                )

                Text(
                    item.unit,
                    fontSize = 11.sp,
                    color = Color.Gray
                )
            }
        }
    }
}