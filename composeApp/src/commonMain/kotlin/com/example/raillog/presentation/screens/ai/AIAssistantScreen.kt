package com.example.raillog.presentation.screens.ai

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch
import org.koin.compose.viewmodel.koinViewModel

private val RailBlue = Color(0xFF193255)
private val RailLightBlue = Color(0xFFF0F4FA)
private val SurfaceGray = Color(0xFFF7F9FC)
private val AISurface = Color(0xFFEEF2FF)
private val AIBorder = Color(0xFFC7D2FE)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AIAssistantScreen(
    noteId: Long?,
    initialText: String?,
    onNavigateBack: () -> Unit,
    onApplyResult: ((String) -> Unit)? = null,
    viewModel: AIAssistantViewModel = koinViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val listState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()

    // Auto-scroll saat pesan baru masuk
    LaunchedEffect(uiState.messages.size) {
        if (uiState.messages.isNotEmpty()) {
            listState.animateScrollToItem(uiState.messages.size - 1)
        }
    }

    // Isi input jika ada initialText
    LaunchedEffect(initialText) {
        if (!initialText.isNullOrBlank()) {
            viewModel.updateInput(initialText)
        }
    }

    Scaffold(
        containerColor = SurfaceGray,
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .background(AISurface, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Default.AutoAwesome,
                                contentDescription = null,
                                tint = Color(0xFF6366F1),
                                modifier = Modifier.size(18.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                "RailLog Intelligence",
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp,
                                color = RailBlue
                            )
                            Text(
                                "AI Decision Support",
                                fontSize = 11.sp,
                                color = Color.Gray
                            )
                        }
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = RailBlue
                        )
                    }
                },
                actions = {
                    IconButton(onClick = { viewModel.clearConversation() }) {
                        Icon(
                            Icons.Default.DeleteOutline,
                            contentDescription = "Clear chat",
                            tint = Color.Gray
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
            )
        },
        bottomBar = {
            ChatInputBar(
                value = uiState.inputText,
                onValueChange = { viewModel.updateInput(it) },
                onSend = {
                    viewModel.sendMessage()
                    coroutineScope.launch {
                        if (uiState.messages.isNotEmpty()) {
                            listState.animateScrollToItem(uiState.messages.size - 1)
                        }
                    }
                },
                isLoading = uiState.isLoading
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            if (uiState.messages.isEmpty()) {
                // ==================== WELCOME STATE ====================
                WelcomeState(
                    onQuickAction = { prompt -> viewModel.sendQuickMessage(prompt) }
                )
            } else {
                // ==================== CHAT LIST ====================
                LazyColumn(
                    state = listState,
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(uiState.messages) { message ->
                        ChatBubble(
                            message = message,
                            onApply = onApplyResult
                        )
                    }

                    // Loading indicator
                    if (uiState.isLoading) {
                        item {
                            TypingIndicator()
                        }
                    }

                    item { Spacer(modifier = Modifier.height(8.dp)) }
                }
            }
        }
    }
}

// ==================== WELCOME STATE ====================
@Composable
private fun WelcomeState(onQuickAction: (String) -> Unit) {
    val quickActions = listOf(

        QuickAction(
            icon = Icons.Default.Inventory2,
            title = "Analisis Material",
            subtitle = "Evaluasi kebutuhan material proyek",
            prompt = "Analisis kebutuhan material untuk proyek perkeretaapian dan identifikasi item kritis yang harus diprioritaskan."
        ),

        QuickAction(
            icon = Icons.Default.Description,
            title = "Review Dokumen",
            subtitle = "Validasi dokumen teknis",
            prompt = "Apa saja poin yang harus diverifikasi pada Delivery Order, SPK, dan dokumen pengadaan material kereta api?"
        ),

        QuickAction(
            icon = Icons.Default.WarningAmber,
            title = "Risk Assessment",
            subtitle = "Identifikasi risiko logistik",
            prompt = "Identifikasi risiko keterlambatan material dan dampaknya terhadap proyek perkeretaapian."
        ),

        QuickAction(
            icon = Icons.Default.FactCheck,
            title = "AI Verification",
            subtitle = "Panduan validasi hasil AI",
            prompt = "Bagaimana cara memverifikasi hasil ekstraksi AI dan confidence score sebelum approval?"
        )
    )

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(16.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(AISurface, RoundedCornerShape(16.dp))
                    .padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Box(
                        modifier = Modifier
                            .size(64.dp)
                            .background(Color(0xFF6366F1).copy(alpha = 0.15f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Default.AutoAwesome,
                            contentDescription = null,
                            tint = Color(0xFF6366F1),
                            modifier = Modifier.size(32.dp)
                        )
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        "RailLog Intelligence Center",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = RailBlue
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        "Gunakan AI untuk membantu analisis material, validasi dokumen, assessment risiko logistik, dan pengambilan keputusan operasional.",
                        fontSize = 13.sp,
                        color = Color.Gray,
                        textAlign = TextAlign.Center,
                        lineHeight = 18.sp
                    )
                }
            }
        }

        item {
            Text(
                "Mulai dengan pertanyaan cepat:",
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                color = RailBlue
            )
        }

        items(quickActions) { action ->
            QuickActionCard(action = action, onClick = { onQuickAction(action.prompt) })
        }

        item { Spacer(modifier = Modifier.height(80.dp)) }
    }
}

