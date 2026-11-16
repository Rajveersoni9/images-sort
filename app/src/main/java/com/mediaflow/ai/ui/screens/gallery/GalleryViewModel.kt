package com.mediaflow.ai.ui.screens.gallery

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mediaflow.ai.domain.model.MediaItem
import com.mediaflow.ai.domain.usecase.DeleteMediaUseCase
import com.mediaflow.ai.domain.usecase.GetMediaUseCase
import com.mediaflow.ai.domain.usecase.RunAiOrganizationUseCase
import com.mediaflow.ai.domain.usecase.SyncMediaStoreUseCase
import com.mediaflow.ai.domain.usecase.ToggleFavoriteUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject

@HiltViewModel
class GalleryViewModel @Inject constructor(
    private val getMediaUseCase: GetMediaUseCase,
    private val toggleFavoriteUseCase: ToggleFavoriteUseCase,
    private val deleteMediaUseCase: DeleteMediaUseCase,
    private val syncMediaStoreUseCase: SyncMediaStoreUseCase,
    private val runAiOrganizationUseCase: RunAiOrganizationUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(GalleryUiState(isLoading = true))
    val uiState: StateFlow<GalleryUiState> = _uiState.asStateFlow()

    init {
        loadMedia()
        observeRealtimeNotifications()
    }

    private fun loadMedia() {
        viewModelScope.launch {
            getMediaUseCase.getAll().collectLatest { items ->
                val grouped = groupMediaByDate(items)
                _uiState.update {
                    it.copy(
                        mediaItems = items,
                        groupedMedia = grouped,
                        isLoading = false
                    )
                }
            }
        }
    }

    private fun observeRealtimeNotifications() {
        viewModelScope.launch {
            runAiOrganizationUseCase.notificationBanner.collectLatest { msg ->
                _uiState.update { it.copy(realTimeMessage = msg) }
            }
        }
    }

    fun syncWithDeviceStorage() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            syncMediaStoreUseCase()
            runAiOrganizationUseCase.runBatchClassification()
            _uiState.update { it.copy(isLoading = false) }
        }
    }

    fun toggleFavorite(id: Long, isFavorite: Boolean) {
        viewModelScope.launch {
            toggleFavoriteUseCase(id, isFavorite)
        }
    }

    fun toggleSelection(id: Long) {
        _uiState.update { state ->
            val newSelection = state.selectedIds.toMutableSet()
            if (newSelection.contains(id)) {
                newSelection.remove(id)
            } else {
                newSelection.add(id)
            }
            state.copy(
                selectedIds = newSelection,
                isSelectionMode = newSelection.isNotEmpty()
            )
        }
    }

    fun clearSelection() {
        _uiState.update {
            it.copy(
                selectedIds = emptySet(),
                isSelectionMode = false
            )
        }
    }

    fun deleteSelected() {
        viewModelScope.launch {
            val ids = _uiState.value.selectedIds.toList()
            deleteMediaUseCase(ids)
            clearSelection()
        }
    }

    fun setColumnCount(columns: Int) {
        if (columns in 2..4) {
            _uiState.update { it.copy(columnCount = columns) }
        }
    }

    private fun groupMediaByDate(items: List<MediaItem>): Map<String, List<MediaItem>> {
        val dateFormat = SimpleDateFormat("MMMM d, yyyy", Locale.getDefault())
        val today = SimpleDateFormat("yyyyMMdd", Locale.getDefault()).format(Date())
        val yesterday = SimpleDateFormat("yyyyMMdd", Locale.getDefault()).format(Date(System.currentTimeMillis() - 86400000L))

        return items.groupBy { item ->
            val itemDateCode = SimpleDateFormat("yyyyMMdd", Locale.getDefault()).format(Date(item.dateTaken))
            when (itemDateCode) {
                today -> "Today"
                yesterday -> "Yesterday"
                else -> dateFormat.format(Date(item.dateTaken))
            }
        }
    }
}
