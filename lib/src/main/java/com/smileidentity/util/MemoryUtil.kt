package com.smileidentity.util

import android.app.ActivityManager
import android.content.Context

class MemoryUtil(private val context: Context) {
    fun getAvailableMemory(): Long {
        val activityManager = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        val memoryInfo = ActivityManager.MemoryInfo()
        activityManager.getMemoryInfo(memoryInfo)
        return memoryInfo.availMem
    }
}
