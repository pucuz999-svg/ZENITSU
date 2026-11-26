package com.example.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.model.ActiveAppliedProfile
import com.example.model.FpsGraphicsPreset
import com.example.model.SavedCalibratedProfile
import com.example.model.SensitivityValues
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "ff_apex_prefs")

class UserPreferencesRepository(private val context: Context) {

    private object Keys {
        val FAVORITE_DEVICE_IDS = stringSetPreferencesKey("favorite_device_ids")
        val CHECKLIST_COMPLETED = stringSetPreferencesKey("checklist_completed_ids")

        val CAL_GENERAL = intPreferencesKey("cal_general")
        val CAL_RED_DOT = intPreferencesKey("cal_red_dot")
        val CAL_2X = intPreferencesKey("cal_2x")
        val CAL_4X = intPreferencesKey("cal_4x")
        val CAL_SNIPER = intPreferencesKey("cal_sniper")
        val CAL_FREE_LOOK = intPreferencesKey("cal_free_look")
        val CAL_FIRE_BTN = intPreferencesKey("cal_fire_btn")
        val CAL_DPI = intPreferencesKey("cal_dpi")
        val CAL_HS_RATE = intPreferencesKey("cal_hs_rate")
        val CAL_TOUCH_HZ = intPreferencesKey("cal_touch_hz")
        val CAL_SAVED_AT = longPreferencesKey("cal_saved_at")

        val DPI_TARGET = intPreferencesKey("dpi_target")
        val THUMB_SPEED = intPreferencesKey("thumb_speed_level")

        val FORCE_MAX_REFRESH_RATE = booleanPreferencesKey("force_max_refresh_rate")
        val ADAPTIVE_BATTERY_PAUSE = booleanPreferencesKey("adaptive_battery_pause")
        val FLOATING_FPS_OVERLAY = booleanPreferencesKey("floating_fps_overlay")
        val THUNDER_SOUND_ENABLED = booleanPreferencesKey("thunder_sound_enabled")

        val ACTIVE_SOURCE_NAME = stringPreferencesKey("active_source_name")
        val ACTIVE_GENERAL = intPreferencesKey("active_general")
        val ACTIVE_RED_DOT = intPreferencesKey("active_red_dot")
        val ACTIVE_2X = intPreferencesKey("active_2x")
        val ACTIVE_4X = intPreferencesKey("active_4x")
        val ACTIVE_SNIPER = intPreferencesKey("active_sniper")
        val ACTIVE_FREE_LOOK = intPreferencesKey("active_free_look")
        val ACTIVE_FIRE_BTN = intPreferencesKey("active_fire_btn")
        val ACTIVE_DPI = intPreferencesKey("active_dpi")
        val ACTIVE_GRAPHICS = stringPreferencesKey("active_graphics")
        val ACTIVE_FPS_MODE = stringPreferencesKey("active_fps_mode")
        val ACTIVE_TARGET_FPS = intPreferencesKey("active_target_fps")
        val ACTIVE_SHADOW = booleanPreferencesKey("active_shadow")
        val ACTIVE_HIGH_RES = stringPreferencesKey("active_high_res")
        val ACTIVE_FILTER = stringPreferencesKey("active_filter")
        val ACTIVE_AUTO_SCALE = booleanPreferencesKey("active_auto_scale")
        val ACTIVE_IS_CUSTOM = booleanPreferencesKey("active_is_custom")
    }

    val favoriteDeviceIds: Flow<Set<String>> = context.dataStore.data.map { prefs ->
        prefs[Keys.FAVORITE_DEVICE_IDS] ?: emptySet()
    }

    val completedChecklistIds: Flow<Set<String>> = context.dataStore.data.map { prefs ->
        prefs[Keys.CHECKLIST_COMPLETED] ?: emptySet()
    }

