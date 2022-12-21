package com.smileidentity.sample

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.smileidentity.ui.theme.SmileIdentityTheme
import com.smileidentity.ui.SelfieCaptureOrPermissionScreen

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            SmileIdentityTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    SelfieCaptureOrPermissionScreen()
                }
            }
        }
    }
}
