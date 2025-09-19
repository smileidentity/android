package com.smileidentity.camera.extensions

import android.content.Context
import android.os.Build
import android.os.Handler
import java.util.concurrent.Executor

private class MainThreadExecutor(context: Context) : Executor {
    private val handler: Handler = Handler(context.mainLooper)

    override fun execute(runnable: Runnable) {
        handler.post(runnable)
    }
}

internal val Context.compatMainExecutor: Executor
    get() = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
        mainExecutor
    } else {
        MainThreadExecutor(this)
    }
