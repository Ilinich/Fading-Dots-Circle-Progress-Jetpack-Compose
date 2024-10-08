package com.begoml.fadingdotscircleprogress.progress

import androidx.annotation.FloatRange
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.DurationBasedAnimationSpec
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.progressSemantics
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.begoml.fadingdotscircleprogress.ui.theme.FadingDotsCircleProgressJetpackComposeTheme
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun FadingDotsCircleProgress(
    modifier: Modifier = Modifier,
    size: Dp = 150.dp,

    animation: DurationBasedAnimationSpec<Float> = tween(
        durationMillis = 3000,
        easing = LinearEasing,
    ),

    dotsSizeRange: ClosedRange<Dp> = 1.dp..10.dp,
    dotsAlphaRange: ClosedRange<Float> = 0f..1f,

    numberOfRings: Int = 3,

    dotsSeparationDistance: Dp = dotsSizeRange.endInclusive / 1.5f,

    dotColor: Color = Color.White,

    @FloatRange(0.0, 360.0) fadeAngel: Float = 150f,
) {
    val transition = rememberInfiniteTransition(
        label = "FadingDotsCircleProgress::InfiniteTransition"
    )

    val angle by transition.animateFloat(
        initialValue = 0f, targetValue = 360f, animationSpec = infiniteRepeatable(
            animation = animation,
            repeatMode = RepeatMode.Restart,
        ), label = "FadingDotsCircleProgress::animateFloat"
    )

    val maxDotDiameterPx = with(LocalDensity.current) { dotsSizeRange.endInclusive.toPx() }
    val minDotDiameterPx = with(LocalDensity.current) { dotsSizeRange.start.toPx() }

    val componentSizePx = with(LocalDensity.current) { size.toPx() }

    val ringRadiusStepPx = maxDotDiameterPx + maxDotDiameterPx / 2

    val dotSpaceDistancePx = with(LocalDensity.current) { dotsSeparationDistance.toPx() }

    val maxRadius = (componentSizePx / 2) - maxDotDiameterPx
    val minRadius = maxDotDiameterPx * 1.5

    Canvas(modifier = modifier
        .progressSemantics()
        .size(size), onDraw = {
        var currentRingRadius = maxRadius

        repeat(numberOfRings) {
            if (currentRingRadius < minRadius) return@repeat

            val circumference = 2 * Math.PI * currentRingRadius
            val totalSpacePerDot = maxDotDiameterPx + dotSpaceDistancePx
            val dotsCount = (circumference / totalSpacePerDot).toInt()

            drawDots(
                dotsCount = dotsCount,
                animatedAngle = angle,
                radius = currentRingRadius,
                maxDotDiameter = maxDotDiameterPx,
                minDotDiameter = minDotDiameterPx,
                maxDotAlpha = dotsAlphaRange.endInclusive,
                minDotAlpha = dotsAlphaRange.start,
                fadeAngel = fadeAngel,
                dotColor = dotColor,
            )

            currentRingRadius -= ringRadiusStepPx
        }
    })
}

private fun DrawScope.drawDots(
    dotsCount: Int,
    animatedAngle: Float,
    radius: Float,
    fadeAngel: Float,
    maxDotDiameter: Float,
    minDotDiameter: Float,
    maxDotAlpha: Float,
    minDotAlpha: Float,
    dotColor: Color,
) {
    val centerX = center.x
    val centerY = center.y

    val angleStep = 360f / dotsCount

    for (i in 0 until dotsCount) {
        val angle = i * angleStep

        val dotAngleOffset = (animatedAngle - angle + 360) % 360
        val isInFadeRange = dotAngleOffset <= fadeAngel || dotAngleOffset >= (360 - fadeAngel)

        val fadeInterpolation = if (isInFadeRange) {
            val adjustedDistance = if (dotAngleOffset <= fadeAngel) {
                dotAngleOffset / fadeAngel
            } else {
                (360 - dotAngleOffset) / fadeAngel
            }
            1f - adjustedDistance
        } else {
            0f
        }

        val dotDiameter = lerp(minDotDiameter, maxDotDiameter, fadeInterpolation)
        val dotAlpha = lerp(minDotAlpha, maxDotAlpha, fadeInterpolation)

        val x = centerX + radius * cos(Math.toRadians(angle.toDouble())).toFloat()
        val y = centerY + radius * sin(Math.toRadians(angle.toDouble())).toFloat()

        drawCircle(
            color = dotColor.copy(alpha = dotAlpha), radius = dotDiameter / 2, center = Offset(x, y)
        )
    }
}

private fun lerp(start: Float, end: Float, fraction: Float): Float {
    return start + fraction * (end - start)
}

@Preview
@Composable
fun FadingDotsCircleProgressPreview() {
    FadingDotsCircleProgressJetpackComposeTheme {
        Column(
            modifier = Modifier
                .background(Color.Black)
                .fillMaxSize(),
        ) {
            FadingDotsCircleProgress(
                modifier = Modifier
                    .padding(top = 50.dp)
                    .align(Alignment.CenterHorizontally),
                numberOfRings = 1,
            )
            FadingDotsCircleProgress(
                modifier = Modifier
                    .padding(top = 50.dp)
                    .align(Alignment.CenterHorizontally),
                numberOfRings = 3,
            )
            FadingDotsCircleProgress(
                modifier = Modifier
                    .padding(top = 50.dp)
                    .align(Alignment.CenterHorizontally),
                numberOfRings = 3,
                animation = tween(
                    durationMillis = 3000,
                    easing = CubicBezierEasing(0.4f, 0.2f, 0.3f, .8f),
                ),
            )
        }
    }
}