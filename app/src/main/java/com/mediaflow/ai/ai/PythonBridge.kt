package com.mediaflow.ai.ai

import android.content.Context
import android.graphics.Bitmap
import dagger.hilt.android.qualifiers.ApplicationContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.ByteArrayOutputStream
import javax.inject.Inject
import javax.inject.Singleton

data class PythonAnalysisPayload(
    val perceptualHash: Long,
    val dominantColorHex: String,
    val sceneCategory: String,
    val aiTags: List<String>
)

data class DuplicateClusterResult(
    val duplicateGroups: List<Pair<String, List<Long>>>,
    val totalDuplicatesFound: Int,
    val totalRecoverableBytes: Long
)

@Singleton
class PythonBridge @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private var isPythonAvailable: Boolean = false

    init {
        // Detect if Chaquopy Python runtime is initialized
        isPythonAvailable = runCatching {
            val pyClass = Class.forName("com.chaquo.python.Python")
            val isStartedMethod = pyClass.getMethod("isStarted")
            isStartedMethod.invoke(null) as Boolean
        }.getOrDefault(false)
    }

    fun isAvailable(): Boolean = isPythonAvailable

    /**
     * Executes the comprehensive Python AI media analysis pipeline.
     */
    fun analyzeMediaFull(
        bitmap: Bitmap,
        filename: String,
        rawTags: List<String>,
        ocrText: String?,
        faceCount: Int
    ): PythonAnalysisPayload? {
        if (!isPythonAvailable) return null

        return runCatching {
            val stream = ByteArrayOutputStream()
            bitmap.compress(Bitmap.CompressFormat.JPEG, 85, stream)
            val bytes = stream.toByteArray()

            val rawTagsJson = JSONArray(rawTags).toString()

            val pyClass = Class.forName("com.chaquo.python.Python")
            val getInstanceMethod = pyClass.getMethod("getInstance")
            val pyInstance = getInstanceMethod.invoke(null)

            val getModuleMethod = pyClass.getMethod("getModule", String::class.java)
            val module = getModuleMethod.invoke(pyInstance, "mediaflow_engine")

            val callAttrMethod = module.javaClass.getMethod("callAttr", String::class.java, Array<Any>::class.java)
            val jsonResultStr = callAttrMethod.invoke(
                module,
                "analyze_media_full",
                arrayOf(bytes, filename, rawTagsJson, ocrText as Any?, faceCount as Any)
            ).toString()

            val jsonObj = JSONObject(jsonResultStr)
            val hash = jsonObj.optLong("perceptualHash", 0L)
            val color = jsonObj.optString("dominantColorHex", "#6750A4")
            val category = jsonObj.optString("sceneCategory", "GENERAL")
            val tagsArray = jsonObj.optJSONArray("aiTags")
            val tags = mutableListOf<String>()
            if (tagsArray != null) {
                for (i in 0 until tagsArray.length()) {
                    tags.add(tagsArray.getString(i))
                }
            }

            PythonAnalysisPayload(
                perceptualHash = hash,
                dominantColorHex = color,
                sceneCategory = category,
                aiTags = tags
            )
        }.getOrNull()
    }

    /**
     * Calls Python disjoint-set algorithm to cluster duplicates and compute byte savings.
     */
    fun clusterDuplicates(hashesJson: String): DuplicateClusterResult? {
        if (!isPythonAvailable) return null

        return runCatching {
            val pyClass = Class.forName("com.chaquo.python.Python")
            val getInstanceMethod = pyClass.getMethod("getInstance")
            val pyInstance = getInstanceMethod.invoke(null)

            val getModuleMethod = pyClass.getMethod("getModule", String::class.java)
            val module = getModuleMethod.invoke(pyInstance, "mediaflow_engine")

            val callAttrMethod = module.javaClass.getMethod("callAttr", String::class.java, Array<Any>::class.java)
            val jsonResultStr = callAttrMethod.invoke(
                module,
                "cluster_duplicates_and_savings",
                arrayOf(hashesJson)
            ).toString()

            val jsonObj = JSONObject(jsonResultStr)
            val totalFound = jsonObj.optInt("totalDuplicatesFound", 0)
            val totalBytes = jsonObj.optLong("totalRecoverableBytes", 0L)
            val groupsArray = jsonObj.optJSONArray("duplicateGroups")

            val groupsList = mutableListOf<Pair<String, List<Long>>>()
            if (groupsArray != null) {
                for (i in 0 until groupsArray.length()) {
                    val groupObj = groupsArray.getJSONObject(i)
                    val gId = groupObj.getString("groupId")
                    val itemsArr = groupObj.getJSONArray("itemIds")
                    val idList = mutableListOf<Long>()
                    for (j in 0 until itemsArr.length()) {
                        idList.add(itemsArr.getLong(j))
                    }
                    groupsList.add(Pair(gId, idList))
                }
            }

            DuplicateClusterResult(
                duplicateGroups = groupsList,
                totalDuplicatesFound = totalFound,
                totalRecoverableBytes = totalBytes
            )
        }.getOrNull()
    }

    /**
     * Calls Python natural language tokenization and ranking for semantic search.
     */
    fun rankSearchResults(query: String, itemsJson: String): List<Long>? {
        if (!isPythonAvailable) return null

        return runCatching {
            val pyClass = Class.forName("com.chaquo.python.Python")
            val getInstanceMethod = pyClass.getMethod("getInstance")
            val pyInstance = getInstanceMethod.invoke(null)

            val getModuleMethod = pyClass.getMethod("getModule", String::class.java)
            val module = getModuleMethod.invoke(pyInstance, "mediaflow_engine")

            val callAttrMethod = module.javaClass.getMethod("callAttr", String::class.java, Array<Any>::class.java)
            val jsonResultStr = callAttrMethod.invoke(
                module,
                "semantic_search_rank_python",
                arrayOf(query, itemsJson)
            ).toString()

            val jsonArray = JSONArray(jsonResultStr)
            val rankedIds = mutableListOf<Long>()
            for (i in 0 until jsonArray.length()) {
                rankedIds.add(jsonArray.getLong(i))
            }
            rankedIds
        }.getOrNull()
    }

    fun calculateDHashWithPython(bitmap: Bitmap): Long? {
        return analyzeMediaFull(bitmap, "", emptyList(), null, 0)?.perceptualHash
    }

    fun extractDominantColorWithPython(bitmap: Bitmap): String? {
        return analyzeMediaFull(bitmap, "", emptyList(), null, 0)?.dominantColorHex
    }
}
