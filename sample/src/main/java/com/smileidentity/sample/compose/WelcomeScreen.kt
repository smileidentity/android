package com.smileidentity.sample.compose

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.ClickableText
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.smileidentity.compose.components.BottomPinnedColumn
import com.smileidentity.compose.components.annotatedStringResource
import com.smileidentity.sample.R

@Composable
fun WelcomeScreen(
    showBottomSheet: () -> Unit,
    showQRScanner: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val uriHandler = LocalUriHandler.current

    BottomPinnedColumn(
        modifier = modifier,
        scrollableContent = {
            Image(
                painter = painterResource(id = R.drawable.ic_smile_logo),
                contentDescription = "",
            )
            Text(
                text = stringResource(id = R.string.root_welcome),
                modifier = Modifier.padding(horizontal = 16.dp),
                style = MaterialTheme.typography.bodyLarge.copy(
                    fontSize = 32.sp,
                    lineHeight = 40.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = colorResource(R.color.color_digital_blue),
                ),
            )
            Spacer(modifier = Modifier.height(46.dp))
            val annotatedText = annotatedStringResource(
                id = R.string.root_description,
                spanStyles = { annotation ->
                    when (annotation.key) {
                        "is_url" -> SpanStyle(color = Color.Blue)
                        else -> null
                    }
                },
            )
            ClickableText(
                text = annotatedText,
                modifier = Modifier.padding(horizontal = 16.dp),
                style = MaterialTheme.typography.bodyMedium.copy(fontSize = 16.sp),
                onClick = { offset ->
                    annotatedText.getStringAnnotations(
                        tag = "is_url",
                        start = offset,
                        end = offset,
                    ).firstOrNull()?.let {
                        uriHandler.openUri("https://portal.smileidentity.com/sdk")
                    }
                },
            )
        },
        pinnedContent = {
            Button(
                onClick = showQRScanner,
                modifier = Modifier.padding(horizontal = 16.dp),
            ) {
                Text(
                    text = stringResource(id = R.string.root_scan_qr),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp),
                    textAlign = TextAlign.Center,
                )
            }
            OutlinedButton(
                onClick = showBottomSheet,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
            ) {
                Text(
                    text = stringResource(R.string.root_add_config),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp),
                    textAlign = TextAlign.Center,
                )
            }
        },
    )
}