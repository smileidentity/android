package com.smileidentity.compose.document

enum class SmileDocumentType(
    val documentType: String,
    val aspectRatio: Float,
) {
    ID_CARD("ID Card", 8.5f / 11.0f),
    PASSPORT("Passport", 8.5f / 11.0f),
    DRIVING_LICENSE("Driving License", 8.5f / 11.0f),
    ;

    /**
     * Get width when height is provided
     */
    fun getWidthFromHeight(height: Float): Float = height * aspectRatio

    /**
     * Get height when width is provided
     */
    fun getHeightFromWidth(width: Float): Float = width / aspectRatio

    override fun toString(): String = documentType
}