    val savedCalibratedProfile: Flow<SavedCalibratedProfile?> = context.dataStore.data.map { prefs ->
        val savedAt = prefs[Keys.CAL_SAVED_AT] ?: return@map null
        SavedCalibratedProfile(
            sensitivity = SensitivityValues(
                general = prefs[Keys.CAL_GENERAL] ?: 185,
                redDot = prefs[Keys.CAL_RED_DOT] ?: 178,
                scope2x = prefs[Keys.CAL_2X] ?: 164,
                scope4x = prefs[Keys.CAL_4X] ?: 148,
                sniperScope = prefs[Keys.CAL_SNIPER] ?: 110,
                freeLook = prefs[Keys.CAL_FREE_LOOK] ?: 155,
                fireButtonPercent = prefs[Keys.CAL_FIRE_BTN] ?: 48,
                recommendedDpi = prefs[Keys.CAL_DPI] ?: 480,
            ),
            headshotRatePercent = prefs[Keys.CAL_HS_RATE] ?: 0,
            measuredTouchHz = prefs[Keys.CAL_TOUCH_HZ] ?: 60,
            savedAtEpochMs = savedAt,
        )
    }

    val savedDpiTarget: Flow<Int?> = context.dataStore.data.map { it[Keys.DPI_TARGET] }
    val savedThumbSpeed: Flow<Int> = context.dataStore.data.map { it[Keys.THUMB_SPEED] ?: 2 }

    val forceMaxRefreshRate: Flow<Boolean> = context.dataStore.data.map {
        it[Keys.FORCE_MAX_REFRESH_RATE] ?: true
    }

    val adaptiveBatteryPause: Flow<Boolean> = context.dataStore.data.map {
        it[Keys.ADAPTIVE_BATTERY_PAUSE] ?: true
    }

    val floatingFpsOverlayEnabled: Flow<Boolean> = context.dataStore.data.map {
        it[Keys.FLOATING_FPS_OVERLAY] ?: true
    }

    val thunderSoundEnabled: Flow<Boolean> = context.dataStore.data.map {
        it[Keys.THUNDER_SOUND_ENABLED] ?: true
    }

    val activeAppliedProfile: Flow<ActiveAppliedProfile?> = context.dataStore.data.map { prefs ->
        val source = prefs[Keys.ACTIVE_SOURCE_NAME] ?: return@map null
        val fpsTarget = prefs[Keys.ACTIVE_TARGET_FPS] ?: 90
        ActiveAppliedProfile(
            sourceName = source,
            sensitivity = SensitivityValues(
                general = prefs[Keys.ACTIVE_GENERAL] ?: 190,
                redDot = prefs[Keys.ACTIVE_RED_DOT] ?: 184,
                scope2x = prefs[Keys.ACTIVE_2X] ?: 170,
                scope4x = prefs[Keys.ACTIVE_4X] ?: 155,
                sniperScope = prefs[Keys.ACTIVE_SNIPER] ?: 116,
                freeLook = prefs[Keys.ACTIVE_FREE_LOOK] ?: 162,
                fireButtonPercent = prefs[Keys.ACTIVE_FIRE_BTN] ?: 48,
                recommendedDpi = prefs[Keys.ACTIVE_DPI] ?: 520,
            ),
            fpsPreset = FpsGraphicsPreset(
                graphicsQuality = prefs[Keys.ACTIVE_GRAPHICS] ?: "Standard",
                fpsMode = prefs[Keys.ACTIVE_FPS_MODE] ?: "High",
                targetFps = fpsTarget,
                shadowEnabled = prefs[Keys.ACTIVE_SHADOW] ?: false,
                highResolution = prefs[Keys.ACTIVE_HIGH_RES] ?: "High",
                filter = prefs[Keys.ACTIVE_FILTER] ?: "Bright",
                autoScale = prefs[Keys.ACTIVE_AUTO_SCALE] ?: false,
                rationale = "Active Scribd preference preset applied for $source (${fpsTarget} FPS target).",
            ),
            isCustomized = prefs[Keys.ACTIVE_IS_CUSTOM] ?: false,
        )
    }

    suspend fun toggleFavoriteDevice(deviceId: String) {
        context.dataStore.edit { prefs ->
            val current = prefs[Keys.FAVORITE_DEVICE_IDS]?.toMutableSet() ?: mutableSetOf()
            if (!current.add(deviceId)) {
                current.remove(deviceId)
            }
            prefs[Keys.FAVORITE_DEVICE_IDS] = current
        }
    }

    suspend fun toggleChecklistItem(itemId: String) {
        context.dataStore.edit { prefs ->
            val current = prefs[Keys.CHECKLIST_COMPLETED]?.toMutableSet() ?: mutableSetOf()
            if (!current.add(itemId)) {
                current.remove(itemId)
            }
            prefs[Keys.CHECKLIST_COMPLETED] = current
        }
    }

