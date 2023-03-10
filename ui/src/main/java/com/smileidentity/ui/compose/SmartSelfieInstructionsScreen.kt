package com.smileidentity.ui.compose

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.smileidentity.ui.R
import com.smileidentity.ui.theme.SmileIdentityLightBlue
import com.smileidentity.ui.theme.SmileIdentityTheme
import com.smileidentity.ui.theme.SmileIdentityTypography

@Composable
fun SmartSelfieInstructionsScreen(
    onInstructionsAcknowledged: () -> Unit = { },
) {
    val columnWidth = 320.dp
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.fillMaxSize(),
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier
                .fillMaxHeight()
                .width(columnWidth)
                .verticalScroll(rememberScrollState())
                .weight(1f),
        ) {
            Image(
                painter = painterResource(id = R.drawable.si_smart_selfie_instructions_hero),
                modifier = Modifier.size(128.dp),
                contentDescription = null,
            )
            Text(
                text = stringResource(R.string.si_smart_selfie_instruction_title),
                style = SmileIdentityTypography.headlineLarge,
                textAlign = TextAlign.Center,
                fontWeight = FontWeight.Bold,
            )
            Text(
                text = stringResource(id = R.string.si_smart_selfie_instruction_subtitle),
                style = SmileIdentityTypography.bodySmall,
                fontWeight = FontWeight.Light,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(top = 16.dp, bottom = 32.dp),
            )
            val instructions = listOf(
                Triple(
                    R.drawable.si_smart_selfie_instruction_good_light,
                    R.string.si_smart_selfie_instruction_good_light_title,
                    R.string.si_smart_selfie_instruction_good_light_subtitle,
                ),
                Triple(
                    R.drawable.si_smart_selfie_instruction_clear_image,
                    R.string.si_smart_selfie_instruction_clear_image_title,
                    R.string.si_smart_selfie_instruction_clear_image_subtitle,
                ),
                Triple(
                    R.drawable.si_smart_selfie_instruction_remove_obstructions,
                    R.string.si_smart_selfie_instruction_remove_obstructions_title,
                    R.string.si_smart_selfie_instruction_remove_obstructions_subtitle,
                ),
            )
            instructions.forEach { (imageId, title, subtitle) ->
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Image(
                        painter = painterResource(id = imageId),
                        contentDescription = null,
                    )
                    Column(modifier = Modifier.padding(start = 16.dp)) {
                        Text(
                            text = stringResource(title),
                            style = MaterialTheme.typography.titleMedium,
                        )
                        Text(
                            text = stringResource(subtitle),
                            style = MaterialTheme.typography.bodySmall,
                        )
                    }
                }
                Spacer(modifier = Modifier.size(24.dp))
            }
        }
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .width(columnWidth)
                .padding(8.dp),
        ) {
            Button(
                onClick = onInstructionsAcknowledged,
                colors = ButtonDefaults.buttonColors(containerColor = SmileIdentityLightBlue),
                modifier = Modifier.fillMaxWidth().testTag("readyButton"),
            ) {
                Text(stringResource(R.string.si_smart_selfie_instruction_ready_button))
            }
            SmileIdentityAttribution()
        }
    }
}

@Preview
@Composable
fun SmartSelfieInstructionsScreenPreview() {
    SmileIdentityTheme {
        Surface {
            SmartSelfieInstructionsScreen()
        }
    }
}
