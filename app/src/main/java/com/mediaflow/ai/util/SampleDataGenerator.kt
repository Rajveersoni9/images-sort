package com.mediaflow.ai.util

import android.net.Uri
import com.mediaflow.ai.data.local.entity.ActivityTaskEntity
import com.mediaflow.ai.data.local.entity.AlbumEntity
import com.mediaflow.ai.data.local.entity.MediaEntity
import com.mediaflow.ai.domain.model.MediaType
import com.mediaflow.ai.domain.model.SceneCategory
import com.mediaflow.ai.domain.model.SmartAlbumType
import com.mediaflow.ai.domain.model.TaskStatus
import com.mediaflow.ai.domain.model.TaskType

object SampleDataGenerator {

    fun generateSampleMedia(): List<MediaEntity> {
        val now = System.currentTimeMillis()
        val day = 86_400_000L

        return listOf(
            MediaEntity(
                id = 101L,
                uriString = "https://images.unsplash.com/photo-1507525428034-b723cf961d3e?auto=format&fit=crop&w=1200&q=80",
                path = "/storage/emulated/0/DCIM/Camera/IMG_20261116_101.jpg",
                displayName = "IMG_Tropical_Sunset.jpg",
                mimeType = "image/jpeg",
                mediaType = MediaType.IMAGE,
                dateAdded = now - (day * 0),
                dateTaken = now - (day * 0),
                sizeBytes = 4_250_000L,
                width = 3840,
                height = 2160,
                isFavorite = true,
                aiTags = listOf("Sunset", "Beach", "Ocean", "Golden Hour", "Tropical"),
                detectedFacesCount = 0,
                sceneCategory = SceneCategory.TRAVEL,
                dominantColorHex = "#F59E0B",
                isProcessedByAi = true
            ),
            MediaEntity(
                id = 102L,
                uriString = "https://images.unsplash.com/photo-1543466835-00a7907e9de1?auto=format&fit=crop&w=1200&q=80",
                path = "/storage/emulated/0/DCIM/Camera/IMG_20261116_102.jpg",
                displayName = "IMG_Golden_Retriever.jpg",
                mimeType = "image/jpeg",
                mediaType = MediaType.IMAGE,
                dateAdded = now - (day * 0),
                dateTaken = now - (day * 0),
                sizeBytes = 3_120_000L,
                width = 4032,
                height = 3024,
                isFavorite = true,
                aiTags = listOf("Dog", "Golden Retriever", "Pet", "Puppy", "Cute"),
                detectedFacesCount = 0,
                sceneCategory = SceneCategory.PETS,
                dominantColorHex = "#D97706",
                isProcessedByAi = true
            ),
            MediaEntity(
                id = 103L,
                uriString = "https://images.unsplash.com/photo-1503376780353-7e6692767b70?auto=format&fit=crop&w=1200&q=80",
                path = "/storage/emulated/0/DCIM/Camera/IMG_20261115_103.jpg",
                displayName = "IMG_Porsche_911.jpg",
                mimeType = "image/jpeg",
                mediaType = MediaType.IMAGE,
                dateAdded = now - (day * 1),
                dateTaken = now - (day * 1),
                sizeBytes = 5_800_000L,
                width = 3840,
                height = 2560,
                isFavorite = false,
                aiTags = listOf("Red Car", "Porsche", "Supercar", "Automobile", "Speed"),
                detectedFacesCount = 0,
                sceneCategory = SceneCategory.CARS,
                dominantColorHex = "#DC2626",
                isProcessedByAi = true
            ),
            MediaEntity(
                id = 104L,
                uriString = "https://images.unsplash.com/photo-1534528741775-53994a69daeb?auto=format&fit=crop&w=1200&q=80",
                path = "/storage/emulated/0/DCIM/Camera/IMG_20261115_104.jpg",
                displayName = "IMG_Portrait_Sarah.jpg",
                mimeType = "image/jpeg",
                mediaType = MediaType.IMAGE,
                dateAdded = now - (day * 1),
                dateTaken = now - (day * 1),
                sizeBytes = 6_100_000L,
                width = 3000,
                height = 4000,
                isFavorite = true,
                aiTags = listOf("Portrait", "Smile", "Person", "Girl", "Blue Shirt"),
                detectedFacesCount = 1,
                sceneCategory = SceneCategory.PEOPLE,
                dominantColorHex = "#8B5CF6",
                isProcessedByAi = true
            ),
            MediaEntity(
                id = 105L,
                uriString = "https://images.unsplash.com/photo-1464822759023-fed622ff2c3b?auto=format&fit=crop&w=1200&q=80",
                path = "/storage/emulated/0/DCIM/Camera/IMG_20261114_105.jpg",
                displayName = "IMG_Alpine_Peaks.jpg",
                mimeType = "image/jpeg",
                mediaType = MediaType.IMAGE,
                dateAdded = now - (day * 2),
                dateTaken = now - (day * 2),
                sizeBytes = 7_400_000L,
                width = 4000,
                height = 2250,
                isFavorite = false,
                aiTags = listOf("Mountain", "Snow", "Peak", "Sky", "Alps", "Nature"),
                detectedFacesCount = 0,
                sceneCategory = SceneCategory.NATURE,
                dominantColorHex = "#3B82F6",
                isProcessedByAi = true
            ),
            MediaEntity(
                id = 106L,
                uriString = "https://images.unsplash.com/photo-1555396273-367ea4eb4db5?auto=format&fit=crop&w=1200&q=80",
                path = "/storage/emulated/0/DCIM/Camera/IMG_20261114_106.jpg",
                displayName = "IMG_Gourmet_Burger.jpg",
                mimeType = "image/jpeg",
                mediaType = MediaType.IMAGE,
                dateAdded = now - (day * 2),
                dateTaken = now - (day * 2),
                sizeBytes = 3_900_000L,
                width = 3024,
                height = 3024,
                isFavorite = false,
                aiTags = listOf("Food", "Burger", "Cuisine", "Delicious", "Dinner"),
                detectedFacesCount = 0,
                sceneCategory = SceneCategory.FOOD,
                dominantColorHex = "#EA580C",
                isProcessedByAi = true
            ),
            MediaEntity(
                id = 107L,
                uriString = "https://images.unsplash.com/photo-1519681393784-d120267933ba?auto=format&fit=crop&w=1200&q=80",
                path = "/storage/emulated/0/DCIM/Camera/IMG_20261113_107.jpg",
                displayName = "IMG_Milky_Way_Night.jpg",
                mimeType = "image/jpeg",
                mediaType = MediaType.IMAGE,
                dateAdded = now - (day * 3),
                dateTaken = now - (day * 3),
                sizeBytes = 8_200_000L,
                width = 4032,
                height = 3024,
                isFavorite = true,
                aiTags = listOf("Night Sky", "Stars", "Milky Way", "Astronomy", "Galaxy"),
                detectedFacesCount = 0,
                sceneCategory = SceneCategory.NIGHT_SKY,
                dominantColorHex = "#1E1B4B",
                isProcessedByAi = true
            ),
            MediaEntity(
                id = 108L,
                uriString = "https://images.unsplash.com/photo-1568602471122-7832951cc4c5?auto=format&fit=crop&w=1200&q=80",
                path = "/storage/emulated/0/DCIM/Camera/IMG_20261113_108.jpg",
                displayName = "IMG_Group_Selfie.jpg",
                mimeType = "image/jpeg",
                mediaType = MediaType.IMAGE,
                dateAdded = now - (day * 3),
                dateTaken = now - (day * 3),
                sizeBytes = 4_800_000L,
                width = 3264,
                height = 2448,
                isFavorite = false,
                aiTags = listOf("Selfie", "Friends", "Smile", "People", "Party"),
                detectedFacesCount = 3,
                sceneCategory = SceneCategory.PEOPLE,
                dominantColorHex = "#4F46E5",
                isProcessedByAi = true
            ),
            MediaEntity(
                id = 109L,
                uriString = "https://images.unsplash.com/photo-1589829545856-d10d557cf95f?auto=format&fit=crop&w=1200&q=80",
                path = "/storage/emulated/0/Download/DOC_Agreement_2026.jpg",
                displayName = "DOC_Passport_Scan.jpg",
                mimeType = "image/jpeg",
                mediaType = MediaType.IMAGE,
                dateAdded = now - (day * 4),
                dateTaken = now - (day * 4),
                sizeBytes = 2_100_000L,
                width = 2480,
                height = 3508,
                isFavorite = false,
                aiTags = listOf("Document", "Passport", "Contract", "Text", "Official"),
                detectedFacesCount = 1,
                ocrText = "PASSPORT / PASSEPORT\nUNITED STATES OF AMERICA\nSurname: DOE\nGiven Names: JANE\nDate of Expiry: 12 NOV 2034",
                sceneCategory = SceneCategory.DOCUMENT,
                dominantColorHex = "#64748B",
                isProcessedByAi = true
            ),
            MediaEntity(
                id = 110L,
                uriString = "https://images.unsplash.com/photo-1518770660439-4636190af475?auto=format&fit=crop&w=1200&q=80",
                path = "/storage/emulated/0/Pictures/Screenshots/Screenshot_20261112.png",
                displayName = "Screenshot_Code_Snippet.png",
                mimeType = "image/png",
                mediaType = MediaType.IMAGE,
                dateAdded = now - (day * 5),
                dateTaken = now - (day * 5),
                sizeBytes = 1_400_000L,
                width = 1080,
                height = 2400,
                isFavorite = false,
                aiTags = listOf("Screenshot", "Technology", "Code", "Android"),
                ocrText = "@Composable\nfun MediaFlowApp() {\n    // Ultra fast AI indexing\n}",
                sceneCategory = SceneCategory.SCREENSHOT,
                dominantColorHex = "#0F172A",
                isProcessedByAi = true
            ),
            MediaEntity(
                id = 201L,
                uriString = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ForBiggerBlazes.mp4",
                path = "/storage/emulated/0/DCIM/Camera/VID_Drone_Coastline.mp4",
                displayName = "VID_Drone_Coastline.mp4",
                mimeType = "video/mp4",
                mediaType = MediaType.VIDEO,
                dateAdded = now - (day * 1),
                dateTaken = now - (day * 1),
                durationMs = 15_000L,
                sizeBytes = 28_400_000L,
                width = 1920,
                height = 1080,
                isFavorite = true,
                aiTags = listOf("Video", "Drone", "Coastline", "Ocean", "4K"),
                sceneCategory = SceneCategory.NATURE,
                dominantColorHex = "#0284C7",
                isProcessedByAi = true
            )
        )
    }

