package com.smileidentity.compose.components

import android.annotation.SuppressLint
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.runtime.toMutableStateList
import com.smileidentity.models.v2.Metadata

@SuppressLint("ComposeCompositionLocalUsage")
val LocalMetadata = staticCompositionLocalOf { Metadata.default().items.toMutableStateList() }
