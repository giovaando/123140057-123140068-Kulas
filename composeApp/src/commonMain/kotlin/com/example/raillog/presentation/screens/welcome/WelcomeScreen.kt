package com.example.raillog.presentation.screens.welcome

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.DirectionsTransit
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

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
            .padding(32.dp)
    ) {
        // Logo & Judul
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(top = 16.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .background(Color.White, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.DirectionsTransit,
                    contentDescription = "Logo",
                    tint = darkBlueBottom,
                    modifier = Modifier.size(24.dp)
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = "RailLog",
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 22.sp
            )
        }

        // Konten & Tombol Aksi
        Column(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(bottom = 16.dp)
        ) {
            Text(
                text = "Streamlining Rail\nLogistics with\nAI Precision",
                color = Color.White,
                fontSize = 38.sp,
                fontWeight = FontWeight.ExtraBold,
                lineHeight = 44.sp,
                letterSpacing = (-1).sp
            )

            Spacer(modifier = Modifier.height(20.dp))

            Text(
                text = "Advanced tracking, predictive maintenance,\nand optimized routing for Nusantara's modern\nrail network.",
                color = Color(0xFFB0C4DE),
                fontSize = 15.sp,
                lineHeight = 22.sp
            )

            Spacer(modifier = Modifier.height(48.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Tombol Back (Dekorasi)
                Box(
                    modifier = Modifier
                        .size(64.dp)
                        .border(1.dp, Color.White.copy(alpha = 0.3f), CircleShape)
                        .clip(CircleShape)
                        .clickable { },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = "Back",
                        tint = Color.White,
                        modifier = Modifier.size(24.dp)
                    )
                }

                Spacer(modifier = Modifier.width(20.dp))

                // Tombol Start
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(64.dp)
                        .background(Color(0xFF193255).copy(alpha = 0.6f), RoundedCornerShape(32.dp))
                        .border(1.dp, Color.White.copy(alpha = 0.15f), RoundedCornerShape(32.dp))
                        .clip(RoundedCornerShape(32.dp))
                        .clickable { onNavigateToLogin() },
                    contentAlignment = Alignment.CenterStart
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .background(Color.White, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.PlayArrow,
                                contentDescription = "Start",
                                tint = darkBlueBottom,
                                modifier = Modifier.size(28.dp)
                            )
                        }

                        Text(
                            text = "Start",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp
                        )

                        Row(modifier = Modifier.padding(end = 12.dp)) {
                            Icon(Icons.Default.ChevronRight, contentDescription = null, tint = Color.White.copy(alpha = 0.2f), modifier = Modifier.size(18.dp))
                            Icon(Icons.Default.ChevronRight, contentDescription = null, tint = Color.White.copy(alpha = 0.5f), modifier = Modifier.size(18.dp))
                            Icon(Icons.Default.ChevronRight, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
                        }
                    }
                }
            }
        }
    }
}