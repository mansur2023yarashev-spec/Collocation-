package com.example.ui.theme

import androidx.compose.runtime.Composable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

// Dark Theme Colors (Exact match to the user's screenshot)
val DarkPlumBg = Color(0xFF110D13)          // Outer pitch dark background with subtle aubergine warmth
val DarkPlumCard = Color(0xFF1B121C)        // Rounded card container (eggplant / deep plum)
val DarkPlumBorder = Color(0xFF331F32)      // Subtle plum border
val DarkPlumRow = Color(0xFF261826)         // Pill row & search bar container
val DarkPlumRowBorder = Color(0xFF3B243A)   // Pill row & button border
val DarkPlumTextPrimary = Color(0xFFF6EFF6) // Crisp white primary text
val DarkPlumTextSecondary = Color(0xFF8E758C)// Grayish mauve (count badge, subtitle, alphabet, icons)
val DarkPlumAccent = Color(0xFFD897C0)      // Soft rose-plum accent (selected tab)
val DarkPlumHeartActive = Color(0xFFE05D82) // Active favorite red/pink

// Light Theme Colors (Harmonious light mode companion)
val LightMauveBg = Color(0xFFF5EFF5)
val LightMauveCard = Color(0xFFFFFFFF)
val LightMauveBorder = Color(0xFFE3D5E2)
val LightMauveRow = Color(0xFFFAF4FA)
val LightMauveRowBorder = Color(0xFFE9DCE8)
val LightMauveTextPrimary = Color(0xFF221522)
val LightMauveTextSecondary = Color(0xFF7E687D)
val LightMauveAccent = Color(0xFF8E3A75)
val LightMauveHeartActive = Color(0xFFDC2626)

data class AppThemeColors(
    val isDark: Boolean,
    val bg: Color,
    val card: Color,
    val border: Color,
    val buttonDark: Color,
    val buttonWhite: Color,
    val rowBorder: Color,
    val accentTerracotta: Color,
    val accentForest: Color,
    val textPrimary: Color,
    val textSecondary: Color,
    val searchBarBg: Color,
    val wordColor: Color,
    val selectedTabTint: Color,
    val heartActive: Color
)

// Dark / Tungi rejim (matching user screenshot)
val DarkAppColors = AppThemeColors(
    isDark = true,
    bg = DarkPlumBg,
    card = DarkPlumCard,
    border = DarkPlumBorder,
    buttonDark = DarkPlumRow,
    buttonWhite = DarkPlumRow,
    rowBorder = DarkPlumRowBorder,
    accentTerracotta = DarkPlumAccent,
    accentForest = DarkPlumRow,
    textPrimary = DarkPlumTextPrimary,
    textSecondary = DarkPlumTextSecondary,
    searchBarBg = DarkPlumRow,
    wordColor = DarkPlumTextPrimary,
    selectedTabTint = DarkPlumAccent,
    heartActive = DarkPlumHeartActive
)

// Light / Yorug'lik rejimi
val LightAppColors = AppThemeColors(
    isDark = false,
    bg = LightMauveBg,
    card = LightMauveCard,
    border = LightMauveBorder,
    buttonDark = LightMauveRow,
    buttonWhite = LightMauveRow,
    rowBorder = LightMauveRowBorder,
    accentTerracotta = LightMauveAccent,
    accentForest = LightMauveRow,
    textPrimary = LightMauveTextPrimary,
    textSecondary = LightMauveTextSecondary,
    searchBarBg = LightMauveRow,
    wordColor = LightMauveTextPrimary,
    selectedTabTint = LightMauveAccent,
    heartActive = LightMauveHeartActive
)

val LocalAppColors = staticCompositionLocalOf { DarkAppColors }

val OxfordBg: Color
    @Composable get() = LocalAppColors.current.bg

val OxfordCard: Color
    @Composable get() = LocalAppColors.current.card

val OxfordBorder: Color
    @Composable get() = LocalAppColors.current.border

val OxfordButtonDark: Color
    @Composable get() = LocalAppColors.current.buttonDark

val OxfordButtonWhite: Color
    @Composable get() = LocalAppColors.current.buttonWhite

val OxfordRowBorder: Color
    @Composable get() = LocalAppColors.current.rowBorder

val OxfordAccentTerracotta: Color
    @Composable get() = LocalAppColors.current.accentTerracotta

val OxfordAccentForest: Color
    @Composable get() = LocalAppColors.current.accentForest

val OxfordAccentGold: Color
    @Composable get() = LocalAppColors.current.accentTerracotta

val OxfordAccentGoldLight: Color
    @Composable get() = LocalAppColors.current.textPrimary

val OxfordTextPrimary: Color
    @Composable get() = LocalAppColors.current.textPrimary

val OxfordTextSecondary: Color
    @Composable get() = LocalAppColors.current.textSecondary

val OxfordSearchBarBg: Color
    @Composable get() = LocalAppColors.current.searchBarBg

val JsonWordColor: Color
    @Composable get() = LocalAppColors.current.wordColor

val OxfordSelectedTab: Color
    @Composable get() = LocalAppColors.current.selectedTabTint

val OxfordHeartActive: Color
    @Composable get() = LocalAppColors.current.heartActive

// Note and task tag colors
val NoteBlue = Color(0xFF3B82F6)
val NotePink = Color(0xFFEC4899)
val NotePurple = Color(0xFF8B5CF6)
val NoteOrange = Color(0xFFF97316)

// Standard theme colors
val PrimaryBlue = Color(0xFF2563EB)
val PrimaryBlueDark = Color(0xFF60A5FA)
val PrimaryContainerLight = Color(0xFFDBEAFE)
val PrimaryContainerDark = Color(0xFF1E3A8A)

val SecondaryTeal = Color(0xFF059669)
val SecondaryTealDark = Color(0xFF34D399)
val SecondaryContainerLight = Color(0xFFD1FAE5)
val SecondaryContainerDark = Color(0xFF064E3B)

val TertiaryAmber = Color(0xFFD97706)
val TertiaryAmberDark = Color(0xFFFBBF24)
val TertiaryContainerLight = Color(0xFFFEF3C7)
val TertiaryContainerDark = Color(0xFF78350F)

val ExpenseRed = Color(0xFFEF4444)
val IncomeGreen = Color(0xFF10B981)

val BackgroundLight = Color(0xFF070B19)
val SurfaceLight = Color(0xFF0E162E)
val SurfaceVariantLight = Color(0xFF131D38)
val TextPrimaryLight = Color(0xFFFFFFFF)
val TextSecondaryLight = Color(0xFF8B9CB7)

val BackgroundDark = Color(0xFF070B19)
val SurfaceDark = Color(0xFF0E162E)
val SurfaceVariantDark = Color(0xFF131D38)
val TextPrimaryDark = Color(0xFFFFFFFF)
val TextSecondaryDark = Color(0xFF8B9CB7)
