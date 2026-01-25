package tn.petcare_android.ui.animations

import androidx.compose.animation.core.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import kotlinx.coroutines.delay

@Composable
fun Modifier.staggeredFadeIn(
    itemIndex: Int,
    delayPerItem: Long = 50L,
    durationMillis: Int = 400
): Modifier {
    var isVisible by remember { mutableStateOf(false) }
    val alpha by animateFloatAsState(
        targetValue = if (isVisible) 1f else 0f,
        animationSpec = tween(
            durationMillis = durationMillis,
            easing = FastOutSlowInEasing
        ),
        label = "staggeredAlpha_$itemIndex"
    )

    val offsetY by animateFloatAsState(
        targetValue = if (isVisible) 0f else 20f,
        animationSpec = tween(
            durationMillis = durationMillis,
            easing = FastOutSlowInEasing
        ),
        label = "staggeredOffset_$itemIndex"
    )

    LaunchedEffect(Unit) {
        delay(itemIndex * delayPerItem)
        isVisible = true
    }

    return this.graphicsLayer {
        this.alpha = alpha
        this.translationY = offsetY
    }
}

@Composable
fun Modifier.fadeIn(
    durationMillis: Int = 500,
    delayMillis: Long = 0L
): Modifier {
    var isVisible by remember { mutableStateOf(false) }
    val alpha by animateFloatAsState(
        targetValue = if (isVisible) 1f else 0f,
        animationSpec = tween(durationMillis = durationMillis),
        label = "fadeInAlpha"
    )

    LaunchedEffect(Unit) {
        if (delayMillis > 0) delay(delayMillis)
        isVisible = true
    }

    return this.graphicsLayer { this.alpha = alpha }
}

@Composable
fun Modifier.slideUpFadeIn(
    durationMillis: Int = 500,
    slideDistance: Float = 30f
): Modifier {
    var isVisible by remember { mutableStateOf(false) }

    val alpha by animateFloatAsState(
        targetValue = if (isVisible) 1f else 0f,
        animationSpec = tween(durationMillis = durationMillis),
        label = "slideUpAlpha"
    )

    val offsetY by animateFloatAsState(
        targetValue = if (isVisible) 0f else slideDistance,
        animationSpec = tween(
            durationMillis = durationMillis,
            easing = FastOutSlowInEasing
        ),
        label = "slideUpOffset"
    )

    LaunchedEffect(Unit) {
        isVisible = true
    }

    return this.graphicsLayer {
        this.alpha = alpha
        this.translationY = offsetY
    }
}
