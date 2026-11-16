package com.mediaflow.ai.ui.screens.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mediaflow.ai.domain.model.AppSettings
import com.mediaflow.ai.domain.model.AppThemeMode
import com.mediaflow.ai.domain.repository.SettingsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val settingsRepository: SettingsRepository
) : ViewModel() {

    private val _settings = MutableStateFlow(AppSettings())
    val settings: StateFlow<AppSettings> = _settings.asStateFlow()

    init {
        viewModelScope.launch {
            settingsRepository.getSettings().collectLatest { s ->
                _settings.value = s
            }
        }
    }

    fun setThemeMode(mode: AppThemeMode) {
        update { it.copy(themeMode = mode) }
    }

    fun setAccentColor(hex: String) {
        update { it.copy(accentColorHex = hex) }
    }

    fun toggleRealTimeSorting(enabled: Boolean) {
        update { it.copy(isRealTimeSortingEnabled = enabled) }
    }

    fun toggleDuplicateDetection(enabled: Boolean) {
        update { it.copy(isAutoDuplicateDetectionEnabled = enabled) }
    }

    fun toggleOcr(enabled: Boolean) {
        update { it.copy(isOcrEnabled = enabled) }
    }

    fun toggleFaceGrouping(enabled: Boolean) {
        update { it.copy(isFaceGroupingEnabled = enabled) }
    }

    fun toggleNotifications(enabled: Boolean) {
        update { it.copy(isNotificationsEnabled = enabled) }
    }

    private fun update(block: (AppSettings) -> AppSettings) {
        val updated = block(_settings.value)
        _settings.value = updated
        viewModelScope.launch {
            settingsRepository.updateSettings(updated)
        }
    }
}
