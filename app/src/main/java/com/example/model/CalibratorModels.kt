package com.example.model

import kotlin.math.roundToInt

enum class TargetDistance(
    val label: String,
    val headRadiusFactor: Float,
    val idealVelocityMin: Float,
    val idealVelocityMax: Float,
) {
    CLOSE_10M("Close 10m", 1.15f, 2.2f, 4.8f),
    MID_30M("Mid 30m", 0.95f, 1.5f, 3.6f),
    LONG_60M("Long 60m", 0.78f, 1.0f, 2.7f),
}

enum class DragOutcome(val label: String, val coachHint: String) {
    HEADSHOT("HEADSHOT LOCK", "Clean upward drag velocity and straight vertical path!"),
    OVERSHOT("OVERSHOT", "Drag moved past the head hitbox. Lower General/Red Dot slightly or shorten swipe."),
    UNDERSHOT("UNDERSHOT (CHEST)", "Drag stopped in torso/chest zone. Raise General/Red Dot or flick faster."),
    OFF_AXIS("CURVED SWIPE", "Swipe drifted sideways. Keep thumb drag straight up from fire button."),
}

data class DragAttemptResult(
    val id: Long,
    val distance: TargetDistance,
    val outcome: DragOutcome,
    val velocityPxPerMs: Float,
    val angleDeviationDeg: Float,
    val dragDistanceDp: Float,
    val sampleRateHz: Int,
)

data class CalibrationSummary(
    val attemptsCount: Int,
    val headshotRatePercent: Int,
    val avgVelocityPxPerMs: Float,
    val avgAngleDeviationDeg: Float,
    val measuredTouchHz: Int,
    val recommendedSensitivity: SensitivityValues,
    val profileVerdict: String,
)

data class SavedCalibratedProfile(
    val sensitivity: SensitivityValues,
    val headshotRatePercent: Int,
    val measuredTouchHz: Int,
    val savedAtEpochMs: Long,
)

data class DpiCalculationResult(
    val currentDpi: Int,
    val targetDpi: Int,
    val displayHz: Int,
    val thumbSpeedLevel: Int, // 1=Slow, 2=Balanced, 3=Fast
    val sensitivity: SensitivityValues,
    val recommendedFpsTarget: Int,
    val coachNote: String,
) {
    companion object {
        fun calculate(
            currentDpi: Int,
            targetDpi: Int,
            displayHz: Int,
            thumbSpeedLevel: Int,
        ): DpiCalculationResult {
            val dpiRatio = (411f / targetDpi.coerceAtLeast(320)).coerceIn(0.62f, 1.25f)
            val hzAdjust = when {
                displayHz >= 120 -> 1.04f
                displayHz >= 90 -> 1.02f
                else -> 1.0f
            }
            val thumbAdjust = when (thumbSpeedLevel) {
                1 -> 1.08f // slower thumb benefits from higher sensitivity
                3 -> 0.94f // fast flick thumb benefits from slight control damping
                else -> 1.0f
            }

            val rawGeneral = (182f * dpiRatio * hzAdjust * thumbAdjust).roundToInt().coerceIn(95, 200)
            val rawRedDot = (176f * dpiRatio * hzAdjust * thumbAdjust).roundToInt().coerceIn(90, 200)
            val raw2x = (162f * dpiRatio * thumbAdjust).roundToInt().coerceIn(85, 196)
            val raw4x = (146f * dpiRatio * thumbAdjust).roundToInt().coerceIn(75, 190)
            val rawSniper = (108f * dpiRatio).roundToInt().coerceIn(55, 175)
            val rawFreeLook = (155f * dpiRatio).roundToInt().coerceIn(80, 195)
            val fireBtn = when {
                targetDpi >= 560 -> 46
                targetDpi >= 460 -> 49
                else -> 53
            }
            val note = buildString {
                if (targetDpi > currentDpi + 60) {
                    append("Raising DPI from $currentDpi to $targetDpi boosts touch responsiveness; sensitivity is scaled down slightly to prevent overshooting. ")
                } else if (targetDpi < currentDpi - 40) {
                    append("Lower DPI ($targetDpi) needs higher in-game sliders so short drags still reach head level. ")
                } else {
                    append("Balanced DPI ($targetDpi dp) keeps drag trajectory predictable without shrinking UI text. ")
                }
                if (displayHz >= 90) {
                    append("With ${displayHz}Hz enabled, set Free Fire FPS to High for smoother frame-synced drags.")
                } else {
                    append("At 60Hz, avoid excessive General >195 if frames dip during close-range gloo walls.")
                }
            }

            return DpiCalculationResult(
                currentDpi = currentDpi,
                targetDpi = targetDpi,
                displayHz = displayHz,
                thumbSpeedLevel = thumbSpeedLevel,
                sensitivity = SensitivityValues(
                    general = rawGeneral,
                    redDot = rawRedDot,
                    scope2x = raw2x,
                    scope4x = raw4x,
                    sniperScope = rawSniper,
                    freeLook = rawFreeLook,
                    fireButtonPercent = fireBtn,
                    recommendedDpi = targetDpi,
                ),
                recommendedFpsTarget = if (displayHz >= 90) 90 else 60,
                coachNote = note,
            )
        }
    }
}
