import android.app.Activity
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat
import com.smileidentity.compose.theme.SmileIdentityColorScheme
import com.smileidentity.compose.theme.SmileIdentityTypography

@Composable
fun SmileIdentityTheme(content: @Composable () -> Unit) {
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = false
        }
    }

    MaterialTheme(
        colorScheme = SmileIdentityColorScheme,
        typography = SmileIdentityTypography,
        content = content,
    )
}
