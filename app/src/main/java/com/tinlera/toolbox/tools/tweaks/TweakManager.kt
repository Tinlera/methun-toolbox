package com.tinlera.toolbox.tools.tweaks

import com.tinlera.toolbox.core.RootBridge
import com.tinlera.toolbox.core.ShellResult

object TweakManager {

    // Animation
    suspend fun getAnimationScale(): Triple<String, String, String> {
        val window = RootBridge.executeAsSh("settings get global window_animation_scale").output.trim()
        val transition = RootBridge.executeAsSh("settings get global transition_animation_scale").output.trim()
        val animator = RootBridge.executeAsSh("settings get global animator_duration_scale").output.trim()
        return Triple(
            window.ifBlank { "1.0" },
            transition.ifBlank { "1.0" },
            animator.ifBlank { "1.0" }
        )
    }

    suspend fun setAnimationScale(scale: String): ShellResult {
        RootBridge.execute("settings put global window_animation_scale $scale")
        RootBridge.execute("settings put global transition_animation_scale $scale")
        return RootBridge.execute("settings put global animator_duration_scale $scale")
    }

    // ADB
    suspend fun isAdbEnabled(): Boolean {
        val result = RootBridge.executeAsSh("settings get global adb_enabled").output.trim()
        return result == "1"
    }

    suspend fun setAdb(enabled: Boolean): ShellResult {
        return RootBridge.execute("settings put global adb_enabled ${if (enabled) "1" else "0"}")
    }

    // AOD
    suspend fun isAodEnabled(): Boolean {
        val result = RootBridge.executeAsSh("settings get secure doze_always_on").output.trim()
        return result == "1"
    }

    suspend fun setAod(enabled: Boolean): ShellResult {
        return RootBridge.execute("settings put secure doze_always_on ${if (enabled) "1" else "0"}")
    }

    // Screen timeout
    suspend fun getScreenTimeout(): Long {
        val result = RootBridge.executeAsSh("settings get system screen_off_timeout").output.trim()
        return result.toLongOrNull() ?: 60000
    }

    suspend fun setScreenTimeout(ms: Long): ShellResult {
        return RootBridge.execute("settings put system screen_off_timeout $ms")
    }

    // Navigation mode
    suspend fun getNavigationMode(): Int {
        val result = RootBridge.executeAsSh("settings get secure navigation_mode").output.trim()
        return result.toIntOrNull() ?: 2
    }

    suspend fun setNavigationMode(mode: Int): ShellResult {
        return RootBridge.execute("settings put secure navigation_mode $mode")
    }

    // Font scale
    suspend fun getFontScale(): Float {
        val result = RootBridge.executeAsSh("settings get system font_scale").output.trim()
        return result.toFloatOrNull() ?: 1.0f
    }

    suspend fun setFontScale(scale: Float): ShellResult {
        return RootBridge.execute("settings put system font_scale $scale")
    }
}
