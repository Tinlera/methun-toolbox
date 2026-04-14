package com.tinlera.toolbox.tools.shizuku

import com.tinlera.toolbox.core.RootBridge
import com.tinlera.toolbox.core.ShellResult

object DozeManager {

    suspend fun isDozeEnabled(): Boolean {
        val result = RootBridge.executeAsSh("dumpsys deviceidle enabled")
        return result.output.trim() == "1"
    }

    suspend fun forceIdle(): ShellResult {
        RootBridge.execute("dumpsys deviceidle enable")
        return RootBridge.execute("dumpsys deviceidle force-idle")
    }

    suspend fun exitIdle(): ShellResult {
        return RootBridge.execute("dumpsys deviceidle unforce")
    }

    suspend fun getWhitelist(): List<String> {
        val result = RootBridge.execute("dumpsys deviceidle whitelist")
        return result.output.lines()
            .filter { it.contains(",") }
            .map { it.trim().removePrefix("system-excidle,").removePrefix("system,").removePrefix("user,") }
            .filter { it.isNotEmpty() }
    }

    suspend fun addToWhitelist(packageName: String): ShellResult {
        return RootBridge.execute("dumpsys deviceidle whitelist +$packageName")
    }

    suspend fun removeFromWhitelist(packageName: String): ShellResult {
        return RootBridge.execute("dumpsys deviceidle whitelist -$packageName")
    }

    suspend fun getDozeStatus(): String {
        val result = RootBridge.executeAsSh("dumpsys deviceidle | head -10")
        return result.output
    }

    suspend fun enableAggressiveDoze(): ShellResult {
        RootBridge.execute("settings put global device_idle_constants inactive_to=30000,sensing_to=0,locating_to=0,location_accuracy=2000,motion_inactive_to=0,idle_after_inactive_to=0,idle_pending_to=10000,max_idle_pending_to=20000,idle_to=60000,max_idle_to=360000,idle_factor=2.0,min_time_to_alarm=30000,max_temp_app_whitelist_duration=20000,notification_whitelist_duration=6000")
        return ShellResult(0, "Agresif Doze aktifleştirildi", "")
    }

    suspend fun resetDozeSettings(): ShellResult {
        return RootBridge.execute("settings delete global device_idle_constants")
    }
}
