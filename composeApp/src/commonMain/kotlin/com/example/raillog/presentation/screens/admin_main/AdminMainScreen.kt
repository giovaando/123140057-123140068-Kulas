package com.example.raillog.presentation.screens.admin_main

import androidx.compose.animation.animateContentSize
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
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.raillog.domain.model.SupplyItem
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
import org.koin.compose.viewmodel.koinViewModel

// ==================== Warna dari DESIGN.md ====================
val BrandNavy = Color(0xFF00236F)
val SuccessGreen = Color(0xFF10B981)
val SurfaceBg = Color(0xFFF7F9FB)
val BorderColor = Color(0xFFE2E8F0)
val TextMuted = Color(0xFF64748B)
val ErrorRed = Color(0xFFBA1A1A)
val LowConfidenceBg = Color(0xFFFFDAD6)
val HighConfidenceBg = Color(0xFFDCE1FF)

// ==================== Helper Models & Functions ====================
data class SystemAlert(
    val id: Int,
    val title: String,
    val timeAgo: String,
    val description: String
)

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

// ==================== DATA MODELS UNTUK REQUISITION DETAIL ====================
data class Requisition(
    val id: Long,
    val requisitionId: String,
    val requesterName: String,
    val date: String,
    val items: List<RequisitionItem>,
    val status: RequisitionStatus,
    val notes: String? = null
)

data class RequisitionItem(
    val name: String,
    val sku: String,
    val requestedQuantity: Int,
    val approvedQuantity: Int? = null
)

enum class RequisitionStatus {
    PENDING, VERIFIED, REJECTED
}

interface RequisitionRepository {
    suspend fun getRequisitionById(id: Long): Requisition?
}

// ==================== VIEWMODEL UNTUK DETAIL ====================
class RequisitionDetailViewModel(
    private val repository: RequisitionRepository
) : ViewModel() {
    private val _requisition = MutableStateFlow<Requisition?>(null)
    val requisition: StateFlow<Requisition?> = _requisition.asStateFlow()

    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    fun loadRequisition(id: Long) {
        viewModelScope.launch {
            _isLoading.value = true
            _requisition.value = repository.getRequisitionById(id)
            _isLoading.value = false
        }
    }

    fun approve() {
        // TODO: Panggil API/DB Approve
    }

    fun revise() {
        // TODO: Panggil API/DB Revise
    }
}

// ==================== SCREEN UTAMA (TABS) ====================
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminMainScreen(
    viewModel: AdminMainViewModel = koinViewModel(),
    onNavigateToVerificationDetail: (Long) -> Unit,
    onLogout: () -> Unit
) {
    var selectedTab by remember { mutableIntStateOf(1) }

    val pendingItems by viewModel.pendingRequisitions.collectAsState()
    val allItems by viewModel.allItems.collectAsState()

    Scaffold(
        containerColor = SurfaceBg,
        topBar = {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = SurfaceBg,
                shadowElevation = 0.dp
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .statusBarsPadding()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(modifier = Modifier.size(36.dp).background(Color.LightGray, CircleShape))
                    Text(
                        text = "RailLog Nusantara",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = BrandNavy,
                        modifier = Modifier.padding(start = 12.dp).weight(1f)
                    )
                    IconButton(onClick = onLogout) {
                        Icon(Icons.Default.Logout, contentDescription = "Logout", tint = ErrorRed)
                    }
                }
            }
        },
        bottomBar = {
            Surface(
                modifier = Modifier.border(BorderStroke(1.dp, BorderColor)),
                color = Color.White,
                tonalElevation = 0.dp
            ) {
                NavigationBar(containerColor = Color.Transparent, tonalElevation = 0.dp) {
                    val items = listOf("Inventory", "Verification", "Operations")
                    items.forEachIndexed { index, item ->
                        NavigationBarItem(
                            selected = selectedTab == index,
                            onClick = { selectedTab = index },
                            label = { Text(item, fontSize = 12.sp, fontWeight = FontWeight.Medium) },
                            icon = {
                                Icon(
                                    when (index) {
                                        0 -> Icons.Default.Inventory
                                        1 -> Icons.Default.FactCheck
                                        else -> Icons.Default.Dashboard
                                    },
                                    contentDescription = null
                                )
                            },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = BrandNavy,
                                selectedTextColor = BrandNavy,
                                unselectedIconColor = TextMuted,
                                unselectedTextColor = TextMuted,
                                indicatorColor = BrandNavy.copy(alpha = 0.1f)
                            )
                        )
                    }
                }
            }
        }
    ) { paddingValues ->
        Box(modifier = Modifier.padding(paddingValues)) {
            when (selectedTab) {
                0 -> InventoryTab(allItems)
                1 -> VerificationTab(viewModel, onNavigateToVerificationDetail)
                2 -> OperationsTab(allItems, pendingItems.size)
            }
        }
    }
}

