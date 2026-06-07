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
            Text(
                "${getGreeting()},\n$userRole", 
                fontSize = 24.sp, 
                fontWeight = FontWeight.ExtraBold, 
                color = RailLogColors.PrimaryNavy, 
                lineHeight = 30.sp
            )
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(top = 4.dp)) {
                Box(modifier = Modifier.size(8.dp).background(if (pending > 0) RailLogColors.WarningAmber else RailLogColors.SuccessEmerald, CircleShape))
                Spacer(modifier = Modifier.width(8.dp))
                Text(if (pending > 0) "$pending Permintaan Audit Tertunda" else "Status Operasional Aman", color = Color.Black, fontSize = 14.sp, fontWeight = FontWeight.ExtraBold)
            }
        }

        item {
            Card(onClick = onStart, modifier = Modifier.fillMaxWidth().height(100.dp), colors = CardDefaults.cardColors(containerColor = RailLogColors.PrimaryNavy), shape = RoundedCornerShape(12.dp)) {
                Row(modifier = Modifier.fillMaxSize().padding(horizontal = 20.dp), verticalAlignment = Alignment.CenterVertically) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Form Pengadaan Audit", color = Color.White, fontWeight = FontWeight.ExtraBold, fontSize = 18.sp)
                        Text("Mulai alur 5-langkah profesional", color = Color.White.copy(alpha = 0.9f), fontSize = 12.sp)
                    }
                    Icon(Icons.Default.PostAdd, null, tint = Color.White, modifier = Modifier.size(36.dp))
                }
            }
        }

        item {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                MetricCard(Modifier.weight(1f), Icons.Default.Inventory2, "Total Stok", allItems.size.toString(), RailLogColors.PrimaryNavy)
                MetricCard(Modifier.weight(1f), Icons.Default.ReportProblem, "Peringatan", critical.toString(), RailLogColors.ErrorRed, RailLogColors.ErrorBackground)
            }
        }

        item {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text("Audit Terbaru", fontSize = 18.sp, fontWeight = FontWeight.ExtraBold, color = RailLogColors.PrimaryNavy)
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
        FunctionalSearchBar(query, { viewModel.updateSearchQuery(it) }, "Cari berdasarkan nama atau kode...")

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
        FunctionalSearchBar(query, { viewModel.updateSearchQuery(it) }, "Cari di gudang...")
        LazyRow(contentPadding = PaddingValues(horizontal = 16.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            items(cats) { cat ->
                FilterChip(
                    selected = selectedCat == cat, onClick = { viewModel.updateInventoryCategory(cat) },
                    label = { Text(cat, fontWeight = FontWeight.ExtraBold) },
                    colors = FilterChipDefaults.filterChipColors(selectedContainerColor = RailLogColors.PrimaryNavy, selectedLabelColor = Color.White)
                )
            }
        }
        LazyColumn(contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            if (items.isEmpty()) item { EmptyStateBox("Inventaris kosong.") }
            else items(items) { InventoryItemCard(it) }
            item { Spacer(modifier = Modifier.height(72.dp)) }
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
        FunctionalSearchBar(query, { viewModel.updateHistorySearchQuery(it) }, "Cari ID Proyek...")
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
fun FunctionalSearchBar(value: String, onValueChange: (String) -> Unit, placeholder: String) {
    OutlinedTextField(
        value = value, onValueChange = onValueChange,
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
        placeholder = { Text(placeholder, color = Color.Black.copy(alpha = 0.6f)) },
        leadingIcon = { Icon(Icons.Default.Search, null, tint = Color.Black) },
        trailingIcon = { if (value.isNotEmpty()) IconButton(onClick = { onValueChange("") }) { Icon(Icons.Default.Close, null, tint = Color.Black) } },
        shape = RoundedCornerShape(12.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedTextColor = Color.Black, unfocusedTextColor = Color.Black,
            focusedContainerColor = Color.White, unfocusedContainerColor = Color.White,
            unfocusedBorderColor = Color.Black, focusedBorderColor = RailLogColors.PrimaryNavy
        ),
        singleLine = true
    )
}

@Composable
fun EmptyStateBox(msg: String) {
    Box(modifier = Modifier.fillMaxWidth().padding(top = 40.dp), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(Icons.Default.Inbox, null, tint = Color.Black, modifier = Modifier.size(64.dp))
            Spacer(modifier = Modifier.height(16.dp))
            Text(msg, color = Color.Black, fontSize = 14.sp, fontWeight = FontWeight.ExtraBold)
        }
    }
}

@Composable
fun HistoryCard(item: SupplyItem) {
    Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = Color.White), border = BorderStroke(2.dp, Color.Black)) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text(item.partCode, color = RailLogColors.PrimaryNavy, fontWeight = FontWeight.ExtraBold, fontSize = 14.sp)
                Box(modifier = Modifier.background(RailLogColors.AISurface, RoundedCornerShape(4.dp)).padding(horizontal = 8.dp, vertical = 4.dp)) {
                    Text(formatTimestamp(item.createdAt.toEpochMilliseconds()), fontSize = 11.sp, color = RailLogColors.PrimaryNavy, fontWeight = FontWeight.ExtraBold)
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
            Text(item.name, fontSize = 16.sp, fontWeight = FontWeight.ExtraBold, color = Color.Black)
            Spacer(modifier = Modifier.height(16.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Inventory2, null, tint = Color.Black, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("${item.quantity} Unit", fontSize = 14.sp, color = Color.Black, fontWeight = FontWeight.ExtraBold)
                }
                StatusChip(item.status.name)
            }
        }
    }
}

