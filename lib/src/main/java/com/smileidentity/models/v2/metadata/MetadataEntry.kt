package com.smileidentity.models.v2.metadata

import com.smileidentity.util.getCurrentIsoTimestamp

class MetadataEntry(val value: Any) {
    val timestamp: String = getCurrentIsoTimestamp()
}