// ==================== INVENTORY TAB ====================
@Composable
fun InventoryTab(allItems: List<SupplyItem>) {
    val maxCapacity = 1000
    val verifiedItems = allItems.filter { it.status.name == "VERIFIED" }

    val infraUsed = verifiedItems.filter { it.category.name.contains("INFRA", true) }.sumOf { it.quantity }
    val spareUsed = verifiedItems.filter { it.category.name.contains("SPARE", true) }.sumOf { it.quantity }
    val toolsUsed = verifiedItems.filter { it.category.name.contains("TOOL", true) }.sumOf { it.quantity }

    val infraLeft = (maxCapacity - infraUsed).coerceAtLeast(0)
    val spareLeft = (maxCapacity - spareUsed).coerceAtLeast(0)
    val toolsLeft = (maxCapacity - toolsUsed).coerceAtLeast(0)

    val infraProgress = (infraLeft.toFloat() / maxCapacity).coerceIn(0f, 1f)
    val spareProgress = (spareLeft.toFloat() / maxCapacity).coerceIn(0f, 1f)
    val toolsProgress = (toolsLeft.toFloat() / maxCapacity).coerceIn(0f, 1f)

    val urgentItems = mutableListOf<Triple<String, String, String>>()
    if (infraProgress < 0.2f) urgentItems.add(Triple("Infrastructure Items", "CAT-INFRA", "$infraLeft Units Left"))
    if (spareProgress < 0.2f) urgentItems.add(Triple("Spare Parts", "CAT-SPARE", "$spareLeft Units Left"))
    if (toolsProgress < 0.2f) urgentItems.add(Triple("Tools & Equipment", "CAT-TOOL", "$toolsLeft Units Left"))

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        item { Text("Inventory Overview", fontSize = 24.sp, fontWeight = FontWeight.SemiBold, color = BrandNavy) }
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp),
                colors = CardDefaults.cardColors(Color.White),
                border = BorderStroke(1.dp, BorderColor)
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    InventoryProgressItem("Infrastructure", infraProgress, isWarning = infraProgress < 0.2f)
                    InventoryProgressItem("Rolling Stock", spareProgress, isWarning = spareProgress < 0.2f)
                    InventoryProgressItem("Electronics", toolsProgress, isWarning = toolsProgress < 0.2f)
                }
            }
        }

        if (urgentItems.isNotEmpty()) {
            item { Text("Urgent Replenishments", fontSize = 18.sp, fontWeight = FontWeight.SemiBold, color = BrandNavy, modifier = Modifier.padding(top = 8.dp)) }
            items(urgentItems) { (name, sku, stock) ->
                Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(8.dp), colors = CardDefaults.cardColors(Color.White), border = BorderStroke(1.dp, BorderColor)) {
                    Row(modifier = Modifier.fillMaxWidth().padding(16.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Column { Text(name, fontWeight = FontWeight.Medium, color = BrandNavy); Text(sku, fontSize = 12.sp, color = TextMuted) }
                        Text(stock, color = ErrorRed, fontWeight = FontWeight.Medium, fontSize = 14.sp)
                    }
                }
            }
        }
    }
}

