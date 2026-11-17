package com.mediaflow.ai.ai

import android.content.Context
import android.graphics.Bitmap
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.label.ImageLabeling
import com.google.mlkit.vision.label.defaults.ImageLabelerOptions
import com.mediaflow.ai.domain.model.SceneCategory
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ImageClassifierEngine @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val labeler by lazy {
        ImageLabeling.getClient(
            ImageLabelerOptions.Builder()
                .setConfidenceThreshold(0.55f)
                .build()
        )
    }

    suspend fun classifyImage(bitmap: Bitmap): Pair<List<String>, SceneCategory> {
        return runCatching {
            val image = InputImage.fromBitmap(bitmap, 0)
            val labels = labeler.process(image).await()
            val tagList = labels.map { it.text }

            val scene = inferSceneCategory(tagList)
            Pair(tagList, scene)
        }.getOrElse {
            Pair(emptyList(), SceneCategory.GENERAL)
        }
    }

    private fun inferSceneCategory(tags: List<String>): SceneCategory {
        val lowerTags = tags.map { it.lowercase() }
        return when {
            lowerTags.any { it in listOf("dog", "cat", "pet", "puppy", "kitten") } -> SceneCategory.PETS
            lowerTags.any { it in listOf("animal", "wildlife", "bird", "reptile") } -> SceneCategory.PETS
            lowerTags.any { it in listOf("car", "vehicle", "automobile", "sports car", "motorcycle") } -> SceneCategory.CARS
            lowerTags.any { it in listOf("food", "meal", "dish", "cuisine", "dessert", "coffee") } -> SceneCategory.FOOD
            lowerTags.any { it in listOf("mountain", "tree", "forest", "nature", "sky", "flower", "lake", "ocean") } -> SceneCategory.NATURE
            lowerTags.any { it in listOf("beach", "travel", "resort", "hotel", "vacation", "monument") } -> SceneCategory.TRAVEL
            lowerTags.any { it in listOf("document", "paper", "text", "receipt", "letter") } -> SceneCategory.DOCUMENT
            lowerTags.any { it in listOf("screenshot", "software", "multimedia") } -> SceneCategory.SCREENSHOT
            lowerTags.any { it in listOf("person", "human", "face", "smile", "crowd") } -> SceneCategory.PEOPLE
            lowerTags.any { it in listOf("night", "stars", "astronomy", "darkness") } -> SceneCategory.NIGHT_SKY
            else -> SceneCategory.GENERAL
        }
    }
}
