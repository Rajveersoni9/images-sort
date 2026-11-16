package com.mediaflow.ai.ai

import android.content.Context
import android.graphics.Bitmap
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.ByteArrayOutputStream
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PythonBridge @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private var isPythonAvailable: Boolean = false

    init {
        // Attempt to detect if Chaquopy Python runtime is initialized
        isPythonAvailable = runCatching {
            val pyClass = Class.forName("com.chaquo.python.Python")
            val isStartedMethod = pyClass.getMethod("isStarted")
            isStartedMethod.invoke(null) as Boolean
        }.getOrDefault(false)
    }

    fun isAvailable(): Boolean = isPythonAvailable

    fun calculateDHashWithPython(bitmap: Bitmap): Long? {
        if (!isPythonAvailable) return null

        return runCatching {
            val stream = ByteArrayOutputStream()
            bitmap.compress(Bitmap.CompressFormat.JPEG, 85, stream)
            val bytes = stream.toByteArray()

            val pyClass = Class.forName("com.chaquo.python.Python")
            val getInstanceMethod = pyClass.getMethod("getInstance")
            val pyInstance = getInstanceMethod.invoke(null)

            val getModuleMethod = pyClass.getMethod("getModule", String::class.java)
            val module = getModuleMethod.invoke(pyInstance, "mediaflow_engine")

            val callAttrMethod = module.javaClass.getMethod("callAttr", String::class.java, Array<Any>::class.java)
            val result = callAttrMethod.invoke(module, "calculate_dhash", arrayOf(bytes))
            (result as Number).toLong()
        }.getOrNull()
    }

    fun extractDominantColorWithPython(bitmap: Bitmap): String? {
        if (!isPythonAvailable) return null

        return runCatching {
            val stream = ByteArrayOutputStream()
            bitmap.compress(Bitmap.CompressFormat.JPEG, 80, stream)
            val bytes = stream.toByteArray()

            val pyClass = Class.forName("com.chaquo.python.Python")
            val getInstanceMethod = pyClass.getMethod("getInstance")
            val pyInstance = getInstanceMethod.invoke(null)

            val getModuleMethod = pyClass.getMethod("getModule", String::class.java)
            val module = getModuleMethod.invoke(pyInstance, "mediaflow_engine")

            val callAttrMethod = module.javaClass.getMethod("callAttr", String::class.java, Array<Any>::class.java)
            val result = callAttrMethod.invoke(module, "extract_dominant_color_hex", arrayOf(bytes))
            result.toString()
        }.getOrNull()
    }
}