@Composable
fun InventoryProgressItem(title: String, progress: Float, isWarning: Boolean = false) {
    Column {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(title, fontWeight = FontWeight.Medium, color = BrandNavy)
            Text("${(progress * 100).toInt()}%", color = if (isWarning) ErrorRed else TextMuted, fontWeight = FontWeight.Medium)
        }
        Spacer(modifier = Modifier.height(8.dp))
        LinearProgressIndicator(progress = { progress }, modifier = Modifier.fillMaxWidth().height(8.dp).clip(RoundedCornerShape(4.dp)), color = if (isWarning) ErrorRed else BrandNavy, trackColor = BorderColor)
    }
}

// ==================== VERIFICATION TAB ====================
@Composable
fun VerificationTab(viewModel: AdminMainViewModel, onItemClick: (Long) -> Unit) {
    val searchQuery by viewModel.searchQuery.collectAsState()
    val selectedFilter by viewModel.selectedFilter.collectAsState()
    val filteredItems by viewModel.filteredPendingItems.collectAsState()
    val filterOptions = listOf("All", "Pending", "Verified", "Rejected")

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item { Text("Verification Queue", fontSize = 24.sp, fontWeight = FontWeight.SemiBold, color = BrandNavy) }
        item {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { viewModel.updateSearchQuery(it) },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("Search by Request Name...", color = TextMuted) },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = TextMuted) },
                shape = RoundedCornerShape(8.dp),
                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = BrandNavy, unfocusedBorderColor = BorderColor, focusedTextColor = BrandNavy),
                singleLine = true
            )
        }
        item {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                filterOptions.forEachIndexed { index, label ->
                    FilterChip(
                        selected = selectedFilter == index,
                        onClick = { viewModel.updateSelectedFilter(index) },
                        label = { Text(label, fontSize = 13.sp) },
                        modifier = Modifier.height(36.dp),
                        colors = FilterChipDefaults.filterChipColors(selectedContainerColor = BrandNavy.copy(alpha = 0.1f), selectedLabelColor = BrandNavy),
                        border = BorderStroke(1.dp, if (selectedFilter == index) BrandNavy else BorderColor)
                    )
                }
            }
        }

        items(filteredItems) { item ->
            VerificationCard(item, onItemClick)
        }

        if (filteredItems.isEmpty()) {
            item {
                Box(modifier = Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                    Text("No documents found.", color = TextMuted)
                }
            }
        }
    }
}

@Composable
fun VerificationCard(item: SupplyItem, onClick: (Long) -> Unit) {
    val confidence = 75 + (item.id % 25).toInt()
    val isHighConfidence = confidence >= 85

    Card(
        modifier = Modifier.fillMaxWidth().clickable { onClick(item.id) },
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(Color.White),
        border = BorderStroke(1.dp, BorderColor)
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text("REQ-${item.id.toString().padStart(4, '0')}", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = BrandNavy)
                Text(formatAdminTimestamp(item.createdAt.toEpochMilliseconds()), fontSize = 12.sp, color = TextMuted)
            }
            Text(item.name, fontWeight = FontWeight.Medium, fontSize = 15.sp, color = BrandNavy.copy(alpha = 0.8f))
            Text("Requested Quantity: ${item.quantity} | Category: ${item.category.name}", fontSize = 13.sp, color = TextMuted, maxLines = 2, overflow = TextOverflow.Ellipsis)
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text("AI Extraction Confidence", fontSize = 12.sp, color = TextMuted)
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = if (isHighConfidence) HighConfidenceBg else LowConfidenceBg,
                    border = BorderStroke(0.5.dp, if (isHighConfidence) BrandNavy.copy(alpha = 0.3f) else ErrorRed.copy(alpha = 0.3f))
                ) {
                    Text("${confidence}%", modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp), fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = if (isHighConfidence) BrandNavy else ErrorRed)
                }
            }
        }
    }
}

