package com.smileidentity.compose.components

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieClipSpec
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.LottieConstants
import com.airbnb.lottie.compose.animateLottieCompositionAsState
import com.airbnb.lottie.compose.rememberLottieComposition
import com.smileidentity.R

@Composable
fun LottieInstruction(modifier: Modifier = Modifier, startFrame: Int = 0, endFrame: Int = 286) {
    val composition by rememberLottieComposition(
        LottieCompositionSpec.RawRes(R.raw.si_anim_instruction_screen),
    )
    val progress by animateLottieCompositionAsState(
        composition = composition,
        clipSpec = LottieClipSpec.Frame(startFrame, endFrame),
        reverseOnRepeat = false,
        ignoreSystemAnimatorScale = true,
        iterations = LottieConstants.IterateForever,
    )
    LottieAnimation(
        modifier = modifier,
        composition = composition,
        progress = { progress },
    )
}
