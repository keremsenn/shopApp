package com.keremsen.e_commerce.view

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import com.airbnb.lottie.RenderMode
import com.airbnb.lottie.compose.*
import com.keremsen.e_commerce.R
import androidx.compose.ui.graphics.graphicsLayer

@Composable
fun LottieBackground() {
    val composition by rememberLottieComposition(
        LottieCompositionSpec.RawRes(R.raw.login_background)
    )

    val progress by animateLottieCompositionAsState(
        composition = composition,
        iterations = LottieConstants.IterateForever
    )

    LottieAnimation(
        composition = composition,
        progress = { progress },
        modifier = Modifier
            .fillMaxSize()
            .graphicsLayer(renderEffect = null),
        contentScale = ContentScale.FillBounds,
        renderMode = RenderMode.HARDWARE
    )
}