    fun generateSampleAlbums(): List<AlbumEntity> {
        return listOf(
            AlbumEntity(
                id = "smart_people",
                title = "People & Pets",
                type = SmartAlbumType.PEOPLE,
                coverUriString = "https://images.unsplash.com/photo-1534528741775-53994a69daeb?auto=format&fit=crop&w=600&q=80",
                itemCount = 142,
                description = "Faces, portraits, friends, and family",
                gradientStartHex = "#7C3AED",
                gradientEndHex = "#C084FC"
            ),
            AlbumEntity(
                id = "smart_travel",
                title = "Travel & Places",
                type = SmartAlbumType.TRAVEL,
                coverUriString = "https://images.unsplash.com/photo-1507525428034-b723cf961d3e?auto=format&fit=crop&w=600&q=80",
                itemCount = 89,
                description = "Beaches, resorts, vacations & landmarks",
                gradientStartHex = "#2563EB",
                gradientEndHex = "#60A5FA"
            ),
            AlbumEntity(
                id = "smart_nature",
                title = "Nature & Mountains",
                type = SmartAlbumType.NATURE,
                coverUriString = "https://images.unsplash.com/photo-1464822759023-fed622ff2c3b?auto=format&fit=crop&w=600&q=80",
                itemCount = 215,
                description = "Forests, peaks, lakes, and landscapes",
                gradientStartHex = "#059669",
                gradientEndHex = "#34D399"
            ),
            AlbumEntity(
                id = "smart_cars",
                title = "Cars & Vehicles",
                type = SmartAlbumType.CARS,
                coverUriString = "https://images.unsplash.com/photo-1503376780353-7e6692767b70?auto=format&fit=crop&w=600&q=80",
                itemCount = 48,
                description = "Supercars, bikes, and automobiles",
                gradientStartHex = "#DC2626",
                gradientEndHex = "#F87171"
            ),
            AlbumEntity(
                id = "smart_food",
                title = "Food & Dining",
                type = SmartAlbumType.FOOD,
                coverUriString = "https://images.unsplash.com/photo-1555396273-367ea4eb4db5?auto=format&fit=crop&w=600&q=80",
                itemCount = 63,
                description = "Dishes, restaurants, and desserts",
                gradientStartHex = "#D97706",
                gradientEndHex = "#FBBF24"
            ),
            AlbumEntity(
                id = "smart_documents",
                title = "Documents & Receipts",
                type = SmartAlbumType.DOCUMENTS,
                coverUriString = "https://images.unsplash.com/photo-1589829545856-d10d557cf95f?auto=format&fit=crop&w=600&q=80",
                itemCount = 37,
                description = "OCR indexed paper, passports, and bills",
                gradientStartHex = "#475569",
                gradientEndHex = "#94A3B8"
            ),
            AlbumEntity(
                id = "smart_screenshots",
                title = "Screenshots",
                type = SmartAlbumType.SCREENSHOTS,
                coverUriString = "https://images.unsplash.com/photo-1518770660439-4636190af475?auto=format&fit=crop&w=600&q=80",
                itemCount = 184,
                description = "App screens, chats, and captures",
                gradientStartHex = "#4F46E5",
                gradientEndHex = "#818CF8"
            ),
            AlbumEntity(
                id = "smart_videos",
                title = "Videos & Clips",
                type = SmartAlbumType.VIDEOS,
                coverUriString = "https://images.unsplash.com/photo-1507525428034-b723cf961d3e?auto=format&fit=crop&w=600&q=80",
                itemCount = 52,
                description = "High framerate 4K and HD recordings",
                gradientStartHex = "#9333EA",
                gradientEndHex = "#C084FC"
            )
        )
    }

