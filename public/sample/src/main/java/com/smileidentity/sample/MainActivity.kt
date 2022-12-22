package com.smileidentity.sample

import android.os.Bundle
import android.widget.Toast
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.smileidentity.ui.compose.SelfieCaptureOrPermissionScreen
import com.smileidentity.ui.core.SelfieCaptureResult
import com.smileidentity.ui.theme.SmileIdentityTheme
import timber.log.Timber

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            SmileIdentityTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    SelfieCaptureOrPermissionScreen {
                        if (it is SelfieCaptureResult.Success) {
                            val message = "Image captured successfully: ${it.selfieFile}"
                            Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
                            Timber.d(message)
                        } else if (it is SelfieCaptureResult.Error) {
                            val message = "Image capture error: $it"
                            Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
                            Timber.e(it.throwable, message)
                        }
                    }
                }
            }
        }
    }
}
