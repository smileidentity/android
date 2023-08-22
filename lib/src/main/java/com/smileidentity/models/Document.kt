package com.smileidentity.models

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

/**
 * Represents a document type that can be used for Document Verification.
 * @param countryCode The ISO 3166-1 alpha-3 country code of the document
 * @param documentType The document type
 */
@Parcelize
data class Document(
    val countryCode: String,
    val documentType: String,
) : Parcelable
