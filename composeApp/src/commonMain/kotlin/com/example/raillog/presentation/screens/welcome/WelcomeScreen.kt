package com.example.raillog.presentation.screens.welcome

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.DirectionsRailway
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.raillog.data.local.datastore.DataStoreFactory
import com.example.raillog.presentation.screens.login.GlobalSessionManager
import kotlinx.coroutines.launch
import org.koin.compose.koinInject
import kotlin.math.roundToInt

val WelcomeBg = Color(0xFF1E3A5F)
val BrandNavy = Color(0xFF00236F)
val SuccessGreen = Color(0xFF10B981)
val TextGray = Color(0xFFB0C4DE)

@Composable
fun WelcomeScreen(
    onNavigateToLogin: () -> Unit,
    onAutoLogin: (String) -> Unit = {},
    dataStoreFactory: DataStoreFactory = koinInject()
) {
    // --- LOGIKA AUTO-LOGIN DIKEMBALIKAN ---
    val userPreferences = remember { GlobalSessionManager.getPrefs(dataStoreFactory) }
    val savedRole by userPreferences.userRole.collectAsState(initial = "")

    LaunchedEffect(savedRole) {
        if (savedRole.isNotEmpty()) {
            onAutoLogin(savedRole)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(WelcomeBg)
            .padding(32.dp)
            .statusBarsPadding()
            .navigationBarsPadding(),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        // --- HEADER ---
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(top = 16.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .background(Color.White, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.DirectionsRailway,
                    contentDescription = "Logo",
                    tint = BrandNavy,
                    modifier = Modifier.size(28.dp)
                )
            }
            Spacer(modifier = Modifier.width(16.dp))
            Text(
                text = "RailLog",
                color = Color.White,
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold
            )
        }

        // --- BAGIAN TEKS TENGAH ---
        Column {
            Text(
                text = "Streamlining Rail\nLogistics with\nAI Precision",
                color = Color.White,
                fontSize = 40.sp,
                fontWeight = FontWeight.ExtraBold,
                lineHeight = 46.sp,
                letterSpacing = (-1).sp
            )
            Spacer(modifier = Modifier.height(24.dp))
            Text(
                text = "Advanced tracking, predictive maintenance,\nand optimized routing for Nusantara's\nmodern rail network.",
                color = TextGray,
                fontSize = 16.sp,
                lineHeight = 24.sp
            )
        }

        // --- TOMBOL SWIPE TO START ---
        // Menggunakan parameter onNavigateToLogin yang diminta oleh AppNavHost
        SwipeToStartButton(onSwipeComplete = onNavigateToLogin)
    }
}

@Composable
fun SwipeToStartButton(onSwipeComplete: () -> Unit) {
    val thumbSize = 64.dp
    val thumbSizePx = with(LocalDensity.current) { thumbSize.toPx() }
    val coroutineScope = rememberCoroutineScope()

    var dragOffset by remember { mutableFloatStateOf(0f) }

    BoxWithConstraints(
        modifier = Modifier
            .fillMaxWidth()
            .height(80.dp)
            .clip(RoundedCornerShape(40.dp))
            .background(Color.Black.copy(alpha = 0.2f))
            .border(1.dp, Color.White.copy(alpha = 0.1f), RoundedCornerShape(40.dp))
            .padding(8.dp),
        contentAlignment = Alignment.CenterStart
    ) {
        val maxWidthPx = constraints.maxWidth.toFloat()
        val maxDragPx = maxWidthPx - thumbSizePx

        val progress = if (maxDragPx > 0) (dragOffset / maxDragPx).coerceIn(0f, 1f) else 0f

        val currentThumbColor = lerp(Color.White, SuccessGreen, progress)
        val currentIconColor = lerp(BrandNavy, Color.White, progress)

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Start",
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
                modifier = Modifier.padding(end = 8.dp)
            )
            AnimatedArrows()
        }

        Box(
            modifier = Modifier
                .offset { IntOffset(dragOffset.roundToInt(), 0) }
                .size(thumbSize)
                .background(currentThumbColor, CircleShape)
                .pointerInput(Unit) {
                    detectHorizontalDragGestures(
                        onDragEnd = {
                            if (dragOffset > maxDragPx * 0.8f) {
                                coroutineScope.launch {
                                    dragOffset = maxDragPx
                                    onSwipeComplete()
                                }
                            } else {
                                coroutineScope.launch {
                                    val anim = Animatable(dragOffset)
                                    anim.animateTo(0f, tween(300)) {
                                        dragOffset = value
                                    }
                                }
                            }
                        }
                    ) { change, dragAmount ->
                        change.consume()
                        dragOffset = (dragOffset + dragAmount).coerceIn(0f, maxDragPx)
                    }
                },
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.PlayArrow,
                contentDescription = "Swipe to start",
                tint = currentIconColor,
                modifier = Modifier.size(32.dp)
            )
        }
    }
}

@Composable
fun AnimatedArrows() {
    Row(horizontalArrangement = Arrangement.spacedBy((-4).dp)) {
        for (i in 0..2) {
            val infiniteTransition = rememberInfiniteTransition(label = "arrow_$i")
            val alpha by infiniteTransition.animateFloat(
                initialValue = 0.2f,
                targetValue = 1f,
                animationSpec = infiniteRepeatable(
                    animation = tween(durationMillis = 500, easing = LinearEasing),
                    repeatMode = RepeatMode.Reverse,
                    initialStartOffset = StartOffset(offsetMillis = i * 200)
                ),
                label = "alpha_anim_$i"
            )
            Icon(
                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = null,
                tint = Color.White.copy(alpha = alpha),
                modifier = Modifier.size(20.dp)
            )
        }
    }
}