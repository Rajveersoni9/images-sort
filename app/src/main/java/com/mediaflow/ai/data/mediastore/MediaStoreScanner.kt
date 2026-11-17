package com.mediaflow.ai.data.mediastore

import android.content.ContentUris
import android.content.Context
import android.database.Cursor
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import com.mediaflow.ai.data.local.entity.MediaEntity
import com.mediaflow.ai.domain.model.MediaType
import com.mediaflow.ai.domain.model.SceneCategory
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MediaStoreScanner @Inject constructor(
    @ApplicationContext private val context: Context
) {
    suspend fun queryAllMedia(): List<MediaEntity> = withContext(Dispatchers.IO) {
        val mediaList = mutableListOf<MediaEntity>()
        mediaList.addAll(queryImages())
        mediaList.addAll(queryVideos())
        mediaList.sortedByDescending { it.dateTaken }
    }

    private fun queryImages(): List<MediaEntity> {
        val list = mutableListOf<MediaEntity>()
        val projection = mutableListOf(
            MediaStore.Images.Media._ID,
            MediaStore.Images.Media.DISPLAY_NAME,
            MediaStore.Images.Media.MIME_TYPE,
            MediaStore.Images.Media.DATE_ADDED,
            MediaStore.Images.Media.DATE_TAKEN,
            MediaStore.Images.Media.SIZE,
            MediaStore.Images.Media.WIDTH,
            MediaStore.Images.Media.HEIGHT,
            MediaStore.Images.Media.DATA
        ).apply {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                add(MediaStore.Images.Media.BUCKET_DISPLAY_NAME)
                add(MediaStore.Images.Media.RELATIVE_PATH)
            }
        }.toTypedArray()

        val sortOrder = "${MediaStore.Images.Media.DATE_TAKEN} DESC"

        runCatching {
            context.contentResolver.query(
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                projection,
                null,
                null,
                sortOrder
            )?.use { cursor ->
                val idCol = cursor.getColumnIndexOrThrow(MediaStore.Images.Media._ID)
                val nameCol = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DISPLAY_NAME)
                val mimeCol = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.MIME_TYPE)
                val dateAddedCol = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATE_ADDED)
                val dateTakenCol = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATE_TAKEN)
                val sizeCol = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.SIZE)
                val widthCol = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.WIDTH)
                val heightCol = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.HEIGHT)
                val dataCol = cursor.getColumnIndex(MediaStore.Images.Media.DATA)
                val bucketCol = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    cursor.getColumnIndex(MediaStore.Images.Media.BUCKET_DISPLAY_NAME)
                } else -1
                val relativePathCol = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    cursor.getColumnIndex(MediaStore.Images.Media.RELATIVE_PATH)
                } else -1

                while (cursor.moveToNext()) {
                    val id = cursor.getLong(idCol)
                    val name = cursor.getString(nameCol) ?: "IMG_$id.jpg"
                    val mime = cursor.getString(mimeCol) ?: "image/jpeg"
                    val dateAdded = cursor.getLong(dateAddedCol) * 1000L
                    var dateTaken = cursor.getLong(dateTakenCol)
                    if (dateTaken == 0L) dateTaken = dateAdded
                    val size = cursor.getLong(sizeCol)
                    val width = cursor.getInt(widthCol)
                    val height = cursor.getInt(heightCol)
                    val path = if (dataCol >= 0) cursor.getString(dataCol) ?: "" else ""
                    val bucketName = if (bucketCol >= 0) cursor.getString(bucketCol) ?: "" else ""
                    val relPath = if (relativePathCol >= 0) cursor.getString(relativePathCol) ?: "" else ""

                    val contentUri = ContentUris.withAppendedId(
                        MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                        id
                    )

                    // Robust local folder & tag detection combining path, bucket name, and filename
                    val combinedSearchStr = "$path $bucketName $relPath $name".lowercase()
                    val (initialCategory, initialTags) = when {
                        combinedSearchStr.contains("screenshot") -> 
                            Pair(SceneCategory.SCREENSHOT, listOf("Screenshot", "Screen Capture"))
                        combinedSearchStr.contains("whatsapp") || combinedSearchStr.contains("telegram") || combinedSearchStr.contains("messenger") -> 
                            Pair(SceneCategory.PEOPLE, listOf("Social", "Messaging", "Friends"))
                        combinedSearchStr.contains("selfie") || combinedSearchStr.contains("portrait") || combinedSearchStr.contains("face") -> 
                            Pair(SceneCategory.PEOPLE, listOf("Selfie", "Portrait"))
                        combinedSearchStr.contains("doc") || combinedSearchStr.contains("receipt") || combinedSearchStr.contains("invoice") || combinedSearchStr.contains("bill") || combinedSearchStr.contains("scan") || combinedSearchStr.contains("pdf") -> 
                            Pair(SceneCategory.DOCUMENT, listOf("Document", "Receipt"))
                        combinedSearchStr.contains("car") || combinedSearchStr.contains("bike") || combinedSearchStr.contains("vehicle") || combinedSearchStr.contains("motor") -> 
                            Pair(SceneCategory.CARS, listOf("Vehicle", "Transport"))
                        combinedSearchStr.contains("food") || combinedSearchStr.contains("meal") || combinedSearchStr.contains("dinner") || combinedSearchStr.contains("lunch") || combinedSearchStr.contains("breakfast") || combinedSearchStr.contains("restaurant") -> 
                            Pair(SceneCategory.FOOD, listOf("Food", "Dining"))
                        combinedSearchStr.contains("dog") || combinedSearchStr.contains("cat") || combinedSearchStr.contains("pet") || combinedSearchStr.contains("animal") -> 
                            Pair(SceneCategory.PETS, listOf("Pet", "Animal"))
                        combinedSearchStr.contains("sunset") || combinedSearchStr.contains("beach") || combinedSearchStr.contains("travel") || combinedSearchStr.contains("trip") || combinedSearchStr.contains("hotel") || combinedSearchStr.contains("tour") -> 
                            Pair(SceneCategory.TRAVEL, listOf("Travel", "Vacation"))
                        combinedSearchStr.contains("dcim") || combinedSearchStr.contains("camera") || combinedSearchStr.contains("nature") || combinedSearchStr.contains("mountain") || combinedSearchStr.contains("flower") || combinedSearchStr.contains("garden") -> 
                            Pair(SceneCategory.NATURE, listOf("Camera", "Nature"))
                        else -> 
                            Pair(SceneCategory.NATURE, listOf("Photo", "Camera"))
                    }

                    list.add(
                        MediaEntity(
                            id = id,
                            uriString = contentUri.toString(),
                            path = if (path.isNotBlank()) path else "$relPath$name",
                            displayName = name,
                            mimeType = mime,
                            mediaType = MediaType.IMAGE,
                            dateAdded = dateAdded,
                            dateTaken = dateTaken,
                            sizeBytes = size,
                            width = width,
                            height = height,
                            sceneCategory = initialCategory,
                            aiTags = initialTags,
                            isProcessedByAi = false
                        )
                    )
                }
            }
        }
        return list
    }

    private fun queryVideos(): List<MediaEntity> {
        val list = mutableListOf<MediaEntity>()
        val projection = arrayOf(
            MediaStore.Video.Media._ID,
            MediaStore.Video.Media.DISPLAY_NAME,
            MediaStore.Video.Media.MIME_TYPE,
            MediaStore.Video.Media.DATE_ADDED,
            MediaStore.Video.Media.DATE_TAKEN,
            MediaStore.Video.Media.SIZE,
            MediaStore.Video.Media.WIDTH,
            MediaStore.Video.Media.HEIGHT,
            MediaStore.Video.Media.DURATION,
            MediaStore.Video.Media.DATA
        )

        val sortOrder = "${MediaStore.Video.Media.DATE_TAKEN} DESC"

        runCatching {
            context.contentResolver.query(
                MediaStore.Video.Media.EXTERNAL_CONTENT_URI,
                projection,
                null,
                null,
                sortOrder
            )?.use { cursor ->
                val idCol = cursor.getColumnIndexOrThrow(MediaStore.Video.Media._ID)
                val nameCol = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DISPLAY_NAME)
                val mimeCol = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.MIME_TYPE)
                val dateAddedCol = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DATE_ADDED)
                val dateTakenCol = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DATE_TAKEN)
                val sizeCol = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.SIZE)
                val widthCol = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.WIDTH)
                val heightCol = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.HEIGHT)
                val durationCol = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DURATION)
                val dataCol = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DATA)

                while (cursor.moveToNext()) {
                    val id = cursor.getLong(idCol)
                    val name = cursor.getString(nameCol) ?: "VID_$id.mp4"
                    val mime = cursor.getString(mimeCol) ?: "video/mp4"
                    val dateAdded = cursor.getLong(dateAddedCol) * 1000L
                    var dateTaken = cursor.getLong(dateTakenCol)
                    if (dateTaken == 0L) dateTaken = dateAdded
                    val size = cursor.getLong(sizeCol)
                    val width = cursor.getInt(widthCol)
                    val height = cursor.getInt(heightCol)
                    val duration = cursor.getLong(durationCol)
                    val path = cursor.getString(dataCol) ?: ""

                    val contentUri = ContentUris.withAppendedId(
                        MediaStore.Video.Media.EXTERNAL_CONTENT_URI,
                        id
                    )

                    list.add(
                        MediaEntity(
                            id = id + 1_000_000_000L, // offset video IDs to prevent clash with image IDs
                            uriString = contentUri.toString(),
                            path = path,
                            displayName = name,
                            mimeType = mime,
                            mediaType = MediaType.VIDEO,
                            dateAdded = dateAdded,
                            dateTaken = dateTaken,
                            durationMs = duration,
                            sizeBytes = size,
                            width = width,
                            height = height
                        )
                    )
                }
            }
        }
        return list
    }
}
