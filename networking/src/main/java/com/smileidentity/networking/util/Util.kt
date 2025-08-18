package com.smileidentity.networking.util

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone
import java.util.UUID
import okio.ByteString.Companion.encode

fun calculateSignature(timestamp: String): String {
    val apiKey = ""
    val hashContent = timestamp + "SmileID.partnerId" + "sid_request"
    return hashContent.encode().hmacSha256(key = apiKey.encode()).base64()
}

internal fun randomId(prefix: String) = prefix + "-" + UUID.randomUUID().toString()

fun randomUserId() = randomId("user")

fun randomJobId() = randomId("job")

/**
 * Converts current time to ISO8601 string with milliseconds in UTC
 * Format: yyyy-MM-dd'T'HH:mm:ss.SSS'Z'
 */
internal fun getCurrentIsoTimestamp(timeZone: TimeZone = TimeZone.getTimeZone("UTC")): String {
    val pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSZ"
    val sdf = SimpleDateFormat(pattern, Locale.US)
    sdf.timeZone = timeZone
    val formatted = sdf.format(Date())
    /*
    For UTC timezone the pattern adds 'Z' at the end of the string. For any other timezone the
    pattern adds the timezone offset in format +hh:mm or -hh:mm.
     */
    return if (timeZone.id == "UTC" || timeZone.rawOffset == 0) {
        // Replace +0000 or -0000 with Z for UTC
        formatted.replace("+0000", "Z").replace("-0000", "Z")
    } else {
        // Insert colon in timezone offset (+hhmm -> +hh:mm)
        formatted.replace(Regex("([+-]\\d{2})(\\d{2})$"), "$1:$2")
    }
}
