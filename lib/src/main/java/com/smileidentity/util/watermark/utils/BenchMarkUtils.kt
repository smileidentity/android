package com.smileidentity.util.watermark.utils

import java.io.File
import timber.log.Timber

/**
 * Utility class for benchmarking execution time and measuring file sizes.
 */
object BenchMarkUtils {
    /**
     * Measures the size of a file.
     *
     * @param filePath The path to the file
     * @return The size of the file in bytes, or -1 if the file doesn't exist
     */
    fun measureFileSize(filePath: String): Long {
        val file = File(filePath)
        if (!file.exists()) {
            Timber.e("File does not exist: $filePath")
            return -1
        }

        val size = file.length()
        Timber.d("File size ($filePath): ${formatSize(size)}")
        return size
    }

    /**
     * Formats a byte size into a human-readable string (KB, MB, etc.)
     *
     * @param bytes The size in bytes
     * @return A formatted string representation of the size
     */
    fun formatSize(bytes: Long): String {
        return when {
            bytes < 1024 -> "$bytes bytes"
            bytes < 1024 * 1024 -> String.format("%.2f KB", bytes / 1024.0)
            bytes < 1024 * 1024 * 1024 -> String.format("%.2f MB", bytes / (1024.0 * 1024.0))
            else -> String.format("%.2f GB", bytes / (1024.0 * 1024.0 * 1024.0))
        }
    }

    /**
     * Creates a comprehensive benchmark session that can track both time and size metrics.
     *
     * @param operationName The name of the operation being benchmarked
     * @return A BenchmarkSession instance
     */
    fun createSession(operationName: String): BenchmarkSession {
        return BenchmarkSession(operationName)
    }

    /**
     * A class for tracking comprehensive benchmarks including time and size metrics.
     */
    class BenchmarkSession(private val operationName: String) {
        private val startTime = System.currentTimeMillis()
        private var lastLapTime = startTime
        private var lapCount = 0
        private val metrics = mutableMapOf<String, String>()

        /**
         * Logs the time elapsed since the last lap (or start if no laps yet).
         *
         * @param lapName Optional name for this timing point
         * @return This BenchmarkSession instance for chaining
         */
        fun lap(lapName: String = "Step ${++lapCount}"): BenchmarkSession {
            val currentTime = System.currentTimeMillis()
            val lapTime = currentTime - lastLapTime
            val totalTime = currentTime - startTime

            Timber.d("$operationName - $lapName: $lapTime ms (Total: $totalTime ms)")
            metrics["$lapName Time"] = "$lapTime ms"
            lastLapTime = currentTime
            return this
        }

        /**
         * Records a file size metric.
         *
         * @param name Name of the metric
         * @param filePath The path to the file
         * @return This BenchmarkSession instance for chaining
         */
        fun recordFileSize(name: String, filePath: String): BenchmarkSession {
            val size = measureFileSize(filePath)
            if (size >= 0) {
                metrics["$name Size"] = formatSize(size)
            }
            return this
        }

        /**
         * Logs the total time elapsed and all recorded metrics, then finishes the benchmark session.
         *
         * @param finalMessage Optional final message
         * @return A map of all recorded metrics
         */
        fun stop(finalMessage: String = "completed"): Map<String, String> {
            val totalTime = System.currentTimeMillis() - startTime
            metrics["Total Time"] = "$totalTime ms"

            Timber.d("=== $operationName $finalMessage in $totalTime ms ===")
            Timber.d("=== Benchmark Summary ===")
            metrics.forEach { (key, value) ->
                Timber.d("$key: $value")
            }
            Timber.d("=== End of Benchmark ===")

            return metrics
        }
    }
}
