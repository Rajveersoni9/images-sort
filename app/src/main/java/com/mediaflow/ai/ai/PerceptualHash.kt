package com.mediaflow.ai.ai

import android.graphics.Bitmap

object PerceptualHash {
    /**
     * Calculates 64-bit difference hash (dHash) for an image bitmap.
     * Resistant to compression, subtle noise, and slight color shift.
     */
    fun calculateDHash(bitmap: Bitmap): Long {
        // Resize to 9x8 grayscale
        val resized = Bitmap.createScaledBitmap(bitmap, 9, 8, true)
        var hash: Long = 0L

        for (y in 0 until 8) {
            for (x in 0 until 8) {
                val leftPixel = resized.getPixel(x, y)
                val rightPixel = resized.getPixel(x + 1, y)

                val leftLuma = (leftPixel and 0xFF) + ((leftPixel shr 8) and 0xFF) + ((leftPixel shr 16) and 0xFF)
                val rightLuma = (rightPixel and 0xFF) + ((rightPixel shr 8) and 0xFF) + ((rightPixel shr 16) and 0xFF)

                if (leftLuma > rightLuma) {
                    hash = hash or (1L shl (y * 8 + x))
                }
            }
        }
        return hash
    }

    /**
     * Calculates Hamming Distance between two hashes.
     * Distance <= 5 typically indicates identical or near-duplicate photos.
     */
    fun hammingDistance(hash1: Long, hash2: Long): Int {
        var xor = hash1 xor hash2
        var distance = 0
        while (xor != 0L) {
            distance += (xor and 1L).toInt()
            xor = xor ushr 1
        }
        return distance
    }
}
