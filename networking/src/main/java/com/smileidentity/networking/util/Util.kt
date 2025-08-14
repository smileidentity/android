package com.smileidentity.networking.util

import java.util.UUID

internal fun randomId(prefix: String) = prefix + "-" + UUID.randomUUID().toString()

fun randomUserId() = randomId("user")

fun randomJobId() = randomId("job")
