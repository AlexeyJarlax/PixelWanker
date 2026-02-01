package com.pavlovalexey.pavlovAlexeySandbox.ui.theme.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.offset
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import kotlin.math.roundToInt

@Composable
fun WankerAnimatedResetFiltersText(
    text: String,
    modifier: Modifier = Modifier,
    startAnimation: Boolean = true,
    restartKey: Int = 0,
    textAlign: TextAlign = TextAlign.Center,
    iterations: Int = 3,
    letterDelayMillis: Long = 80,
    onClick: () -> Unit,
    textColor: Color = Color.Black,
    bounceHeight: Dp = 8.dp,
    animationDuration: Int = 180,
) {
    val lines = text.split("\n")

    Column(
        modifier = modifier.clickable(
            indication = ripple(bounded = false),
            interactionSource = remember { MutableInteractionSource() }
        ) { onClick() },
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        lines.forEachIndexed { lineIndex, line ->
            Row(
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                line.forEachIndexed { charIndex, char ->
                    val delayBeforeStart =
                        (lineIndex * (line.length + 1) + charIndex) * letterDelayMillis

                    AnimatedLetter(
                        char = char,
                        startAnimation = startAnimation,
                        restartKey = restartKey,
                        startDelayMillis = delayBeforeStart,
                        iterations = iterations,
                        bounceHeight = bounceHeight,
                        animationDuration = animationDuration,
                        textColor = textColor,
                        textAlign = textAlign,
                    )
                }
            }
        }
    }
}

@Composable
private fun AnimatedLetter(
    char: Char,
    startAnimation: Boolean,
    restartKey: Int,
    startDelayMillis: Long,
    iterations: Int,
    bounceHeight: Dp,
    animationDuration: Int,
    textColor: Color,
    textAlign: TextAlign,
) {
    val offsetYPx = remember { Animatable(0f) }
    val density = LocalDensity.current
    val bouncePx = with(density) { bounceHeight.toPx() }

    LaunchedEffect(startAnimation, restartKey, bouncePx, animationDuration, iterations) {
        offsetYPx.snapTo(0f)
        if (!startAnimation) return@LaunchedEffect

        delay(startDelayMillis)

        repeat(iterations) {
            offsetYPx.animateTo(
                targetValue = -bouncePx,
                animationSpec = tween(durationMillis = animationDuration, easing = FastOutSlowInEasing)
            )
            offsetYPx.animateTo(
                targetValue = 0f,
                animationSpec = tween(durationMillis = animationDuration, easing = LinearOutSlowInEasing)
            )
            delay(animationDuration.toLong() / 2)
        }
    }

    Text(
        text = char.toString(),
        color = textColor,
        textAlign = textAlign,
        modifier = Modifier
            .alpha(if (startAnimation || offsetYPx.value != 0f) 1f else 0.85f)
            .offset { IntOffset(x = 0, y = offsetYPx.value.roundToInt()) }
    )
}