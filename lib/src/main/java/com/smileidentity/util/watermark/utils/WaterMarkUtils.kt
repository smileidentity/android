package com.smileidentity.util.watermark.utils

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import androidx.core.graphics.createBitmap
import com.smileidentity.util.randomJobId
import com.smileidentity.util.watermark.models.DetectionResults
import com.smileidentity.util.watermark.models.WatermarkPosition
import com.smileidentity.util.watermark.models.WatermarkText
import java.io.File
import kotlin.math.ceil
import timber.log.Timber

const val ERROR_PIXELS_NOT_ENOUGH = "Not enough pixels"
const val LSB_TEXT_PREFIX_FLAG = "2323"
const val LSB_TEXT_SUFFIX_FLAG = "4545"

internal fun createInvisibleTextMark(backgroundImg: Bitmap, file: File) {
    try {
        val runId = randomJobId()
        val useNativeProcessor = false
        val applyProcessors = true
        val session = BenchMarkUtils.createSession(
            "Liveness Comparison useNativeProcessor $useNativeProcessor applyProcessors" +
                " $applyProcessors run id = $runId",
        )
        session.lap("Watermark Start")
        session.recordFileSize("liveness", file.absolutePath)
        if (applyProcessors) {
            val regionWidth = (backgroundImg.width * 0.2).toInt()
            val regionHeight = (backgroundImg.height * 0.1).toInt()
            val startX = backgroundImg.width - regionWidth
            val startY = backgroundImg.height - regionHeight

            val watermarkText = WatermarkText(
                "Smile Identity",
                WatermarkPosition(startX, startY, regionWidth, regionHeight),
            )
            val result = applyWatermark(
                backgroundImg,
                watermarkText,
                useNativeProcessor,
            )
            session.lap("Watermark applied")
            // Save the watermarked image to file
            file.outputStream().use { stream ->
                val compressSuccess = result.compress(Bitmap.CompressFormat.JPEG, 100, stream)
                session.lap("File created")
                Timber.i("Done creating invisible watermark, compression success: $compressSuccess")
            }
            session.lap("Watermark application complete")
            // Now detect the watermark we just created
            session.lap("Watermark detection start")
            val detectionResult = detectWatermark(result, useNativeProcessor)
            // Get the detected watermark text
            val detectedText = detectionResult.watermarkString
            session.lap("Watermark detection complete result $detectedText")
        }
        session.lap("Watermark indy complete result")
        session.stop(
            "Liveness Comparison useNativeProcessor $useNativeProcessor applyProcessors " +
                "$applyProcessors run id = $runId",
        )
    } catch (e: Exception) {
        Timber.e(e, "Error creating invisible watermark")
    }
}