    suspend fun saveCalibratedProfile(
        sensitivity: SensitivityValues,
        headshotRatePercent: Int,
        measuredTouchHz: Int,
    ) {
        context.dataStore.edit { prefs ->
            prefs[Keys.CAL_GENERAL] = sensitivity.general
            prefs[Keys.CAL_RED_DOT] = sensitivity.redDot
            prefs[Keys.CAL_2X] = sensitivity.scope2x
            prefs[Keys.CAL_4X] = sensitivity.scope4x
            prefs[Keys.CAL_SNIPER] = sensitivity.sniperScope
            prefs[Keys.CAL_FREE_LOOK] = sensitivity.freeLook
            prefs[Keys.CAL_FIRE_BTN] = sensitivity.fireButtonPercent
            prefs[Keys.CAL_DPI] = sensitivity.recommendedDpi
            prefs[Keys.CAL_HS_RATE] = headshotRatePercent
            prefs[Keys.CAL_TOUCH_HZ] = measuredTouchHz
            prefs[Keys.CAL_SAVED_AT] = System.currentTimeMillis()
        }
    }

    suspend fun saveDpiCalculatorInputs(targetDpi: Int, thumbSpeedLevel: Int) {
        context.dataStore.edit { prefs ->
            prefs[Keys.DPI_TARGET] = targetDpi
            prefs[Keys.THUMB_SPEED] = thumbSpeedLevel
        }
    }

    suspend fun setForceMaxRefreshRate(enabled: Boolean) {
        context.dataStore.edit { prefs ->
            prefs[Keys.FORCE_MAX_REFRESH_RATE] = enabled
        }
    }

    suspend fun setAdaptiveBatteryPause(enabled: Boolean) {
        context.dataStore.edit { prefs ->
            prefs[Keys.ADAPTIVE_BATTERY_PAUSE] = enabled
        }
    }

    suspend fun setFloatingFpsOverlay(enabled: Boolean) {
        context.dataStore.edit { prefs ->
            prefs[Keys.FLOATING_FPS_OVERLAY] = enabled
        }
    }

    suspend fun setThunderSoundEnabled(enabled: Boolean) {
        context.dataStore.edit { prefs ->
            prefs[Keys.THUNDER_SOUND_ENABLED] = enabled
        }
    }

    suspend fun saveActiveAppliedProfile(
        sourceName: String,
        sensitivity: SensitivityValues,
        fpsPreset: FpsGraphicsPreset,
        isCustomized: Boolean,
    ) {
        context.dataStore.edit { prefs ->
            prefs[Keys.ACTIVE_SOURCE_NAME] = sourceName
            prefs[Keys.ACTIVE_GENERAL] = sensitivity.general
            prefs[Keys.ACTIVE_RED_DOT] = sensitivity.redDot
            prefs[Keys.ACTIVE_2X] = sensitivity.scope2x
            prefs[Keys.ACTIVE_4X] = sensitivity.scope4x
            prefs[Keys.ACTIVE_SNIPER] = sensitivity.sniperScope
            prefs[Keys.ACTIVE_FREE_LOOK] = sensitivity.freeLook
            prefs[Keys.ACTIVE_FIRE_BTN] = sensitivity.fireButtonPercent
            prefs[Keys.ACTIVE_DPI] = sensitivity.recommendedDpi
            prefs[Keys.ACTIVE_GRAPHICS] = fpsPreset.graphicsQuality
            prefs[Keys.ACTIVE_FPS_MODE] = fpsPreset.fpsMode
            prefs[Keys.ACTIVE_TARGET_FPS] = fpsPreset.targetFps
            prefs[Keys.ACTIVE_SHADOW] = fpsPreset.shadowEnabled
            prefs[Keys.ACTIVE_HIGH_RES] = fpsPreset.highResolution
            prefs[Keys.ACTIVE_FILTER] = fpsPreset.filter
            prefs[Keys.ACTIVE_AUTO_SCALE] = fpsPreset.autoScale
            prefs[Keys.ACTIVE_IS_CUSTOM] = isCustomized
        }
    }

    suspend fun clearActiveAppliedProfile() {
        context.dataStore.edit { prefs ->
            prefs.remove(Keys.ACTIVE_SOURCE_NAME)
        }
    }
}
