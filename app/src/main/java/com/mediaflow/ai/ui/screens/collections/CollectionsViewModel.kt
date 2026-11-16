package com.mediaflow.ai.ui.screens.collections

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mediaflow.ai.domain.model.MediaItem
import com.mediaflow.ai.domain.model.SceneCategory
import com.mediaflow.ai.domain.model.SmartAlbum
import com.mediaflow.ai.domain.model.SmartAlbumType
import com.mediaflow.ai.domain.usecase.GetAlbumsUseCase
import com.mediaflow.ai.domain.usecase.GetMediaUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class CollectionsUiState(
    val albums: List<SmartAlbum> = emptyList(),
    val isLoading: Boolean = false
)

data class AlbumDetailUiState(
    val album: SmartAlbum? = null,
    val items: List<MediaItem> = emptyList(),
    val isLoading: Boolean = false
)

@HiltViewModel
class CollectionsViewModel @Inject constructor(
    private val getAlbumsUseCase: GetAlbumsUseCase,
    private val getMediaUseCase: GetMediaUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(CollectionsUiState(isLoading = true))
    val uiState: StateFlow<CollectionsUiState> = _uiState.asStateFlow()

    private val _albumDetailState = MutableStateFlow(AlbumDetailUiState())
    val albumDetailState: StateFlow<AlbumDetailUiState> = _albumDetailState.asStateFlow()

    init {
        loadAlbums()
    }

    private fun loadAlbums() {
        viewModelScope.launch {
            getAlbumsUseCase().collectLatest { albumList ->
                _uiState.update {
                    it.copy(
                        albums = albumList,
                        isLoading = false
                    )
                }
            }
        }
    }

    fun loadAlbumDetails(albumId: String) {
        viewModelScope.launch {
            val album = _uiState.value.albums.find { it.id == albumId }
            _albumDetailState.update { it.copy(album = album, isLoading = true) }

            when (album?.type) {
                SmartAlbumType.PEOPLE -> {
                    getMediaUseCase.getByCategory(SceneCategory.PEOPLE).collectLatest { items ->
                        _albumDetailState.update { it.copy(items = items, isLoading = false) }
                    }
                }
                SmartAlbumType.TRAVEL -> {
                    getMediaUseCase.getByCategory(SceneCategory.TRAVEL).collectLatest { items ->
                        _albumDetailState.update { it.copy(items = items, isLoading = false) }
                    }
                }
                SmartAlbumType.NATURE -> {
                    getMediaUseCase.getByCategory(SceneCategory.NATURE).collectLatest { items ->
                        _albumDetailState.update { it.copy(items = items, isLoading = false) }
                    }
                }
                SmartAlbumType.CARS -> {
                    getMediaUseCase.getByCategory(SceneCategory.CARS).collectLatest { items ->
                        _albumDetailState.update { it.copy(items = items, isLoading = false) }
                    }
                }
                SmartAlbumType.FOOD -> {
                    getMediaUseCase.getByCategory(SceneCategory.FOOD).collectLatest { items ->
                        _albumDetailState.update { it.copy(items = items, isLoading = false) }
                    }
                }
                SmartAlbumType.DOCUMENTS -> {
                    getMediaUseCase.getByCategory(SceneCategory.DOCUMENT).collectLatest { items ->
                        _albumDetailState.update { it.copy(items = items, isLoading = false) }
                    }
                }
                SmartAlbumType.SCREENSHOTS -> {
                    getMediaUseCase.getByCategory(SceneCategory.SCREENSHOT).collectLatest { items ->
                        _albumDetailState.update { it.copy(items = items, isLoading = false) }
                    }
                }
                SmartAlbumType.VIDEOS -> {
                    getMediaUseCase.getVideos().collectLatest { items ->
                        _albumDetailState.update { it.copy(items = items, isLoading = false) }
                    }
                }
                SmartAlbumType.FAVORITES -> {
                    getMediaUseCase.getFavorites().collectLatest { items ->
                        _albumDetailState.update { it.copy(items = items, isLoading = false) }
                    }
                }
                SmartAlbumType.DUPLICATES -> {
                    getMediaUseCase.getDuplicates().collectLatest { items ->
                        _albumDetailState.update { it.copy(items = items, isLoading = false) }
                    }
                }
                else -> {
                    getMediaUseCase.getAll().collectLatest { items ->
                        _albumDetailState.update { it.copy(items = items, isLoading = false) }
                    }
                }
            }
        }
    }
}
