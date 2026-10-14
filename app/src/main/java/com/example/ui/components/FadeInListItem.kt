package com.example.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer

/**
 * Applies a subtle fade-in animation to list items as they appear when scrolling through the dictionary.
 * Uses graphicsLayer to keep rendering 60/120fps smooth and non-allocating.
 */
@Composable
fun SubtleFadeInListItem(
    modifier: Modifier = Modifier,
    durationMillis: Int = 300,
    content: @Composable () -> Unit
) {
    val alphaAnim = remember { Animatable(0f) }

    LaunchedEffect(Unit) {
        alphaAnim.animateTo(
            targetValue = 1f,
            animationSpec = tween(
                durationMillis = durationMillis,
                easing = FastOutSlowInEasing
            )
        )
    }

    Box(
        modifier = modifier.graphicsLayer {
            alpha = alphaAnim.value
        }
    ) {
        content()
    }
}
