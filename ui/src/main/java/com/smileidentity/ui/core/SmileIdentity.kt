package com.smileidentity.ui.core

import android.content.Context

object SmileIdentity {
    @JvmStatic
    fun init(config: SmileIdentityConfig) = Unit

    @JvmStatic
    // TODO: Grab the config from assets
    fun init(context: Context) = Unit

    @JvmStatic
    fun enrollSelfie() = Unit
}