    fun generateSampleTasks(): List<ActivityTaskEntity> {
        val now = System.currentTimeMillis()
        val min = 60_000L

        return listOf(
            ActivityTaskEntity(
                id = "task_1",
                title = "12 photos organized",
                subtitle = "Categorized into Nature & Sunset",
                taskType = TaskType.REALTIME_SORT,
                status = TaskStatus.COMPLETED,
                progress = 1.0f,
                itemsAffected = 12,
                timestamp = now - (min * 5)
            ),
            ActivityTaskEntity(
                id = "task_2",
                title = "Duplicate removed",
                subtitle = "Saved 14.2 MB of storage",
                taskType = TaskType.DUPLICATE_SCAN,
                status = TaskStatus.COMPLETED,
                progress = 1.0f,
                itemsAffected = 2,
                timestamp = now - (min * 22)
            ),
            ActivityTaskEntity(
                id = "task_3",
                title = "Screenshot detected",
                subtitle = "Auto-routed to Screenshots album",
                taskType = TaskType.SCENE_CLASSIFICATION,
                status = TaskStatus.COMPLETED,
                progress = 1.0f,
                itemsAffected = 1,
                timestamp = now - (min * 45)
            ),
            ActivityTaskEntity(
                id = "task_4",
                title = "OCR completed",
                subtitle = "Indexed Passport and Receipt text",
                taskType = TaskType.OCR_EXTRACTION,
                status = TaskStatus.COMPLETED,
                progress = 1.0f,
                itemsAffected = 3,
                timestamp = now - (min * 120)
            ),
            ActivityTaskEntity(
                id = "task_5",
                title = "Face grouping finished",
                subtitle = "Clustered 4 distinct individuals",
                taskType = TaskType.FACE_GROUPING,
                status = TaskStatus.COMPLETED,
                progress = 1.0f,
                itemsAffected = 28,
                timestamp = now - (min * 300)
            )
        )
    }
}
