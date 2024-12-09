package com.smileidentity.compose.components

import androidx.annotation.RawRes
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieClipSpec
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.LottieConstants
import com.airbnb.lottie.compose.animateLottieCompositionAsState
import com.airbnb.lottie.compose.rememberLottieComposition
import com.smileidentity.R
import com.smileidentity.compose.preview.Preview
import com.smileidentity.viewmodel.SelfieHint
import com.smileidentity.viewmodel.SelfieState

@Composable
fun DirectiveVisual(selfieState: SelfieState, modifier: Modifier = Modifier) {
    when (selfieState) {
        is SelfieState.Analyzing -> when (selfieState.hint) {
            SelfieHint.NeedLight -> LottieFaceNeedLight(modifier = modifier)
            SelfieHint.SearchingForFace -> LottieFaceSearchingForFace(modifier = modifier)
            SelfieHint.MoveBack -> LottieFaceMoveBack(modifier = modifier)
            SelfieHint.MoveCloser -> LottieFaceMoveCloser(modifier = modifier)
            SelfieHint.LookLeft -> LottieFaceLookingLeft(modifier = modifier)
            SelfieHint.LookRight -> LottieFaceLookingRight(modifier = modifier)
            SelfieHint.LookUp -> LottieFaceLookingUp(modifier = modifier)
            SelfieHint.EnsureDeviceUpright -> LottieFaceEnsureDeviceUpright(modifier = modifier)
            SelfieHint.OnlyOneFace -> LottieFaceOnlyOneFace(modifier = modifier)
            SelfieHint.EnsureEntireFaceVisible -> LottieFaceEnsureEntireFaceVisible(
                modifier = modifier,
            )

            SelfieHint.PoorImageQuality -> LottieFacePoorImageQuality(modifier = modifier)
            SelfieHint.LookStraight -> LottieFaceLookStraight(modifier = modifier)
        }
        // ignore every other state that is not analyzing
        else -> {}
    }
}

@Composable
private fun LottieFace(
    modifier: Modifier = Modifier,
    @RawRes animation: Int = R.raw.si_anim_positioning,
    startFrame: Int = -1,
    endFrame: Int = -1,
) {
    val composition by rememberLottieComposition(LottieCompositionSpec.RawRes(animation))
    val progress by animateLottieCompositionAsState(
        composition = composition,
        clipSpec = LottieClipSpec.Frame(startFrame, endFrame),
        reverseOnRepeat = true,
        ignoreSystemAnimatorScale = true,
        iterations = LottieConstants.IterateForever,
    )
    LottieAnimation(
        modifier = modifier
            .size(200.dp),
        composition = composition,
        progress = { progress },
    )
}

@Composable
fun LottieFaceNeedLight(modifier: Modifier = Modifier) {
    LottieFace(
        modifier = modifier,
        animation = R.raw.si_anim_light,
    )
}

@Composable
fun LottieFaceSearchingForFace(modifier: Modifier = Modifier) {
    LottieFace(
        modifier = modifier,
        animation = R.raw.si_anim_positioning,
        startFrame = 0,
        endFrame = 60,
    )
}

@Composable
fun LottieFaceMoveBack(modifier: Modifier = Modifier) {
    LottieFace(
        modifier = modifier,
        animation = R.raw.si_anim_positioning,
        startFrame = 82,
        endFrame = 145,
    )
}

@Composable
fun LottieFaceMoveCloser(modifier: Modifier = Modifier) {
    LottieFace(
        modifier = modifier,
        animation = R.raw.si_anim_positioning,
        startFrame = 157,
        endFrame = 216,
    )
}

@Composable
fun LottieFaceLookingLeft(modifier: Modifier = Modifier) {
    LottieFace(
        modifier = modifier,
        animation = R.raw.si_anim_face,
        startFrame = 120,
        endFrame = 153,
    )
}

@Composable
fun LottieFaceLookingRight(modifier: Modifier = Modifier) {
    LottieFace(
        modifier = modifier,
        animation = R.raw.si_anim_face,
        startFrame = 30,
        endFrame = 80,
    )
}

@Composable
fun LottieFaceLookingUp(modifier: Modifier = Modifier) {
    LottieFace(
        modifier = modifier,
        animation = R.raw.si_anim_face,
        startFrame = 180,
        endFrame = 260,
    )
}

@Composable
fun LottieFaceEnsureDeviceUpright(modifier: Modifier = Modifier) {
    LottieFace(
        modifier = modifier,
        animation = R.raw.si_anim_device_orientation,
    )
}

@Composable
fun LottieFaceOnlyOneFace(modifier: Modifier = Modifier) {
    LottieFace(
        modifier = modifier,
        animation = R.raw.si_anim_positioning,
        startFrame = 0,
        endFrame = 60,
    )
}

@Composable
fun LottieFaceEnsureEntireFaceVisible(modifier: Modifier = Modifier) {
    LottieFace(
        modifier = modifier,
        animation = R.raw.si_anim_positioning,
        startFrame = 0,
        endFrame = 60,
    )
}

@Composable
fun LottieFacePoorImageQuality(modifier: Modifier = Modifier) {
    LottieFace(modifier = modifier)
}

@Composable
fun LottieFaceLookStraight(modifier: Modifier = Modifier) {
    LottieFace(
        modifier = modifier,
        animation = R.raw.si_anim_positioning,
        startFrame = 0,
        endFrame = 60,
    )
}

@Composable
fun LottieFaceSmile(modifier: Modifier = Modifier) {
    LottieFace(modifier = modifier)
}

@Preview
@Composable
private fun LottieFacePreview() {
    Preview {
        Surface {
            LottieFaceLookingUp()
        }
    }
}
