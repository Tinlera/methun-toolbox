package com.tinlera.toolbox.tools.shizuku

import com.tinlera.toolbox.core.RootBridge
import com.tinlera.toolbox.core.ShellResult

object DisplayManager {

    suspend fun getCurrentDpi(): Int {
        val result = RootBridge.executeAsSh("wm density")
        val match = Regex("Override density: (\\d+)").find(result.output)
            ?: Regex("Physical density: (\\d+)").find(result.output)
        return match?.groupValues?.get(1)?.toIntOrNull() ?: 0
    }

    suspend fun getPhysicalDpi(): Int {
        val result = RootBridge.executeAsSh("wm density")
        val match = Regex("Physical density: (\\d+)").find(result.output)
        return match?.groupValues?.get(1)?.toIntOrNull() ?: 0
    }

    suspend fun setDpi(dpi: Int): ShellResult {
        return RootBridge.execute("wm density $dpi")
    }

    suspend fun resetDpi(): ShellResult {
        return RootBridge.execute("wm density reset")
    }

    suspend fun getCurrentResolution(): String {
        val result = RootBridge.executeAsSh("wm size")
        val match = Regex("Override size: (\\S+)").find(result.output)
            ?: Regex("Physical size: (\\S+)").find(result.output)
        return match?.groupValues?.get(1) ?: "unknown"
    }

    suspend fun getPhysicalResolution(): String {
        val result = RootBridge.executeAsSh("wm size")
        val match = Regex("Physical size: (\\S+)").find(result.output)
        return match?.groupValues?.get(1) ?: "unknown"
    }

    suspend fun setResolution(width: Int, height: Int): ShellResult {
        return RootBridge.execute("wm size ${width}x${height}")
    }

    suspend fun resetResolution(): ShellResult {
        return RootBridge.execute("wm size reset")
    }

    suspend fun getRefreshRate(): String {
        val result = RootBridge.executeAsSh("settings get system peak_refresh_rate")
        return result.output.ifBlank { "default" }
    }

    suspend fun setRefreshRate(rate: Float): ShellResult {
        val r1 = RootBridge.execute("settings put system peak_refresh_rate $rate")
        RootBridge.execute("settings put system min_refresh_rate $rate")
        return r1
    }

    suspend fun resetRefreshRate(): ShellResult {
        RootBridge.execute("settings delete system peak_refresh_rate")
        return RootBridge.execute("settings delete system min_refresh_rate")
    }

    val commonDpi = listOf(320, 360, 400, 440, 480, 520, 560, 600)
    val commonRefreshRates = listOf(60f, 90f, 120f, 144f)
}
