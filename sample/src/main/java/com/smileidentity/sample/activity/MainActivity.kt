package com.smileidentity.sample.activity

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.core.view.WindowCompat
import com.smileidentity.sample.compose.RootScreen
import com.smileidentity.sample.compose.SmileIDTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, false)
        setContent {
            SmileIDTheme {
                RootScreen()
            }
        }
    }
}
