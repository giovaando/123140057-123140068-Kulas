package com.example.raillog.presentation.theme

import androidx.compose.ui.graphics.Color

/**
 * RailLog Nusantara - Extreme Contrast Palette
 * Dioptimalkan untuk visibilitas absolut di lingkungan lapangan.
 */
object RailLogColors {
    // Primary - Deep & Solid
    val PrimaryNavy = Color(0xFF000033)      // Navy super pekat
    val PrimaryNavyLight = Color(0xFF001A4D)
    
    // Status - Bold & 100% Solid
    val SuccessEmerald = Color(0xFF064E3B)   // Hijau hutan pekat
    val SuccessBackground = Color(0xFFDCFCE7)
    
    val ErrorRed = Color(0xFF800000)         // Merah marun pekat
    val ErrorBackground = Color(0xFFFEE2E2)
    
    val WarningAmber = Color(0xFF78350F)     // Oranye tanah pekat
    val WarningBackground = Color(0xFFFEF3C7)

    // UI Surfaces
    val SurfaceSlate = Color(0xFFF8FAFC)     
    val SurfaceWhite = Color(0xFFFFFFFF)
    
    // Form Borders - Solid Black
    val BorderBlack = Color(0xFF000000)      // Hitam Total
    val BorderGray = Color(0xFF1E293B)       // Abu-abu sangat tua

    // Text - Maximum Visibility (Strict No-Alpha)
    val TextPrimary = Color(0xFF000000)      // Hitam Total untuk teks utama & input
    val TextSecondary = Color(0xFF000000)    // Label form juga hitam total
    val TextMuted = Color(0xFF0F172A)        // Biru sangat tua (bukan abu-abu tipis)

    // AI Components
    val AISurface = Color(0xFFF0F7FF)
    val AIBorder = Color(0xFF1E40AF)
}