fun applyWatermark(
    backgroundImg: Bitmap,
    watermarkText: WatermarkText,
    useNativeProcessor: Boolean = true,
): Bitmap {
    try {
        // Create output bitmap
        val outputBitmap =
            createBitmap(backgroundImg.width, backgroundImg.height, backgroundImg.config!!)

        // Copy the input bitmap to the output bitmap first
        val canvas = Canvas(outputBitmap)
        canvas.drawBitmap(backgroundImg, 0f, 0f, null)

        // Define the region to watermark (bottom right corner, 20% of width and 10% of height)
        val regionWidth = watermarkText.position.width
        val regionHeight = watermarkText.position.height
        val startX = watermarkText.position.positionX
        val startY = watermarkText.position.positionY

        // Get only pixels from the region
        val regionPixels = IntArray(regionWidth * regionHeight)
        backgroundImg.getPixels(
            regionPixels,
            0,
            regionWidth,
            startX,
            startY,
            regionWidth,
            regionHeight,
        )

        // Convert the region pixels to ARGB array
        val regionColorArray = BitmapUtils.pixel2ARGBArray(regionPixels)

        // Convert the String into a binary string, and replace the single digit number
        var watermarkBinary =
            if (useNativeProcessor) {
                StringUtils.stringToBinary(watermarkText.text)
            } else {
                StringUtils.nonNativeStringToBinary(
                    watermarkText.text,
                )
            }

        watermarkBinary = LSB_TEXT_PREFIX_FLAG + watermarkBinary + LSB_TEXT_SUFFIX_FLAG

        val watermarkColorArray =
            if (useNativeProcessor) {
                StringUtils.stringToIntArray(watermarkBinary)
            } else {
                StringUtils.nonNativeStringToIntArray(
                    watermarkBinary,
                )
            }
        watermarkColorArray?.let {
            if (watermarkColorArray.size > regionColorArray.size) {
                Timber.i("Watermark return 1 - region too small for watermark")
                throw Exception(ERROR_PIXELS_NOT_ENOUGH)
            }

            // Apply the watermark by replacing LSBs only in the region
            val chunkSize = watermarkColorArray.size
            val numOfChunks = ceil(regionColorArray.size.toDouble() / chunkSize).toInt()

            for (i in 0 until minOf(numOfChunks, 1)) { // Apply only once in the region
                val start = i * chunkSize
                for (j in 0 until chunkSize) {
                    if (start + j < regionColorArray.size) {
                        regionColorArray[start + j] = StringUtils.replaceSingleDigit(
                            regionColorArray[start + j],
                            watermarkColorArray[j],
                        )
                    }
                }
            }

            // Reconstruct the modified region pixel array
            for (i in regionPixels.indices) {
                val color = Color.argb(
                    regionColorArray[4 * i],
                    regionColorArray[4 * i + 1],
                    regionColorArray[4 * i + 2],
                    regionColorArray[4 * i + 3],
                )
                regionPixels[i] = color
            }

            // Set the modified region pixels back to the output bitmap
            outputBitmap.setPixels(
                regionPixels,
                0,
                regionWidth,
                startX,
                startY,
                regionWidth,
                regionHeight,
            )
        }
        return outputBitmap
    } catch (e: Exception) {
        Timber.e(e, "Error applying LSB watermark")
        throw e
    }
}

fun detectWatermark(bitmap: Bitmap, useNativeProcessor: Boolean = true): DetectionResults {
    try {
        // Define the same region used for watermarking
        val regionWidth = (bitmap.width * 0.2).toInt()
        val regionHeight = (bitmap.height * 0.1).toInt()
        val startX = bitmap.width - regionWidth
        val startY = bitmap.height - regionHeight

        // Get only pixels from the region
        val regionPixels = IntArray(regionWidth * regionHeight)
        bitmap.getPixels(
            regionPixels,
            0,
            regionWidth,
            startX,
            startY,
            regionWidth,
            regionHeight,
        )

        // Extract the LSB values from the region
        val regionColorArray = BitmapUtils.pixel2ARGBArray(regionPixels)
        val extractedBinary = StringBuilder()

        for (i in 0 until regionColorArray.size) {
            extractedBinary.append(regionColorArray[i] % 10)
        }

        val binaryString = extractedBinary.toString()

        // Look for the text prefix and suffix markers
        val textStartIndex = binaryString.indexOf(LSB_TEXT_PREFIX_FLAG)
        val textEndIndex = binaryString.indexOf(
            LSB_TEXT_SUFFIX_FLAG,
            textStartIndex + LSB_TEXT_PREFIX_FLAG.length,
        )
        var extractedText: String? = null

        if (textStartIndex >= 0 && textEndIndex > textStartIndex) {
            // Extract the text portion
            val textBinary = binaryString.substring(
                textStartIndex + LSB_TEXT_PREFIX_FLAG.length,
                textEndIndex,
            )

            // Convert binary back to text
            extractedText =
                if (useNativeProcessor) {
                    StringUtils.binaryToString(textBinary)
                } else {
                    StringUtils.nonNativeBinaryToString(
                        textBinary,
                    )
                }
        }
        return DetectionResults(extractedText)
    } catch (e: Exception) {
        Timber.e(e, "Error detecting watermark")
        throw e
    }
}
