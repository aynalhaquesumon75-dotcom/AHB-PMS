package com.example.ui.theme

import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

// Core Dark Navy / Charcoal Surfaces
val NavyVoid = Color(0xFF070C18)
val NavyBackground = Color(0xFF090F1D)
val NavySurface = Color(0xFF111A2E)
val NavySurfaceElevated = Color(0xFF17223B)
val NavySurfaceHighlight = Color(0xFF1E2D4D)
val NavyBorder = Color(0xFF263554)
val NavyBorderSubtle = Color(0xFF1C2942)

// Text Hierarchy
val TextPrimary = Color(0xFFF1F5F9)
val TextSecondary = Color(0xFF94A3B8)
val TextMuted = Color(0xFF64748B)

// Electric Blue & Violet Gradient Accents
val ElectricBlue = Color(0xFF2563EB)
val SkyBlue = Color(0xFF38BDF8)
val BrightBlue = Color(0xFF3B82F6)
val RoyalPurple = Color(0xFF7C3AED)
val SoftViolet = Color(0xFFA78BFA)

// Semantic Status Colors
val EmeraldSuccess = Color(0xFF10B981)
val EmeraldContainer = Color(0xFF064E3B)
val AmberWarning = Color(0xFFF59E0B)
val AmberContainer = Color(0xFF451A03)
val CoralError = Color(0xFFEF4444)
val CoralContainer = Color(0xFF450A0A)
val CyanAccent = Color(0xFF06B6D4)

// Gradient Brushes
val PrimaryGradientBrush = Brush.linearGradient(
    colors = listOf(ElectricBlue, RoyalPurple)
)

val HeroBannerBrush = Brush.linearGradient(
    colors = listOf(Color(0xFF13244A), Color(0xFF1D4ED8), Color(0xFF6D28D9))
)

val SidebarGradientBrush = Brush.verticalGradient(
    colors = listOf(Color(0xFF0E172B), Color(0xFF070C18))
)

val ChartBarGradientBrush = Brush.verticalGradient(
    colors = listOf(SkyBlue, ElectricBlue, RoyalPurple)
)
