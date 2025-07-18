package com.smileidentity.compose.theme.experiment

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

/**
 * Represents a font family. You can add one ore more [SmileFont] with different weights and font styles.
 */
@Parcelize
data class SmileFontFamily(val fonts: List<SmileFont>) : Parcelable
