package com.mediaflow.ai.ui.screens.gallery

import com.mediaflow.ai.domain.model.MediaItem

data class GalleryUiState(
    val mediaItems: List<MediaItem> = emptyList(),
    val groupedMedia: Map<String, List<MediaItem>> = emptyMap(),
    val columnCount: Int = 3,
    val selectedIds: Set<Long> = emptySet(),
    val isSelectionMode: Boolean = false,
    val isLoading: Boolean = false,
    val realTimeMessage: String? = null,
    val errorMessage: String? = null
)
