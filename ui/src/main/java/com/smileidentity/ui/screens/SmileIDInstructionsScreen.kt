package com.smileidentity.ui.screens

import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.smileidentity.ui.R
import com.smileidentity.ui.components.InstructionItem
import com.smileidentity.ui.components.SmileIDButton
import com.smileidentity.ui.design.SmileIDTheme
import com.smileidentity.ui.design.colors.SmileIDColor
import com.smileidentity.ui.previews.DevicePreviews
import com.smileidentity.ui.previews.PreviewContent
import com.smileidentity.ui.utils.SelfieInstruction

@Composable
fun SmileIDInstructionsScreen(
    modifier: Modifier = Modifier,
    scrollState: ScrollState = rememberScrollState(),
    onClose: () -> Unit = {},
    onContinue: () -> Unit = {},
    continueButton: @Composable (onContinue: () -> Unit) -> Unit = { onClick ->
        SmileIDButton(
            text = "Take Selfie",
            modifier = Modifier
                .fillMaxWidth()
                .testTag(tag = "instructions:continue_button"),
            onClick = onClick,
        )
    },
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(6.dp)
            .background(color = SmileIDTheme.colors[SmileIDColor.background]),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(10.dp),
            horizontalArrangement = Arrangement.End,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier
                    .border(
                        width = 1.dp,
                        color = SmileIDTheme.colors[SmileIDColor.stroke],
                        shape = RoundedCornerShape(10.dp),
                    ),
                contentAlignment = Alignment.TopEnd,
            ) {
                Icon(

                    modifier = Modifier
                        .padding(6.dp),
                    painter = painterResource(R.drawable.si_info),
                    contentDescription = "Close",
                    tint = SmileIDTheme.colors[SmileIDColor.cardText],
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Top,
            modifier = Modifier
                .fillMaxHeight()
                .fillMaxWidth()
                .verticalScroll(state = scrollState)
                .weight(1f),
        ) {
            Card(
                modifier = Modifier
                    .fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = SmileIDTheme.colors[SmileIDColor.cardBackground],
                ),
                shape = RoundedCornerShape(16.dp),
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth(),
                    ) {
                        Text(
                            text = "Let's verify your selfie",
                            style = SmileIDTheme.typography.pageHeading,
                            color = SmileIDTheme.colors[SmileIDColor.titleText],
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag(tag = "instructions:header"),
                        )

                        Text(
                            text = "Your selfie is encrypted and used only for \nverification.",
                            color = SmileIDTheme.colors[SmileIDColor.cardText],
                            style = SmileIDTheme.typography.subHeading,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 10.dp)
                                .testTag(tag = "instructions:subtitle"),
                        )
                    }
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        SelfieInstruction.entries.forEachIndexed { index, instruction ->
                            InstructionItem(instruction = instruction)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(
                        width = 1.dp,
                        color = SmileIDTheme.colors[SmileIDColor.warningStroke],
                        shape = RoundedCornerShape(10.dp),
                    )
                    .background(
                        color = SmileIDTheme.colors[SmileIDColor.warningFill],
                        shape = RoundedCornerShape(10.dp),
                    ),
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.Top,
                ) {
                    Icon(
                        painter = painterResource(R.drawable.si_info),
                        contentDescription = "Warning",
                        tint = SmileIDTheme.colors[SmileIDColor.warningIcon],
                        modifier = Modifier.size(24.dp),
                    )
                    Text(
                        text = "Avoid poor lighting, accessories, or looking away as this may " +
                            "lead to rejection of your selfie.",
                        style = SmileIDTheme.typography.body,
                        color = SmileIDTheme.colors[SmileIDColor.cardText],
                        modifier = Modifier.padding(start = 12.dp),
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "By clicking on Take Selfie, you consent to provide " +
                    "\nus with the requested data.",
                color = SmileIDTheme.colors[SmileIDColor.cardText],
                style = SmileIDTheme.typography.body,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                textAlign = TextAlign.Center,
            )
        }

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 24.dp),
        ) {
            continueButton(onContinue)
        }
    }
}

@DevicePreviews
@Composable
private fun SmileIDInstructionsScreenPreview() {
    PreviewContent {
        SmileIDInstructionsScreen()
    }
}

@DevicePreviews
@Composable
private fun SmileIDInstructionsScreenCustomButtonPreview() {
    PreviewContent {
        SmileIDInstructionsScreen(
            continueButton = { onContinue ->
                OutlinedButton(
                    onClick = { onContinue() },
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text(text = "Custom Button")
                }
            },
        )
    }
}
