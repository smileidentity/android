package com.smileidentity.compose.document.composable

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.abs
import kotlin.math.roundToInt
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

// Debounced quality state that aggregates multiple readings
@Composable
fun rememberDebouncedQualityState(): DebouncedQualityState {
    val scope = rememberCoroutineScope()
    val state = remember { DebouncedQualityState() }
    LaunchedEffect(Unit) {
        // Kickstart if needed
        state.updateQuality(
            QualityReading(
                hasDocument = false,
                isBlurry = false,
                hasGlare = false,
                isTilted = false,
                variance = 500f,
            ),
            scope,
        )
    }
    return state
}

data class QualityReading(
    val hasDocument: Boolean,
    val isBlurry: Boolean,
    val hasGlare: Boolean,
    val isTilted: Boolean,
    val variance: Float = 0f,
)

data class StableQualityResult(
    val isStablyDocument: Boolean,
    val isStablyBlurry: Boolean,
    val isStablyGlared: Boolean,
    val isStablyTilted: Boolean,
    val confidence: Float,
    val averageVariance: Float,
    val readingsCount: Int,
) {
    val isGoodQuality: Boolean = isStablyDocument &&
        !isStablyBlurry && !isStablyGlared && !isStablyTilted
}

@Stable
class DebouncedQualityState {
    var currentQuality by mutableStateOf<StableQualityResult?>(null)
        private set

    private val qualityReadings = mutableListOf<QualityReading>()
    private val _qualityUpdates = MutableSharedFlow<QualityReading>(
        replay = 0,
        extraBufferCapacity = 100,
        onBufferOverflow = BufferOverflow.DROP_OLDEST,
    )

    internal var analysisCount = 0
    private var isCollectorStarted = false

    fun updateQuality(reading: QualityReading, scope: CoroutineScope) {
        val emitted = _qualityUpdates.tryEmit(reading)
        if (!emitted) {
            addReadingToWindow(reading)
            calculateStableQuality()
        }

        analysisCount++
        if (analysisCount % 5 == 0) {
            calculateStableQuality()
        }

        ensureFlowCollectorStarted(scope)
    }

    private fun addReadingToWindow(reading: QualityReading) {
        qualityReadings.add(reading)
        if (qualityReadings.size > 30) {
            qualityReadings.removeAt(0)
        }
    }

    private fun calculateStableQuality() {
        if (qualityReadings.size < 3) return

        val avgVariance = qualityReadings.map { it.variance }.average().toFloat()
        val isBlurry = avgVariance < 300f
        val isGlared = qualityReadings.count { it.hasGlare } >= 3
        val isTilted = qualityReadings.count { it.isTilted } >= 3
        val isDocument = qualityReadings.count { it.hasDocument } >= 3

        val confidence = qualityReadings.size.coerceAtMost(30) / 30f

        val newQuality = StableQualityResult(
            isStablyDocument = isDocument,
            isStablyBlurry = isBlurry,
            isStablyGlared = isGlared,
            isStablyTilted = isTilted,
            confidence = confidence,
            averageVariance = avgVariance,
            readingsCount = qualityReadings.size,
        )

        if (shouldUpdateQualityWithHysteresis(currentQuality, newQuality)) {
            currentQuality = newQuality
        }
    }

    private fun shouldUpdateQualityWithHysteresis(
        current: StableQualityResult?,
        new: StableQualityResult,
    ): Boolean {
        if (current == null) return true

        return listOf(
            current.isStablyDocument != new.isStablyDocument,
            current.isStablyBlurry != new.isStablyBlurry,
            current.isStablyGlared != new.isStablyGlared,
            current.isStablyTilted != new.isStablyTilted,
            abs(current.confidence - new.confidence) > 0.05f,
            abs(current.averageVariance - new.averageVariance) > 50f,
        ).any { it }
    }

    @OptIn(FlowPreview::class)
    private fun ensureFlowCollectorStarted(scope: CoroutineScope) {
        if (isCollectorStarted) return
        isCollectorStarted = true

        scope.launch {
            _qualityUpdates
                .onEach { addReadingToWindow(it) }
                .debounce(300)
                .collect {
                    calculateStableQuality()
                }
        }
    }
}

@Composable
fun QualityOverlay(qualityState: DebouncedQualityState, modifier: Modifier = Modifier) {
    val quality = qualityState.currentQuality

    Column(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(Color.Black.copy(alpha = 0.7f))
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        // Quality Status
        Row(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            QualityIndicator(
                label = "Document",
                isGood = quality?.isStablyDocument == true,
                confidence = quality?.confidence ?: 0f,
            )

            QualityIndicator(
                label = "Blur",
                isGood = quality?.isStablyBlurry == false,
                confidence = quality?.confidence ?: 0f,
            )

            QualityIndicator(
                label = "Glare",
                isGood = quality?.isStablyGlared == false,
                confidence = quality?.confidence ?: 0f,
            )

            QualityIndicator(
                label = "Tilt",
                isGood = quality?.isStablyTilted == false,
                confidence = quality?.confidence ?: 0f,
            )

            QualityIndicator(
                label = "Overall",
                isGood = quality?.isGoodQuality == true,
                confidence = quality?.confidence ?: 0f,
            )
        }

        // Analysis Stats
        Text(
            text = "Analyses: ${qualityState.analysisCount} | " +
                "Readings: ${quality?.readingsCount ?: 0} | " +
                "Confidence: ${((quality?.confidence ?: 0f) * 100).roundToInt()}%",
            color = Color.White,
            fontSize = 12.sp,
        )
    }
}

@Composable
private fun QualityIndicator(label: String, isGood: Boolean, confidence: Float) {
    val color = when {
        confidence < 0.3f -> Color.Gray
        isGood -> Color.Green
        else -> Color.Red
    }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(2.dp),
    ) {
        Box(
            modifier = Modifier
                .size(12.dp)
                .clip(CircleShape)
                .background(color),
        )

        Text(
            text = label,
            color = Color.White,
            fontSize = 10.sp,
            fontWeight = FontWeight.Medium,
        )
    }
}
