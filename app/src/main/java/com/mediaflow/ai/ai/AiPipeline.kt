package com.mediaflow.ai.ai

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import com.mediaflow.ai.data.local.entity.MediaEntity
import com.mediaflow.ai.domain.model.MediaType
import com.mediaflow.ai.domain.model.SceneCategory
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AiPipeline @Inject constructor(
    @ApplicationContext private val context: Context,
    private val classifierEngine: ImageClassifierEngine,
    private val ocrEngine: OcrEngine,
    private val faceDetectionEngine: FaceDetectionEngine
) {
    suspend fun processMedia(entity: MediaEntity): MediaEntity = withContext(Dispatchers.IO) {
        val uri = Uri.parse(entity.uriString)
        val bitmap = decodeSampledBitmapFromUri(uri, 512, 512) ?: return@withContext entity.copy(isProcessedByAi = true)

        try {
            // 1. Perceptual dHash calculation
            val hash = PerceptualHash.calculateDHash(bitmap)

            // 2. Dominant color extraction
            val dominantColor = ColorPaletteExtractor.extractDominantColorHex(bitmap)

            // 3. Object & Scene Classification
            val (tags, inferredScene) = classifierEngine.classifyImage(bitmap)

            // 4. Face Detection
            val facesCount = faceDetectionEngine.countFaces(bitmap)

            // 5. OCR (Text recognition) - prioritize for documents/screenshots or when text is detected
            val ocrText = if (inferredScene == SceneCategory.DOCUMENT || inferredScene == SceneCategory.SCREENSHOT || tags.any { it.contains("text", ignoreCase = true) }) {
                ocrEngine.extractText(bitmap)
            } else null

            // Determine final category (e.g. if faces >= 1, and no specific scene, mark as PEOPLE)
            val finalScene = if (facesCount > 0 && inferredScene == SceneCategory.GENERAL) {
                SceneCategory.PEOPLE
            } else {
                inferredScene
            }

            val finalTags = buildList {
                addAll(tags)
                if (facesCount > 0) add("Faces ($facesCount)")
                if (!ocrText.isNullOrBlank()) add("Has Text")
                if (entity.mediaType == MediaType.VIDEO) add("Video")
            }.distinct()

            entity.copy(
                aiTags = finalTags,
                sceneCategory = finalScene,
                detectedFacesCount = facesCount,
                ocrText = ocrText,
                dominantColorHex = dominantColor,
                perceptualHash = hash,
                isProcessedByAi = true
            )
        } catch (e: Exception) {
            entity.copy(isProcessedByAi = true)
        } finally {
            bitmap.recycle()
        }
    }

    private fun decodeSampledBitmapFromUri(uri: Uri, reqWidth: Int, reqHeight: Int): Bitmap? {
        return runCatching {
            context.contentResolver.openInputStream(uri)?.use { stream ->
                val options = BitmapFactory.Options().apply {
                    inJustDecodeBounds = true
                }
                BitmapFactory.decodeStream(stream, null, options)

                // Calculate inSampleSize
                var inSampleSize = 1
                if (options.outHeight > reqHeight || options.outWidth > reqWidth) {
                    val halfHeight = options.outHeight / 2
                    val halfWidth = options.outWidth / 2
                    while ((halfHeight / inSampleSize) >= reqHeight && (halfWidth / inSampleSize) >= reqWidth) {
                        inSampleSize *= 2
                    }
                }

                // Decode with inSampleSize set
                context.contentResolver.openInputStream(uri)?.use { secondStream ->
                    val decodeOptions = BitmapFactory.Options().apply {
                        this.inSampleSize = inSampleSize
                        inPreferredConfig = Bitmap.Config.RGB_565
                    }
                    BitmapFactory.decodeStream(secondStream, null, decodeOptions)
                }
            }
        }.getOrNull()
    }
}
