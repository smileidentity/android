package com.smileidentity.models

import android.os.Parcelable
import androidx.annotation.FloatRange
import kotlinx.parcelize.Parcelize

/**
 * Represents a document type that can be used for Document Verification.
 * @param countryCode The ISO 3166-1 alpha-3 country code of the document
 * @param documentType The document type
 * @param aspectRatio The aspect ratio of the document. Defaults to 3.375f / 2.125f (1.59), which is
 * the standard ID Card and Credit Card size
 */
@Parcelize
data class Document(
    val countryCode: String,
    val documentType: String,
    @FloatRange(from = 0.0) val aspectRatio: Float = 3.375f / 2.125f,
) : Parcelable
