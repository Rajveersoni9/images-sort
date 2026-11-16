package com.mediaflow.ai.ai

import android.graphics.Bitmap

object ColorPaletteExtractor {
    fun extractDominantColorHex(bitmap: Bitmap): String {
        return runCatching {
            val small = Bitmap.createScaledBitmap(bitmap, 16, 16, false)
            var totalR = 0L
            var totalG = 0L
            var totalB = 0L
            val count = 16 * 16

            for (x in 0 until 16) {
                for (y in 0 until 16) {
                    val pixel = small.getPixel(x, y)
                    totalR += (pixel shr 16) and 0xFF
                    totalG += (pixel shr 8) and 0xFF
                    totalB += pixel and 0xFF
                }
            }

            val avgR = (totalR / count).toInt()
            val avgG = (totalG / count).toInt()
            val avgB = (totalB / count).toInt()

            String.format("#%02X%02X%02X", avgR, avgG, avgB)
        }.getOrDefault("#6750A4")
    }
}