// ==================== OPERATIONS TAB ====================
@Composable
fun OperationsTab(allItems: List<SupplyItem>, pendingCount: Int) {
    val verifiedCount = allItems.count { it.status.name == "VERIFIED" }
    val alerts = remember {
        listOf(
            SystemAlert(1, "Database Sync", "10m ago", "Background sync completed successfully."),
            SystemAlert(2, "System Status", "1h ago", "All nodes operating at optimal capacity.")
        )
    }

    LazyColumn(modifier = Modifier.fillMaxSize(), contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(20.dp)) {
        item { Text("Operations Overview", fontSize = 24.sp, fontWeight = FontWeight.SemiBold, color = BrandNavy) }
        item {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                OperationsMetricCard("VERIFIED ITEMS", verifiedCount.toString(), trend = "Approved", trendUp = true, modifier = Modifier.weight(1f))
                OperationsMetricCard("AVG AI CONFIDENCE", "94.2%", subtitle = "Optimal", modifier = Modifier.weight(1f))
                OperationsMetricCard("PENDING QUEUE", pendingCount.toString(), subtitle = if (pendingCount > 0) "Requires Action" else "All Clear", modifier = Modifier.weight(1f))
            }
        }
        item {
            Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(8.dp), colors = CardDefaults.cardColors(Color.White), border = BorderStroke(1.dp, BorderColor)) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Verification Accuracy Trend", fontWeight = FontWeight.SemiBold, color = BrandNavy)
                    Spacer(modifier = Modifier.height(12.dp))
                    Box(modifier = Modifier.fillMaxWidth().height(120.dp).border(1.dp, BorderColor, RoundedCornerShape(4.dp)).background(Color(0xFFF8FAFC)), contentAlignment = Alignment.Center) {
                        Text("Line Chart Visualization Space", color = TextMuted, fontSize = 12.sp)
                    }
                }
            }
        }
        item { Text("System Alerts", fontSize = 18.sp, fontWeight = FontWeight.SemiBold, color = BrandNavy) }
        items(alerts) { alert ->
            Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(8.dp), colors = CardDefaults.cardColors(Color.White), border = BorderStroke(1.dp, BorderColor)) {
                Row(modifier = Modifier.padding(16.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Icon(Icons.Default.Info, contentDescription = null, tint = BrandNavy, modifier = Modifier.size(20.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(alert.title, fontWeight = FontWeight.Medium, color = BrandNavy)
                        Text(alert.timeAgo, fontSize = 11.sp, color = TextMuted)
                        Text(alert.description, fontSize = 13.sp, color = TextMuted, maxLines = 2)
                    }
                }
            }
        }
    }
}

@Composable
fun OperationsMetricCard(title: String, value: String, subtitle: String? = null, trend: String? = null, trendUp: Boolean = true, modifier: Modifier = Modifier) {
    Card(modifier = modifier, shape = RoundedCornerShape(8.dp), colors = CardDefaults.cardColors(Color.White), border = BorderStroke(1.dp, BorderColor)) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(title, fontSize = 11.sp, fontWeight = FontWeight.Medium, color = TextMuted, letterSpacing = 0.5.sp)
            Spacer(modifier = Modifier.height(4.dp))
            Text(value, fontSize = 20.sp, fontWeight = FontWeight.Bold, color = BrandNavy)
            if (subtitle != null) { Text(subtitle, fontSize = 11.sp, color = if (subtitle == "All Clear" || subtitle == "Optimal") SuccessGreen else ErrorRed) }
            if (trend != null) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(if (trendUp) Icons.Default.ArrowUpward else Icons.Default.ArrowDownward, contentDescription = null, modifier = Modifier.size(12.dp), tint = if (trendUp) SuccessGreen else ErrorRed)
                    Spacer(modifier = Modifier.width(2.dp))
                    Text(trend, fontSize = 11.sp, color = if (trendUp) SuccessGreen else ErrorRed)
                }
            }
        }
    }
}