@Composable
private fun QuickActionCard(action: QuickAction, onClick: () -> Unit) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        border = CardDefaults.outlinedCardBorder()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(RailLightBlue, RoundedCornerShape(10.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(action.icon, contentDescription = null, tint = RailBlue, modifier = Modifier.size(20.dp))
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(action.title, fontWeight = FontWeight.SemiBold, fontSize = 14.sp, color = RailBlue)
                Text(action.subtitle, fontSize = 12.sp, color = Color.Gray)
            }
            Icon(
                Icons.Default.ChevronRight,
                contentDescription = null,
                tint = Color.LightGray,
                modifier = Modifier.size(18.dp)
            )
        }
    }
}

// ==================== CHAT BUBBLE ====================
@Composable
private fun ChatBubble(
    message: ChatMessage,
    onApply: ((String) -> Unit)?
) {
    val isUser = message.role == MessageRole.USER

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (isUser) Arrangement.End else Arrangement.Start,
        verticalAlignment = Alignment.Bottom
    ) {
        if (!isUser) {
            Box(
                modifier = Modifier
                    .size(28.dp)
                    .clip(CircleShape)
                    .background(AISurface),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Default.AutoAwesome,
                    contentDescription = null,
                    tint = Color(0xFF6366F1),
                    modifier = Modifier.size(14.dp)
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
        }

        Column(
            modifier = Modifier.widthIn(max = 300.dp),
            horizontalAlignment = if (isUser) Alignment.End else Alignment.Start
        ) {
            Box(
                modifier = Modifier
                    .background(
                        color = if (isUser) RailBlue else Color.White,
                        shape = RoundedCornerShape(
                            topStart = 16.dp,
                            topEnd = 16.dp,
                            bottomStart = if (isUser) 16.dp else 4.dp,
                            bottomEnd = if (isUser) 4.dp else 16.dp
                        )
                    )
                    .then(
                        if (!isUser) Modifier
                            .background(Color.White, RoundedCornerShape(16.dp, 16.dp, 16.dp, 4.dp))
                        else Modifier
                    )
                    .padding(horizontal = 14.dp, vertical = 10.dp)
            ) {
                Text(
                    text = message.content,
                    fontSize = 14.sp,
                    color = if (isUser) Color.White else Color(0xFF1F2937),
                    lineHeight = 20.sp
                )
            }

            // Tombol Apply jika ada onApply callback dan ini pesan AI
            if (!isUser && onApply != null) {
                Spacer(modifier = Modifier.height(4.dp))
                TextButton(
                    onClick = { onApply(message.content) },
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                ) {
                    Icon(
                        Icons.Default.ContentCopy,
                        contentDescription = null,
                        modifier = Modifier.size(12.dp),
                        tint = Color.Gray
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Salin teks", fontSize = 11.sp, color = Color.Gray)
                }
            }
        }

        if (isUser) {
            Spacer(modifier = Modifier.width(8.dp))
            Box(
                modifier = Modifier
                    .size(28.dp)
                    .clip(CircleShape)
                    .background(RailBlue.copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Default.Person,
                    contentDescription = null,
                    tint = RailBlue,
                    modifier = Modifier.size(14.dp)
                )
            }
        }
    }
}

// ==================== TYPING INDICATOR ====================
@Composable
private fun TypingIndicator() {
    Row(
        verticalAlignment = Alignment.Bottom
    ) {
        Box(
            modifier = Modifier
                .size(28.dp)
                .clip(CircleShape)
                .background(AISurface),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                Icons.Default.AutoAwesome,
                contentDescription = null,
                tint = Color(0xFF6366F1),
                modifier = Modifier.size(14.dp)
            )
        }
        Spacer(modifier = Modifier.width(8.dp))
        Box(
            modifier = Modifier
                .background(Color.White, RoundedCornerShape(16.dp, 16.dp, 16.dp, 4.dp))
                .padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                repeat(3) {
                    Box(
                        modifier = Modifier
                            .size(6.dp)
                            .background(Color.Gray, CircleShape)
                    )
                }
                Spacer(modifier = Modifier.width(4.dp))
                Text("Sedang mengetik...", fontSize = 12.sp, color = Color.Gray)
            }
        }
    }
}

// ==================== CHAT INPUT BAR ====================
@Composable
private fun ChatInputBar(
    value: String,
    onValueChange: (String) -> Unit,
    onSend: () -> Unit,
    isLoading: Boolean
) {
    Surface(
        color = Color.White,
        shadowElevation = 8.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.Bottom
        ) {
            OutlinedTextField(
                value = value,
                onValueChange = onValueChange,
                modifier = Modifier.weight(1f),
                placeholder = {
                    Text("Tanyakan sesuatu...", color = Color.Gray, fontSize = 14.sp)
                },
                shape = RoundedCornerShape(24.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = RailBlue,
                    unfocusedBorderColor = Color.LightGray,
                    focusedContainerColor = Color(0xFFF9FAFB),
                    unfocusedContainerColor = Color(0xFFF9FAFB)
                ),
                maxLines = 4,
                enabled = !isLoading
            )
            Spacer(modifier = Modifier.width(8.dp))
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .background(
                        color = if (value.isNotBlank() && !isLoading) RailBlue else Color.LightGray,
                        shape = CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        color = Color.White,
                        strokeWidth = 2.dp
                    )
                } else {
                    IconButton(
                        onClick = { if (value.isNotBlank()) onSend() },
                        enabled = value.isNotBlank()
                    ) {
                        Icon(
                            Icons.AutoMirrored.Filled.Send,
                            contentDescription = "Send",
                            tint = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
        }
    }
}

// ==================== DATA MODELS ====================
data class QuickAction(
    val icon: ImageVector,
    val title: String,
    val subtitle: String,
    val prompt: String
)