package com.example.raillog.presentation.screens.welcome

import androidx.compose.animation.core.Animatable
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.DirectionsTransit
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

@Composable
fun WelcomeScreen(
    onNavigateToLogin: () -> Unit
) {
    val darkBlueTop = Color(0xFF234B76)
    val darkBlueBottom = Color(0xFF04142D)

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(darkBlueTop, darkBlueBottom)))
            // Mengamankan dari status bar atas dan navigation bar bawah
            .statusBarsPadding()
            .navigationBarsPadding()
            .padding(horizontal = 32.dp, vertical = 24.dp)
    ) {
        // --- LOGO (Kiri Atas) ---
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(top = 16.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .background(Color.White, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.DirectionsTransit,
                    contentDescription = "Logo",
                    tint = darkBlueBottom,
                    modifier = Modifier.size(20.dp)
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = "RailLog",
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 20.sp
            )
        }

        // --- KONTEN (Kiri Bawah) ---
        Column(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(bottom = 32.dp) // Jarak ekstra dari dasar layar
        ) {
            Text(
                text = "Streamlining Rail\nLogistics with\nAI Precision",
                color = Color.White,
                fontSize = 36.sp,
                fontWeight = FontWeight.ExtraBold,
                lineHeight = 42.sp,
                letterSpacing = (-1).sp
            )

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "Advanced tracking, predictive maintenance,\nand optimized routing for Nusantara's modern\nrail network.",
                color = Color.White.copy(alpha = 0.7f),
                fontSize = 15.sp,
                lineHeight = 22.sp,
                fontWeight = FontWeight.Normal
            )

            Spacer(modifier = Modifier.height(56.dp))

            // --- TOMBOL GESER (START) ---
            SwipeToStartPill(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp),
                onUnlock = onNavigateToLogin
            )
        }
    }
}

@Composable
fun SwipeToStartPill(
    modifier: Modifier = Modifier,
    onUnlock: () -> Unit
) {
    val darkBlueBottom = Color(0xFF04142D)
    val thumbSize = 52.dp
    val thumbPadding = 6.dp

    var containerWidth by remember { mutableStateOf(0) }
    val thumbSizePx = with(LocalDensity.current) { (thumbSize + (thumbPadding * 2)).toPx() }

    val offsetX = remember { Animatable(0f) }
    val coroutineScope = rememberCoroutineScope()
    var isUnlocked by remember { mutableStateOf(false) }

    Box(
        modifier = modifier
            .height(64.dp)
            .background(Color(0xFF193255).copy(alpha = 0.6f), RoundedCornerShape(32.dp))
            .border(1.dp, Color.White.copy(alpha = 0.15f), RoundedCornerShape(32.dp))
            .onSizeChanged { containerWidth = it.width },
        contentAlignment = Alignment.CenterStart
    ) {
        // Teks "Start" dan Chevron (>>>) di tengah
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
                modifier = Modifier.padding(start = 12.dp)
            )
            Spacer(modifier = Modifier.width(24.dp))
            Row {
                Icon(Icons.Default.ChevronRight, null, tint = Color.White.copy(alpha = 0.3f), modifier = Modifier.size(18.dp))
                Icon(Icons.Default.ChevronRight, null, tint = Color.White.copy(alpha = 0.6f), modifier = Modifier.size(18.dp))
                Icon(Icons.Default.ChevronRight, null, tint = Color.White, modifier = Modifier.size(18.dp))
            }
        }

        // Handle Lingkaran Putih yang Digeser
        Box(
            modifier = Modifier
                .offset { IntOffset(offsetX.value.roundToInt(), 0) }
                .padding(thumbPadding)
                .size(thumbSize)
                .background(Color.White, CircleShape)
                .pointerInput(Unit) {
                    detectHorizontalDragGestures(
                        onDragEnd = {
                            val threshold = containerWidth * 0.6f // Berhasil jika geser > 60%
                            if (offsetX.value >= threshold) {
                                coroutineScope.launch {
                                    offsetX.animateTo(containerWidth - thumbSizePx)
                                    if (!isUnlocked) {
                                        isUnlocked = true
                                        onUnlock()
                                    }
                                }
                            } else {
                                coroutineScope.launch { offsetX.animateTo(0f) }
                            }
                        }
                    ) { change, dragAmount ->
                        change.consume()
                        if (!isUnlocked) {
                            coroutineScope.launch {
                                val newOffset = (offsetX.value + dragAmount)
                                    .coerceIn(0f, containerWidth - thumbSizePx)
                                offsetX.snapTo(newOffset)
                            }
                        }
                    }
                },
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.PlayArrow,
                contentDescription = null,
                tint = darkBlueBottom,
                modifier = Modifier.size(28.dp)
            )
        }
    }
}