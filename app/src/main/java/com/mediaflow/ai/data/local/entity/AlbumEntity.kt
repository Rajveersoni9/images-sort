package com.mediaflow.ai.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.mediaflow.ai.domain.model.SmartAlbumType

@Entity(tableName = "smart_albums")
data class AlbumEntity(
    @PrimaryKey
    val id: String,
    val title: String,
    val type: SmartAlbumType,
    val coverUriString: String?,
    val itemCount: Int,
    val description: String,
    val gradientStartHex: String,
    val gradientEndHex: String
)