// ==================== DETAIL VERIFY REQUISITION DENGAN AI ====================
@Composable
fun VerifyRequisitionScreen(
    viewModel: RequisitionDetailViewModel,
    onApprove: () -> Unit,
    onRevise: () -> Unit,
    onBack: () -> Unit
) {
    val requisition by viewModel.requisition.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()

    var isAiAnalyzing by remember { mutableStateOf(false) }
    var isAiResultReady by remember { mutableStateOf(false) }

    LaunchedEffect(isAiAnalyzing) {
        if (isAiAnalyzing) {
            delay(2500L)
            isAiAnalyzing = false
            isAiResultReady = true
        }
    }

    if (isLoading) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator(color = BrandNavy)
        }
        return
    }

    requisition?.let { req ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(SurfaceBg)
                .padding(top = 32.dp)
                .statusBarsPadding()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBack) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = BrandNavy)
                }
                Text(
                    text = "Verify Requisition",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = BrandNavy
                )
            }

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 20.dp, vertical = 8.dp)
            ) {
                Text(
                    text = "Requisition ID: ${req.requisitionId}",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    color = TextMuted
                )
                Spacer(modifier = Modifier.height(20.dp))

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(Color.White),
                    border = BorderStroke(1.dp, BorderColor)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Requested Items", fontWeight = FontWeight.Bold, color = BrandNavy)
                        Spacer(modifier = Modifier.height(12.dp))
                        req.items.forEach { item ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 6.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(item.name, color = BrandNavy, fontWeight = FontWeight.Medium, fontSize = 14.sp)
                                    Text(item.sku, color = TextMuted, fontSize = 12.sp)
                                }
                                Text("${item.requestedQuantity} pcs", color = BrandNavy, fontWeight = FontWeight.Bold)
                            }
                            HorizontalDivider(color = SurfaceBg)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                Text("AI Verification Analysis", fontWeight = FontWeight.SemiBold, color = BrandNavy)
                Spacer(modifier = Modifier.height(8.dp))

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .animateContentSize(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(if (isAiResultReady) HighConfidenceBg else Color.White),
                    border = BorderStroke(1.dp, if (isAiResultReady) BrandNavy.copy(alpha = 0.3f) else BorderColor)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        when {
                            !isAiAnalyzing && !isAiResultReady -> {
                                Icon(Icons.Default.AutoAwesome, contentDescription = "AI", tint = BrandNavy, modifier = Modifier.size(32.dp))
                                Spacer(modifier = Modifier.height(8.dp))
                                Text("Document is ready for analysis", color = TextMuted, fontSize = 13.sp)
                                Spacer(modifier = Modifier.height(16.dp))
                                Button(
                                    onClick = { isAiAnalyzing = true },
                                    colors = ButtonDefaults.buttonColors(containerColor = BrandNavy),
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Text("Analyze with Gemini", color = Color.White)
                                }
                            }
                            isAiAnalyzing -> {
                                CircularProgressIndicator(color = BrandNavy, modifier = Modifier.size(32.dp))
                                Spacer(modifier = Modifier.height(12.dp))
                                Text("Extracting specs & cross-referencing...", color = BrandNavy, fontWeight = FontWeight.Medium, fontSize = 13.sp)
                            }
                            isAiResultReady -> {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(Icons.Default.CheckCircle, null, tint = SuccessGreen)
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text("Specs Matched", fontWeight = FontWeight.Bold, color = BrandNavy)
                                    }
                                    Text("Confidence: 94%", fontWeight = FontWeight.ExtraBold, color = SuccessGreen)
                                }
                                Spacer(modifier = Modifier.height(12.dp))
                                Text(
                                    text = "AI Summary: All requested quantities align with standard maintenance protocols for Type-B signaling systems. No anomalies detected in supplier SKUs.",
                                    fontSize = 13.sp,
                                    color = BrandNavy.copy(alpha = 0.8f),
                                    lineHeight = 18.sp
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.weight(1f))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedButton(
                        onClick = onRevise,
                        modifier = Modifier
                            .weight(1f)
                            .height(50.dp),
                        enabled = isAiResultReady,
                        border = BorderStroke(1.dp, if (isAiResultReady) ErrorRed else BorderColor),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = ErrorRed),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("Revise")
                    }

                    Button(
                        onClick = onApprove,
                        modifier = Modifier
                            .weight(2f)
                            .height(50.dp),
                        enabled = isAiResultReady,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = SuccessGreen,
                            disabledContainerColor = Color.LightGray
                        ),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("Approve Request", fontWeight = FontWeight.Bold, color = Color.White)
                    }
                }
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}