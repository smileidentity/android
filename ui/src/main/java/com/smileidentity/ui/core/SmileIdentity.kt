package com.smileidentity.ui.core

import android.content.Context

object SmileIdentity {
    @JvmStatic
    fun init(config: SmileIdentityConfig) = Unit

    @JvmStatic
    fun init(context: Context) = Unit // TODO: Grab the config from assets

    @JvmStatic
    fun enrollSelfie() = Unit
}