@Composable
fun StatusChip(status: String) {
    val bgColor = when (status) {
        "VERIFIED" -> RailLogColors.SuccessBackground
        "PENDING" -> RailLogColors.WarningBackground
        "REJECTED" -> RailLogColors.ErrorBackground
        else -> RailLogColors.SurfaceSlate
    }
    val textColor = when (status) {
        "VERIFIED" -> RailLogColors.SuccessEmerald
        "PENDING" -> RailLogColors.WarningAmber
        "REJECTED" -> RailLogColors.ErrorRed
        else -> Color.Black
    }
    Box(modifier = Modifier.background(bgColor, RoundedCornerShape(6.dp)).padding(horizontal = 10.dp, vertical = 4.dp)) {
        Text(status, color = textColor, fontSize = 11.sp, fontWeight = FontWeight.ExtraBold)
    }
}

@Composable
fun DraftCard(draft: DraftItem, onClick: () -> Unit) {
    Card(modifier = Modifier.fillMaxWidth().clickable { onClick() }, colors = CardDefaults.cardColors(containerColor = Color.White), border = BorderStroke(2.dp, RailLogColors.PrimaryNavy)) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(draft.projectTitle.ifEmpty { "Audit Requisition" }, color = RailLogColors.PrimaryNavy, fontWeight = FontWeight.ExtraBold, fontSize = 16.sp)
                Box(modifier = Modifier.background(Color.Black, RoundedCornerShape(4.dp)).padding(horizontal = 6.dp, vertical = 2.dp)) {
                    Text("DRAF", fontSize = 10.sp, color = Color.White, fontWeight = FontWeight.ExtraBold)
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text("Langkah ${draft.currentStep} dari 5", fontSize = 12.sp, color = Color.Black, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(12.dp))
            LinearProgressIndicator(progress = { draft.currentStep / 5f }, modifier = Modifier.fillMaxWidth().height(8.dp), color = RailLogColors.PrimaryNavy, trackColor = RailLogColors.BorderGray)
        }
    }
}

@Composable
fun InventoryItemCard(item: SupplyItem) {
    Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = Color.White), border = BorderStroke(2.dp, Color.Black)) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Column(modifier = Modifier.weight(1f)) {
                Text(item.name, fontWeight = FontWeight.ExtraBold, fontSize = 15.sp, color = Color.Black)
                Text("KODE: ${item.partCode}", color = Color.Black, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                Text(item.category.name, color = RailLogColors.PrimaryNavy, fontSize = 11.sp, fontWeight = FontWeight.ExtraBold)
            }
            Box(modifier = Modifier.background(RailLogColors.SuccessBackground, RoundedCornerShape(4.dp)).border(1.dp, RailLogColors.SuccessEmerald, RoundedCornerShape(4.dp)).padding(horizontal = 10.dp, vertical = 6.dp)) {
                Text("${item.quantity} ${item.unit}", fontSize = 13.sp, color = RailLogColors.SuccessEmerald, fontWeight = FontWeight.ExtraBold)
            }
        }
    }
}

@Composable
fun MetricCard(modifier: Modifier, icon: ImageVector, title: String, count: String, color: Color, bg: Color = RailLogColors.AISurface) {
    Card(modifier = modifier, colors = CardDefaults.cardColors(containerColor = Color.White), border = BorderStroke(2.dp, Color.Black)) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Box(modifier = Modifier.size(32.dp).background(bg, RoundedCornerShape(8.dp)), contentAlignment = Alignment.Center) {
                    Icon(icon, null, tint = color, modifier = Modifier.size(18.dp))
                }
                Text(count, fontWeight = FontWeight.ExtraBold, fontSize = 24.sp, color = RailLogColors.PrimaryNavy)
            }
            Spacer(modifier = Modifier.height(12.dp))
            Text(title, fontSize = 12.sp, color = Color.Black, fontWeight = FontWeight.ExtraBold)
        }
    }
}
