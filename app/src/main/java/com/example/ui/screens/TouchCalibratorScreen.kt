package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.BookmarkAdded
import androidx.compose.material.icons.rounded.CenterFocusStrong
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material.icons.rounded.TouchApp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.model.CalibrationSummary
import com.example.model.DragAttemptResult
import com.example.model.DragOutcome
import com.example.model.TargetDistance
import com.example.ui.components.MiniSpecChip
import com.example.ui.components.ResponsiveScreenContainer
import com.example.ui.components.SensitivityGridPanel
import com.example.ui.components.StatusPill
import com.example.ui.components.TacticalCardSurface
import com.example.ui.components.TacticalSectionHeader
import com.example.ui.theme.ApexOrange
import com.example.ui.theme.BorderSubtle
import com.example.ui.theme.CriticalRed
import com.example.ui.theme.CyanTelemetry
import com.example.ui.theme.ObsidianBg
import com.example.ui.theme.OptimalGreen
import com.example.ui.theme.TacticalCard
import com.example.ui.theme.TacticalCardBright
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.ThermalAmber
import com.example.ui.theme.ThunderInk
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.atan2
import kotlin.math.hypot
import kotlin.math.roundToInt

@Composable
fun TouchCalibratorScreen(
    targetDistance: TargetDistance,
    attempts: List<DragAttemptResult>,
    summary: CalibrationSummary?,
    onSelectDistance: (TargetDistance) -> Unit,
    onRecordAttempt: (
        outcome: DragOutcome,
        velocityPxPerMs: Float,
        angleDeviationDeg: Float,
        dragDistanceDp: Float,
        sampleRateHz: Int,
    ) -> Unit,
    onResetSession: () -> Unit,
    onSaveProfile: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val latestAttempt = attempts.lastOrNull()

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        item {
            ResponsiveScreenContainer {
                TacticalSectionHeader(
                    title = stringResource(R.string.calibrator_title),
                    icon = Icons.Rounded.CenterFocusStrong,
                    accentColor = ApexOrange,
                    trailing = {
                        if (attempts.isNotEmpty()) {
                            StatusPill(
                                text = "${attempts.size} DRAGS LOGGED",
                                color = CyanTelemetry,
                            )
                        }
                    },
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = stringResource(R.string.calibrator_subtitle),
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextSecondary,
                )
                Spacer(modifier = Modifier.height(10.dp))

                // Target Distance Selector
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    TargetDistance.entries.forEach { dist ->
                        val selected = dist == targetDistance
                        FilterChip(
                            selected = selected,
                            onClick = { onSelectDistance(dist) },
                            label = {
                                Text(
                                    text = dist.label,
                                    style = MaterialTheme.typography.labelLarge,
                                )
                            },
                            modifier = Modifier
                                .weight(1f)
                                .testTag("dist_${dist.name}"),
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = ApexOrange,
                                selectedLabelColor = TextPrimary,
                                containerColor = TacticalCard,
                                labelColor = TextSecondary,
                            ),
                            border = FilterChipDefaults.filterChipBorder(
                                enabled = true,
                                selected = selected,
                                borderColor = BorderSubtle,
                                selectedBorderColor = ApexOrange,
                            ),
                        )
                    }
                }
            }
        }

        item {
            ResponsiveScreenContainer {
                DragCalibrationArena(
                    targetDistance = targetDistance,
                    latestOutcome = latestAttempt?.outcome,
                    onSwipeCompleted = onRecordAttempt,
                )
            }
        }

        if (latestAttempt != null) {
            item {
                ResponsiveScreenContainer {
                    val outcomeColor = when (latestAttempt.outcome) {
                        DragOutcome.HEADSHOT -> OptimalGreen
                        DragOutcome.UNDERSHOT -> ThermalAmber
                        DragOutcome.OVERSHOT, DragOutcome.OFF_AXIS -> CriticalRed
                    }
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = MaterialTheme.shapes.medium,
                        color = outcomeColor.copy(alpha = 0.12f),
                        border = BorderStroke(1.dp, outcomeColor.copy(alpha = 0.5f)),
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Text(
                                    text = latestAttempt.outcome.label,
                                    style = MaterialTheme.typography.titleMedium,
                                    color = outcomeColor,
                                    fontWeight = FontWeight.Bold,
                                )
                                Text(
                                    text = "${"%.2f".format(latestAttempt.velocityPxPerMs)} px/ms • ${"%.0f".format(latestAttempt.angleDeviationDeg)}° drift • ${latestAttempt.sampleRateHz}Hz",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = TextPrimary,
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = latestAttempt.outcome.coachHint,
                                style = MaterialTheme.typography.bodyMedium,
                                color = TextSecondary,
                            )
                        }
                    }
                }
            }
        }

        item {
            ResponsiveScreenContainer {
                if (summary == null) {
                    TacticalCardSurface {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.TouchApp,
                                contentDescription = null,
                                tint = CyanTelemetry,
                                modifier = Modifier.size(32.dp),
                            )
                            Column {
                                Text(
                                    text = "Swipe Up inside the Reticle Range",
                                    style = MaterialTheme.typography.titleMedium,
                                    color = TextPrimary,
                                )
                                Text(
                                    text = "Complete 5+ upward drags from the orange Fire Zone toward the target head to compute your personalized sensitivity.",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = TextSecondary,
                                )
                            }
                        }
                    }
                } else {
                    TacticalCardSurface(borderColor = CyanTelemetry.copy(alpha = 0.45f)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Text(
                                text = "CALIBRATION TELEMETRY",
                                style = MaterialTheme.typography.labelLarge,
                                color = CyanTelemetry,
                            )
                            StatusPill(
                                text = "${summary.headshotRatePercent}% HEADSHOTS",
                                color = if (summary.headshotRatePercent >= 60) OptimalGreen else ThermalAmber,
                            )
                        }
                        Spacer(modifier = Modifier.height(10.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            MiniSpecChip(
                                label = "AVG VELOCITY",
                                value = "${"%.2f".format(summary.avgVelocityPxPerMs)} px/ms",
                                color = ApexOrange,
                                modifier = Modifier.weight(1f),
                            )
                            MiniSpecChip(
                                label = "SWIPE DRIFT",
                                value = "${"%.1f".format(summary.avgAngleDeviationDeg)}°",
                                color = CyanTelemetry,
                                modifier = Modifier.weight(1f),
                            )
                            MiniSpecChip(
                                label = "TOUCH POLL",
                                value = "${summary.measuredTouchHz} Hz",
                                color = OptimalGreen,
                                modifier = Modifier.weight(1f),
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = summary.profileVerdict,
                            style = MaterialTheme.typography.bodyMedium,
                            color = TextPrimary,
                        )

                        Spacer(modifier = Modifier.height(14.dp))
                        Text(
                            text = "PERSONALIZED CALIBRATED SENSITIVITY",
                            style = MaterialTheme.typography.labelLarge,
                            color = ApexOrange,
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        SensitivityGridPanel(sensitivity = summary.recommendedSensitivity)

                        Spacer(modifier = Modifier.height(14.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                        ) {
                            Button(
                                onClick = onSaveProfile,
                                modifier = Modifier
                                    .weight(1f)
                                    .height(48.dp)
                                    .testTag("save_calibrated_profile_button"),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = ApexOrange,
                                    contentColor = ThunderInk,
                                ),
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.BookmarkAdded,
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp),
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(stringResource(R.string.calibrator_save_profile))
                            }
                            OutlinedButton(
                                onClick = onResetSession,
                                modifier = Modifier
                                    .height(48.dp)
                                    .testTag("reset_calibrator_button"),
                                border = BorderStroke(1.dp, BorderSubtle),
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.Refresh,
                                    contentDescription = "Reset session",
                                    tint = TextPrimary,
                                )
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}

@Composable
private fun DragCalibrationArena(
    targetDistance: TargetDistance,
    latestOutcome: DragOutcome?,
    onSwipeCompleted: (
        outcome: DragOutcome,
        velocityPxPerMs: Float,
        angleDeviationDeg: Float,
        dragDistanceDp: Float,
        sampleRateHz: Int,
    ) -> Unit,
) {
    val density = LocalDensity.current
    val trailPoints = remember { mutableStateListOf<Offset>() }
    var lastTrajectory by remember { mutableStateOf<List<Offset>>(emptyList()) }
    var isDragging by remember { mutableStateOf(false) }

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .height(330.dp)
            .clip(MaterialTheme.shapes.large)
            .testTag("calibration_arena"),
        color = ObsidianBg,
        border = BorderStroke(
            1.5.dp,
            when (latestOutcome) {
                DragOutcome.HEADSHOT -> OptimalGreen.copy(alpha = 0.7f)
                DragOutcome.OVERSHOT, DragOutcome.OFF_AXIS -> CriticalRed.copy(alpha = 0.6f)
                DragOutcome.UNDERSHOT -> ThermalAmber.copy(alpha = 0.6f)
                null -> BorderSubtle
            },
        ),
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            Canvas(
                modifier = Modifier
                    .fillMaxSize()
                    .pointerInput(targetDistance) {
                        awaitEachGesture {
                            val down = awaitFirstDown(requireUnconsumed = false)
                            down.consume()
                            isDragging = true
                            trailPoints.clear()
                            trailPoints.add(down.position)

                            val startPos = down.position
                            val startTime = down.uptimeMillis
                            var lastTime = down.uptimeMillis
                            val intervalMsSamples = mutableListOf<Long>()
                            var endPos = down.position
                            var endTime = down.uptimeMillis

                            while (true) {
                                val event = awaitPointerEvent()
                                val change = event.changes.firstOrNull() ?: break
                                change.consume()
                                val now = change.uptimeMillis
                                val dt = now - lastTime
                                if (dt in 1..120) {
                                    intervalMsSamples.add(dt)
                                }
                                lastTime = now
                                endPos = change.position
                                endTime = now
                                trailPoints.add(endPos)

                                if (!change.pressed) break
                            }

                            isDragging = false
                            lastTrajectory = trailPoints.toList()

                            val dx = endPos.x - startPos.x
                            val dy = startPos.y - endPos.y // upward is positive
                            val totalDistPx = hypot(dx, dy)
                            val elapsedMs = (endTime - startTime).coerceAtLeast(16L)

                            if (totalDistPx > 28f) {
                                val velocityPxPerMs = totalDistPx / elapsedMs.toFloat()
                                val dragDistanceDp = with(density) { totalDistPx.toDp().value }
                                val angleRad = atan2(abs(dx).toDouble(), dy.coerceAtLeast(1f).toDouble())
                                val angleDeg = (angleRad * 180.0 / PI).toFloat()

                                val avgDtMs = if (intervalMsSamples.isNotEmpty()) {
                                    intervalMsSamples.average().toFloat().coerceAtLeast(1f)
                                } else {
                                    16.6f
                                }
                                val sampleHz = (1000f / avgDtMs).roundToInt().coerceIn(60, 480)

                                // Arena geometry check:
                                val arenaH = size.height.toFloat().coerceAtLeast(1f)
                                val headCenterY = arenaH * 0.24f
                                val headRadius = 32f * targetDistance.headRadiusFactor
                                val chestBottomY = arenaH * 0.56f

                                // Map swipe displacement relative to starting Fire Zone
                                val simulatedEndY = (arenaH * 0.78f) - dy

                                val outcome = when {
                                    angleDeg > 24f -> DragOutcome.OFF_AXIS
                                    simulatedEndY < (headCenterY - headRadius * 1.45f) -> DragOutcome.OVERSHOT
                                    simulatedEndY > (headCenterY + headRadius * 1.35f) ||
                                        dy < (arenaH * 0.22f) -> DragOutcome.UNDERSHOT
                                    simulatedEndY <= chestBottomY &&
                                        velocityPxPerMs in (targetDistance.idealVelocityMin * 0.45f)..(targetDistance.idealVelocityMax * 1.45f) ->
                                        DragOutcome.HEADSHOT
                                    velocityPxPerMs > targetDistance.idealVelocityMax * 1.45f -> DragOutcome.OVERSHOT
                                    else -> DragOutcome.HEADSHOT
                                }

                                onSwipeCompleted(
                                    outcome,
                                    velocityPxPerMs,
                                    angleDeg,
                                    dragDistanceDp,
                                    sampleHz,
                                )
                            }
                        }
                    },
            ) {
                val w = size.width
                val h = size.height

                // Tactical grid lines
                val gridStep = 48.dp.toPx()
                var gx = gridStep
                while (gx < w) {
                    drawLine(
                        color = BorderSubtle.copy(alpha = 0.25f),
                        start = Offset(gx, 0f),
                        end = Offset(gx, h),
                        strokeWidth = 1f,
                    )
                    gx += gridStep
                }
                var gy = gridStep
                while (gy < h) {
                    drawLine(
                        color = BorderSubtle.copy(alpha = 0.25f),
                        start = Offset(0f, gy),
                        end = Offset(w, gy),
                        strokeWidth = 1f,
                    )
                    gy += gridStep
                }

                // Vertical axis guide
                drawLine(
                    color = CyanTelemetry.copy(alpha = 0.3f),
                    start = Offset(w / 2f, h * 0.12f),
                    end = Offset(w / 2f, h * 0.85f),
                    strokeWidth = 2f,
                    pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f)),
                )

                // Target Dummy Silhouette (Head + Torso)
                val headCenter = Offset(w / 2f, h * 0.24f)
                val headRadius = 32f * targetDistance.headRadiusFactor

                // Torso
                val torsoWidth = headRadius * 3.1f
                val torsoHeight = headRadius * 3.3f
                drawRoundRect(
                    color = TacticalCardBright,
                    topLeft = Offset(headCenter.x - torsoWidth / 2f, headCenter.y + headRadius + 8f),
                    size = Size(torsoWidth, torsoHeight),
                    cornerRadius = CornerRadius(18f, 18f),
                )
                drawRoundRect(
                    color = TextMuted.copy(alpha = 0.45f),
                    topLeft = Offset(headCenter.x - torsoWidth / 2f, headCenter.y + headRadius + 8f),
                    size = Size(torsoWidth, torsoHeight),
                    cornerRadius = CornerRadius(18f, 18f),
                    style = Stroke(width = 2f),
                )

                // Head Hitbox
                val headColor = if (latestOutcome == DragOutcome.HEADSHOT) OptimalGreen else ApexOrange
                drawCircle(
                    color = headColor.copy(alpha = 0.22f),
                    radius = headRadius * 1.35f,
                    center = headCenter,
                )
                drawCircle(
                    color = headColor,
                    radius = headRadius,
                    center = headCenter,
                    style = Stroke(width = 4f),
                )
                drawCircle(
                    color = headColor,
                    radius = 6f,
                    center = headCenter,
                )

                // Fire Button Start Zone at bottom-center
                val fireCenter = Offset(w / 2f, h * 0.78f)
                val fireRadius = 36.dp.toPx()
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(ApexOrange.copy(alpha = 0.35f), Color.Transparent),
                        center = fireCenter,
                        radius = fireRadius * 1.5f,
                    ),
                    radius = fireRadius * 1.5f,
                    center = fireCenter,
                )
                drawCircle(
                    color = ApexOrange,
                    radius = fireRadius,
                    center = fireCenter,
                    style = Stroke(width = 3.5f),
                )
                drawCircle(
                    color = ApexOrange.copy(alpha = 0.2f),
                    radius = fireRadius * 0.7f,
                    center = fireCenter,
                )

                // Draw live or last swipe trajectory
                val activeTrail = if (isDragging) trailPoints else lastTrajectory
                if (activeTrail.size >= 2) {
                    val path = Path().apply {
                        moveTo(activeTrail.first().x, activeTrail.first().y)
                        for (i in 1 until activeTrail.size) {
                            lineTo(activeTrail[i].x, activeTrail[i].y)
                        }
                    }
                    val trailColor = when {
                        isDragging -> CyanTelemetry
                        latestOutcome == DragOutcome.HEADSHOT -> OptimalGreen
                        latestOutcome == DragOutcome.UNDERSHOT -> ThermalAmber
                        else -> CriticalRed
                    }
                    drawPath(
                        path = path,
                        color = trailColor,
                        style = Stroke(width = 6f, cap = StrokeCap.Round),
                    )
                    drawCircle(
                        color = trailColor,
                        radius = 10f,
                        center = activeTrail.last(),
                    )
                }
            }

            // Minimal HUD labels overlay
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Text(
                    text = "HEAD ZONE • ${targetDistance.label.uppercase()}",
                    style = MaterialTheme.typography.labelSmall,
                    color = ApexOrange,
                )
                Text(
                    text = "SWIPE STRAIGHT UP ↑",
                    style = MaterialTheme.typography.labelSmall,
                    color = CyanTelemetry,
                )
            }
            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 12.dp)
                    .background(ObsidianBg.copy(alpha = 0.7f), MaterialTheme.shapes.small)
                    .padding(horizontal = 10.dp, vertical = 3.dp),
            ) {
                Text(
                    text = "START DRAG FROM FIRE RING",
                    style = MaterialTheme.typography.labelSmall,
                    color = TextSecondary,
                )
            }
        }
    }
}
