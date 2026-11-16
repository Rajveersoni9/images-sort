package com.mediaflow.ai.ui.screens.viewer

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mediaflow.ai.domain.model.MediaItem
import com.mediaflow.ai.domain.usecase.DeleteMediaUseCase
import com.mediaflow.ai.domain.usecase.GetMediaUseCase
import com.mediaflow.ai.domain.usecase.ToggleFavoriteUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class MediaViewerUiState(
    val mediaItems: List<MediaItem> = emptyList(),
    val currentIndex: Int = 0,
    val showMetadataSheet: Boolean = false,
    val isLoading: Boolean = false
) {
    val currentItem: MediaItem?
        get() = mediaItems.getOrNull(currentIndex)
}

@HiltViewModel
class MediaViewerViewModel @Inject constructor(
    private val getMediaUseCase: GetMediaUseCase,
    private val toggleFavoriteUseCase: ToggleFavoriteUseCase,
    private val deleteMediaUseCase: DeleteMediaUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(MediaViewerUiState(isLoading = true))
    val uiState: StateFlow<MediaViewerUiState> = _uiState.asStateFlow()

    fun loadMedia(initialId: Long) {
        viewModelScope.launch {
            getMediaUseCase.getAll().collectLatest { items ->
                val index = items.indexOfFirst { it.id == initialId }.coerceAtLeast(0)
                _uiState.update {
                    it.copy(
                        mediaItems = items,
                        currentIndex = index,
                        isLoading = false
                    )
                }
            }
        }
    }

    fun onPageChanged(newIndex: Int) {
        _uiState.update { it.copy(currentIndex = newIndex) }
    }

    fun toggleFavorite() {
        val current = _uiState.value.currentItem ?: return
        viewModelScope.launch {
            toggleFavoriteUseCase(current.id, !current.isFavorite)
        }
    }

    fun deleteCurrent(onDeleted: () -> Unit) {
        val current = _uiState.value.currentItem ?: return
        viewModelScope.launch {
            deleteMediaUseCase(listOf(current.id))
            onDeleted()
        }
    }

    fun showMetadataSheet(show: Boolean) {
        _uiState.update { it.copy(showMetadataSheet = show) }
    }
}
