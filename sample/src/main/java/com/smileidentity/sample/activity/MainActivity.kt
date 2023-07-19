package com.smileidentity.sample.activity

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.core.view.WindowCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import com.smileidentity.SmileID
import com.smileidentity.sample.compose.MainScreen
import com.smileidentity.sample.viewmodel.MainScreenViewModel
import com.smileidentity.viewmodel.viewModelFactory

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, false)
        val isSmileIDInitialized = SmileID.isInitialized
        setContent {
            MainScreen(
                viewModel = viewModel(
                    factory = viewModelFactory { MainScreenViewModel(isSmileIDInitialized = isSmileIDInitialized) },
                ),
                isSmileIDInitialized = SmileID.isInitialized
            )
        }
    }
}
