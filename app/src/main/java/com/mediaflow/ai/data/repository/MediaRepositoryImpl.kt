package com.mediaflow.ai.data.repository

import android.net.Uri
import com.mediaflow.ai.data.local.dao.AlbumDao
import com.mediaflow.ai.data.local.dao.MediaDao
import com.mediaflow.ai.data.local.entity.AlbumEntity
import com.mediaflow.ai.data.local.entity.MediaEntity
import com.mediaflow.ai.data.mediastore.MediaStoreScanner
import com.mediaflow.ai.domain.model.MediaItem
import com.mediaflow.ai.domain.model.MediaType
import com.mediaflow.ai.domain.model.SceneCategory
import com.mediaflow.ai.domain.model.SmartAlbum
import com.mediaflow.ai.domain.model.SmartAlbumType
import com.mediaflow.ai.domain.repository.MediaRepository
import com.mediaflow.ai.util.SampleDataGenerator
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MediaRepositoryImpl @Inject constructor(
    private val mediaDao: MediaDao,
    private val albumDao: AlbumDao,
    private val mediaStoreScanner: MediaStoreScanner
) : MediaRepository {

    override fun getAllMedia(): Flow<List<MediaItem>> {
        return mediaDao.getAllMediaFlow().map { entities ->
            if (entities.isEmpty()) {
                // Populate sample dataset on first launch
                populateSampleDataIfEmpty()
            }
            entities.map { it.toDomainModel() }
        }.flowOn(Dispatchers.IO)
    }

    override fun getFavoriteMedia(): Flow<List<MediaItem>> {
        return mediaDao.getFavoriteMediaFlow().map { list ->
            list.map { it.toDomainModel() }
        }.flowOn(Dispatchers.IO)
    }

    override fun getMediaByType(type: MediaType): Flow<List<MediaItem>> {
        return mediaDao.getMediaByTypeFlow(type).map { list ->
            list.map { it.toDomainModel() }
        }.flowOn(Dispatchers.IO)
    }

    override fun getMediaByCategory(category: SceneCategory): Flow<List<MediaItem>> {
        val keyword = when (category) {
            SceneCategory.SCREENSHOT -> "screenshot"
            SceneCategory.DOCUMENT -> "doc"
            SceneCategory.PEOPLE -> "selfie"
            SceneCategory.CARS -> "car"
            SceneCategory.FOOD -> "food"
            SceneCategory.PETS -> "pet"
            SceneCategory.TRAVEL -> "travel"
            SceneCategory.NATURE -> "camera"
            else -> ""
        }
        return mediaDao.getMediaByCategoryOrKeywordFlow(category, keyword).map { list ->
            list.map { it.toDomainModel() }
        }.flowOn(Dispatchers.IO)
    }

    override fun getDuplicates(): Flow<List<MediaItem>> {
        return mediaDao.getDuplicatesFlow().map { list ->
            list.map { it.toDomainModel() }
        }.flowOn(Dispatchers.IO)
    }

    override fun getSmartAlbums(): Flow<List<SmartAlbum>> {
        return albumDao.getAllAlbumsFlow().map { list ->
            if (list.isEmpty()) {
                albumDao.insertAll(SampleDataGenerator.generateSampleAlbums())
            }
            list.map { it.toDomainModel() }
        }.flowOn(Dispatchers.IO)
    }

    override suspend fun getMediaById(id: Long): MediaItem? = withContext(Dispatchers.IO) {
        mediaDao.getMediaById(id)?.toDomainModel()
    }

    override suspend fun toggleFavorite(id: Long, isFavorite: Boolean) = withContext(Dispatchers.IO) {
        mediaDao.updateFavorite(id, isFavorite)
    }

    override suspend fun deleteMedia(ids: List<Long>) = withContext(Dispatchers.IO) {
        mediaDao.deleteByIds(ids)
        refreshSmartAlbums()
    }

    override suspend fun syncMediaStore(): Int = withContext(Dispatchers.IO) {
        val deviceMedia = mediaStoreScanner.queryAllMedia()
        if (deviceMedia.isNotEmpty()) {
            mediaDao.insertAll(deviceMedia)
            refreshSmartAlbums()
        }
        deviceMedia.size
    }

    override suspend fun refreshSmartAlbums() = withContext(Dispatchers.IO) {
        val totalFavorites = mediaDao.getFavoritesCount()
        val totalVideos = mediaDao.getVideosCount()
        val totalNature = mediaDao.getCategoryCountWithKeyword(SceneCategory.NATURE, "camera")
        val totalPeople = mediaDao.getCategoryCountWithKeyword(SceneCategory.PEOPLE, "whatsapp")
        val totalTravel = mediaDao.getCategoryCountWithKeyword(SceneCategory.TRAVEL, "travel")
        val totalCars = mediaDao.getCategoryCountWithKeyword(SceneCategory.CARS, "car")
        val totalFood = mediaDao.getCategoryCountWithKeyword(SceneCategory.FOOD, "food")
        val totalDocs = mediaDao.getCategoryCountWithKeyword(SceneCategory.DOCUMENT, "doc")
        val totalScreenshots = mediaDao.getCategoryCountWithKeyword(SceneCategory.SCREENSHOT, "screenshot")

        val peopleCover = mediaDao.getCoverForCategory(SceneCategory.PEOPLE, "whatsapp")?.uriString
        val travelCover = mediaDao.getCoverForCategory(SceneCategory.TRAVEL, "travel")?.uriString
        val natureCover = mediaDao.getCoverForCategory(SceneCategory.NATURE, "camera")?.uriString
        val carsCover = mediaDao.getCoverForCategory(SceneCategory.CARS, "car")?.uriString
        val foodCover = mediaDao.getCoverForCategory(SceneCategory.FOOD, "food")?.uriString
        val docsCover = mediaDao.getCoverForCategory(SceneCategory.DOCUMENT, "doc")?.uriString
        val screenshotsCover = mediaDao.getCoverForCategory(SceneCategory.SCREENSHOT, "screenshot")?.uriString

        val updatedAlbums = listOf(
            AlbumEntity(
                id = "smart_people",
                title = "People & Pets",
                type = SmartAlbumType.PEOPLE,
                coverUriString = peopleCover ?: "https://images.unsplash.com/photo-1534528741775-53994a69daeb?auto=format&fit=crop&w=600&q=80",
                itemCount = totalPeople,
                description = "Faces, portraits, and friends",
                gradientStartHex = "#7C3AED",
                gradientEndHex = "#C084FC"
            ),
            AlbumEntity(
                id = "smart_travel",
                title = "Travel & Places",
                type = SmartAlbumType.TRAVEL,
                coverUriString = travelCover ?: "https://images.unsplash.com/photo-1507525428034-b723cf961d3e?auto=format&fit=crop&w=600&q=80",
                itemCount = totalTravel,
                description = "Beaches, resorts, vacations & landmarks",
                gradientStartHex = "#2563EB",
                gradientEndHex = "#60A5FA"
            ),
            AlbumEntity(
                id = "smart_nature",
                title = "Nature & Mountains",
                type = SmartAlbumType.NATURE,
                coverUriString = natureCover ?: "https://images.unsplash.com/photo-1464822759023-fed622ff2c3b?auto=format&fit=crop&w=600&q=80",
                itemCount = totalNature,
                description = "Forests, peaks, lakes, and landscapes",
                gradientStartHex = "#059669",
                gradientEndHex = "#34D399"
            ),
            AlbumEntity(
                id = "smart_cars",
                title = "Cars & Vehicles",
                type = SmartAlbumType.CARS,
                coverUriString = carsCover ?: "https://images.unsplash.com/photo-1503376780353-7e6692767b70?auto=format&fit=crop&w=600&q=80",
                itemCount = totalCars,
                description = "Supercars, bikes, and automobiles",
                gradientStartHex = "#DC2626",
                gradientEndHex = "#F87171"
            ),
            AlbumEntity(
                id = "smart_food",
                title = "Food & Dining",
                type = SmartAlbumType.FOOD,
                coverUriString = foodCover ?: "https://images.unsplash.com/photo-1555396273-367ea4eb4db5?auto=format&fit=crop&w=600&q=80",
                itemCount = totalFood,
                description = "Dishes, restaurants, and desserts",
                gradientStartHex = "#D97706",
                gradientEndHex = "#FBBF24"
            ),
            AlbumEntity(
                id = "smart_documents",
                title = "Documents & Receipts",
                type = SmartAlbumType.DOCUMENTS,
                coverUriString = docsCover ?: "https://images.unsplash.com/photo-1589829545856-d10d557cf95f?auto=format&fit=crop&w=600&q=80",
                itemCount = totalDocs,
                description = "OCR indexed paper, passports, and bills",
                gradientStartHex = "#475569",
                gradientEndHex = "#94A3B8"
            ),
            AlbumEntity(
                id = "smart_screenshots",
                title = "Screenshots",
                type = SmartAlbumType.SCREENSHOTS,
                coverUriString = screenshotsCover ?: "https://images.unsplash.com/photo-1518770660439-4636190af475?auto=format&fit=crop&w=600&q=80",
                itemCount = totalScreenshots,
                description = "App screens, chats, and captures",
                gradientStartHex = "#4F46E5",
                gradientEndHex = "#818CF8"
            ),
            AlbumEntity(
                id = "smart_videos",
                title = "Videos & Clips",
                type = SmartAlbumType.VIDEOS,
                coverUriString = "https://images.unsplash.com/photo-1507525428034-b723cf961d3e?auto=format&fit=crop&w=600&q=80",
                itemCount = totalVideos,
                description = "High framerate 4K and HD recordings",
                gradientStartHex = "#9333EA",
                gradientEndHex = "#C084FC"
            )
        )
        albumDao.insertAll(updatedAlbums)
    }

    private suspend fun populateSampleDataIfEmpty() {
        val count = mediaDao.getMediaCount()
        if (count == 0) {
            mediaDao.insertAll(SampleDataGenerator.generateSampleMedia())
            albumDao.insertAll(SampleDataGenerator.generateSampleAlbums())
        }
    }

    private fun MediaEntity.toDomainModel(): MediaItem {
        return MediaItem(
            id = id,
            uri = Uri.parse(uriString),
            path = path,
            displayName = displayName,
            mimeType = mimeType,
            mediaType = mediaType,
            dateAdded = dateAdded,
            dateTaken = dateTaken,
            durationMs = durationMs,
            sizeBytes = sizeBytes,
            width = width,
            height = height,
            isFavorite = isFavorite,
            isDuplicate = isDuplicate,
            duplicateGroupId = duplicateGroupId,
            aiTags = aiTags,
            detectedFacesCount = detectedFacesCount,
            ocrText = ocrText,
            sceneCategory = sceneCategory,
            dominantColorHex = dominantColorHex,
            perceptualHash = perceptualHash
        )
    }

    private fun AlbumEntity.toDomainModel(): SmartAlbum {
        return SmartAlbum(
            id = id,
            title = title,
            type = type,
            coverUri = coverUriString?.let { Uri.parse(it) },
            itemCount = itemCount,
            description = description,
            gradientColors = Pair(gradientStartHex, gradientEndHex)
        )
    }
}
