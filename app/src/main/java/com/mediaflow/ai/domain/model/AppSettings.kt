package com.mediaflow.ai.domain.model

enum class AppThemeMode {
    LIGHT,
    DARK,
    SYSTEM
}

data class AppSettings(
    val themeMode: AppThemeMode = AppThemeMode.LIGHT,
    val accentColorHex: String = "#7C3AED",
    val isRealTimeSortingEnabled: Boolean = true,
    val isAutoDuplicateDetectionEnabled: Boolean = true,
    val isOcrEnabled: Boolean = true,
    val isFaceGroupingEnabled: Boolean = true,
    val isNotificationsEnabled: Boolean = true,
    val gridColumnCount: Int = 3,
    val lowPowerMode: Boolean = false
)
